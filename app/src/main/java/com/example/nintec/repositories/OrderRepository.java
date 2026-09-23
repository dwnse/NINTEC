package com.example.nintec.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.nintec.managers.SessionManager;
import com.example.nintec.models.CartItem;
import com.example.nintec.models.Order;
import com.example.nintec.models.OrderStatus;
import com.example.nintec.models.Product;
import com.example.nintec.models.User;
import com.example.nintec.network.SupabaseClient;
import com.example.nintec.network.dto.OrderDto;
import com.example.nintec.NintecApp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Order repository with persistent local storage (SharedPreferences + Gson)
 * and Supabase REST API synchronization.
 *
 * Orders are saved locally per-user AND into a global device cache so they survive
 * app restarts, session closures, and logouts.
 */
public class OrderRepository {
    private static final String TAG = "OrderRepository";
    private static final String PREFS_NAME = "nintec_orders";
    private static final String KEY_PREFIX = "orders_user_";
    private static final String KEY_GLOBAL_BACKUP = "orders_global_device_backup";

    private static OrderRepository instance;
    private List<Order> orders;
    private final Gson gson = new Gson();

    public interface OrderListCallback {
        void onSuccess(List<Order> orders);
        void onError(String error);
    }

    public interface OrderSaveCallback {
        void onSuccess(Order order);
        void onError(String error);
    }

    private OrderRepository() {
        orders = new ArrayList<>();
        loadFromLocalStorage();

        // Listen for session login / logout to dynamically switch orders context
        try {
            SessionManager session = SessionManager.getInstance();
            if (session != null) {
                session.addUserChangeListener(new SessionManager.UserChangeListener() {
                    @Override
                    public void onUserChanged(User user) {
                        onSessionUserChanged(user);
                    }
                });
            }
        } catch (Exception e) {
            Log.w(TAG, "Error registering session listener: " + e.getMessage());
        }
    }

    public static synchronized OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    // ─── Local Persistence ───

    private String getUserKey() {
        String userId = SessionManager.getInstance().getUserId();
        if (userId != null && !userId.isEmpty()) {
            return KEY_PREFIX + userId;
        }
        String email = SessionManager.getInstance().getUserEmail();
        if (email != null && !email.isEmpty()) {
            return KEY_PREFIX + Math.abs(email.hashCode());
        }
        return KEY_PREFIX + "anon";
    }

    private SharedPreferences getPrefs() {
        Context ctx = NintecApp.getAppContext();
        if (ctx == null) return null;
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private synchronized void loadFromLocalStorage() {
        SharedPreferences prefs = getPrefs();
        if (prefs == null) return;

        String key = getUserKey();
        String json = prefs.getString(key, null);

        // If user-specific storage is empty, fallback to global backup so orders are never lost
        if ((json == null || json.isEmpty() || json.equals("[]")) && !key.endsWith("anon")) {
            json = prefs.getString(KEY_GLOBAL_BACKUP, null);
        } else if (json == null || json.isEmpty()) {
            json = prefs.getString(KEY_GLOBAL_BACKUP, null);
        }

        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<SerializableOrder>>(){}.getType();
                List<SerializableOrder> stored = gson.fromJson(json, type);
                if (stored != null) {
                    orders.clear();
                    for (SerializableOrder so : stored) {
                        orders.add(so.toOrder());
                    }
                    Log.d(TAG, "Loaded " + orders.size() + " orders from local storage (key: " + key + ")");
                }
            } catch (Exception e) {
                Log.w(TAG, "Error parsing local orders: " + e.getMessage());
            }
        }
    }

    private synchronized void saveToLocalStorage() {
        SharedPreferences prefs = getPrefs();
        if (prefs == null) return;

        try {
            List<SerializableOrder> serializableOrders = new ArrayList<>();
            for (Order order : orders) {
                serializableOrders.add(SerializableOrder.fromOrder(order));
            }
            String json = gson.toJson(serializableOrders);

            SharedPreferences.Editor editor = prefs.edit();
            // 1. Save to active user key
            editor.putString(getUserKey(), json);

            // 2. Also merge and save to global device backup so logout never clears history
            mergeIntoGlobalBackup(prefs, editor, serializableOrders);

            editor.apply();
            Log.d(TAG, "Saved " + orders.size() + " orders to local storage");
        } catch (Exception e) {
            Log.w(TAG, "Error saving local orders: " + e.getMessage());
        }
    }

    private void mergeIntoGlobalBackup(SharedPreferences prefs, SharedPreferences.Editor editor, List<SerializableOrder> newOrders) {
        try {
            String globalJson = prefs.getString(KEY_GLOBAL_BACKUP, null);
            Map<String, SerializableOrder> map = new HashMap<>();

            if (globalJson != null && !globalJson.isEmpty()) {
                Type type = new TypeToken<List<SerializableOrder>>(){}.getType();
                List<SerializableOrder> existing = gson.fromJson(globalJson, type);
                if (existing != null) {
                    for (SerializableOrder so : existing) {
                        map.put(so.id != null ? so.id : so.orderNumber, so);
                    }
                }
            }

            for (SerializableOrder so : newOrders) {
                map.put(so.id != null ? so.id : so.orderNumber, so);
            }

            editor.putString(KEY_GLOBAL_BACKUP, gson.toJson(new ArrayList<>(map.values())));
        } catch (Exception e) {
            Log.w(TAG, "Error merging global backup: " + e.getMessage());
        }
    }

    /**
     * Reacts to user login/logout from SessionManager.
     */
    private void onSessionUserChanged(User user) {
        if (user != null) {
            // Check if there are anonymous orders placed before logging in, migrate them
            SharedPreferences prefs = getPrefs();
            if (prefs != null) {
                String anonJson = prefs.getString(KEY_PREFIX + "anon", null);
                if (anonJson != null && !anonJson.isEmpty()) {
                    try {
                        Type type = new TypeToken<List<SerializableOrder>>(){}.getType();
                        List<SerializableOrder> anonOrders = gson.fromJson(anonJson, type);
                        if (anonOrders != null && !anonOrders.isEmpty()) {
                            for (SerializableOrder so : anonOrders) {
                                boolean exists = false;
                                for (Order o : orders) {
                                    if (o.getId().equals(so.id) || o.getOrderNumber().equals(so.orderNumber)) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) orders.add(so.toOrder());
                            }
                            // Clear anon after migration
                            prefs.edit().remove(KEY_PREFIX + "anon").apply();
                        }
                    } catch (Exception ignored) {}
                }
            }

            loadFromLocalStorage();
            // Also sync from remote Supabase in background
            fetchOrders(null);
        } else {
            // User logged out: reload from device backup so order history remains viewable
            loadFromLocalStorage();
        }
    }

    public void onUserLogin() {
        onSessionUserChanged(SessionManager.getInstance().getCurrentUser());
    }

    // ─── Supabase Fetch ───

    public void fetchOrders(OrderListCallback callback) {
        // Return local data immediately for instant responsive UI
        if (!orders.isEmpty() && callback != null) {
            callback.onSuccess(new ArrayList<>(orders));
        }

        String userId = SessionManager.getInstance().getUserId();
        if (userId == null || userId.isEmpty() || !isValidUuid(userId)) {
            // If not a valid Supabase UUID, rely entirely on local persistent storage
            if (callback != null && orders.isEmpty()) {
                callback.onSuccess(new ArrayList<>(orders));
            }
            return;
        }

        SupabaseClient.getInstance().getDataService()
                .getOrders("*,order_items(*),branches:branch_id(id,name,city,address)",
                        "eq." + userId, "created_at.desc")
                .enqueue(new Callback<List<OrderDto>>() {
                    @Override
                    public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Order> remoteOrders = new ArrayList<>();
                            for (OrderDto dto : response.body()) {
                                remoteOrders.add(dto.toOrder());
                            }
                            mergeOrders(remoteOrders);
                            saveToLocalStorage();
                            if (callback != null) callback.onSuccess(new ArrayList<>(orders));
                        } else {
                            if (callback != null) callback.onSuccess(new ArrayList<>(orders));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                        Log.w(TAG, "Fetch orders failed: " + t.getMessage());
                        if (callback != null) {
                            if (!orders.isEmpty()) {
                                callback.onSuccess(new ArrayList<>(orders));
                            } else {
                                callback.onError("Error de red al cargar pedidos");
                            }
                        }
                    }
                });
    }

    private void mergeOrders(List<Order> remoteOrders) {
        Map<String, Order> remoteMap = new HashMap<>();
        for (Order r : remoteOrders) {
            remoteMap.put(r.getId(), r);
            if (r.getOrderNumber() != null) remoteMap.put(r.getOrderNumber(), r);
        }

        for (Order local : orders) {
            if (!remoteMap.containsKey(local.getId()) &&
                    !remoteMap.containsKey(local.getOrderNumber())) {
                remoteOrders.add(local);
            }
        }

        orders = remoteOrders;
    }

    // ─── Create Order with Supabase REST insert ───

    public void addOrder(Order order) {
        orders.add(0, order);
        saveToLocalStorage();
    }

    /**
     * Insert order into Supabase orders + order_items tables via REST.
     * Always saves locally immediately so nothing is ever lost.
     */
    public void createAndSyncOrder(Order order, OrderSaveCallback callback) {
        // 1. Save locally first for instant persistence
        addOrder(order);

        // 2. Validate Supabase conditions
        String userId = SessionManager.getInstance().getUserId();
        if (userId == null || userId.isEmpty() || !isValidUuid(userId)) {
            Log.d(TAG, "Order saved locally. User is anonymous or has non-UUID ID.");
            if (callback != null) callback.onSuccess(order);
            return;
        }

        // Build order payload
        Map<String, Object> orderPayload = new HashMap<>();
        orderPayload.put("user_id", userId);
        orderPayload.put("order_number", order.getOrderNumber());
        orderPayload.put("status", order.getStatus().toApiString());
        orderPayload.put("payment_method", order.getPaymentMethod());
        orderPayload.put("payment_status", "paid");
        orderPayload.put("subtotal", order.getSubtotal() > 0 ? order.getSubtotal() : order.getTotalValue());
        orderPayload.put("discount", order.getDiscount());
        orderPayload.put("tax", 0);
        orderPayload.put("shipping_cost", 0);
        orderPayload.put("total", order.getTotalValue());

        if (order.getBranchId() != null && isValidUuid(order.getBranchId())) {
            orderPayload.put("branch_id", order.getBranchId());
        }

        StringBuilder notesBuilder = new StringBuilder();
        if (order.getNotes() != null && !order.getNotes().trim().isEmpty()) {
            notesBuilder.append(order.getNotes().trim());
        }
        if (order.getBranchName() != null && !order.getBranchName().trim().isEmpty()) {
            if (notesBuilder.length() > 0) notesBuilder.append(" | ");
            notesBuilder.append("Sucursal: ").append(order.getBranchName().trim());
        }
        if (notesBuilder.length() > 0) {
            orderPayload.put("notes", notesBuilder.toString());
        }

        if (order.getPaymentDetails() != null && !order.getPaymentDetails().trim().isEmpty()) {
            orderPayload.put("admin_notes", "Pago: " + order.getPaymentDetails().trim());
        }

        SupabaseClient.getInstance().getDataService()
                .insertOrder(orderPayload, "return=representation")
                .enqueue(new Callback<List<OrderDto>>() {
                    @Override
                    public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            OrderDto created = response.body().get(0);
                            order.setId(created.id);
                            order.setOrderNumber(created.orderNumber);

                            // Insert order items
                            insertOrderItems(created.id, order.getItems());

                            saveToLocalStorage();
                            Log.d(TAG, "Order synced to Supabase: " + created.orderNumber);
                            if (callback != null) callback.onSuccess(order);
                        } else {
                            String errorBody = "";
                            try { errorBody = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                            Log.w(TAG, "Order insert failed: " + response.code() + " " + errorBody);
                            if (callback != null) callback.onSuccess(order);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                        Log.w(TAG, "Order sync failed: " + t.getMessage());
                        if (callback != null) callback.onSuccess(order);
                    }
                });
    }

    private void insertOrderItems(String orderId, List<CartItem> items) {
        if (items == null || items.isEmpty() || orderId == null || !isValidUuid(orderId)) return;

        for (CartItem item : items) {
            Map<String, Object> itemPayload = new HashMap<>();
            itemPayload.put("order_id", orderId);

            String productId = item.getProduct().getId();
            if (isValidUuid(productId)) {
                itemPayload.put("product_id", productId);
            }
            // If productId is not a UUID, product_id is left null, allowing the insert to succeed
            itemPayload.put("product_name", item.getProduct().getName());
            itemPayload.put("product_image", item.getProduct().getImageUrl());
            itemPayload.put("unit_price", item.getProduct().getPriceValue());
            itemPayload.put("quantity", item.getQuantity());
            itemPayload.put("line_total", item.getProduct().getPriceValue() * item.getQuantity());

            SupabaseClient.getInstance().getDataService()
                    .insertOrderItem(itemPayload, "return=minimal")
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!response.isSuccessful()) {
                                Log.w(TAG, "Order item insert failed: " + response.code());
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.w(TAG, "Order item insert network error: " + t.getMessage());
                        }
                    });
        }
    }

    private static boolean isValidUuid(String s) {
        if (s == null || s.length() != 36) return false;
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ─── Queries ───

    public synchronized List<Order> getOrders() {
        if (orders.isEmpty()) {
            loadFromLocalStorage();
        }
        return new ArrayList<>(orders);
    }

    public Order getOrderById(String id) {
        if (id == null) return null;
        for (Order o : orders) {
            if (id.equals(o.getId()) || id.equals(o.getOrderNumber())) return o;
        }
        return null;
    }

    /** Generate unique order number (e.g. NIN-84920) */
    public String generateOrderNumber() {
        long timestampSec = (System.currentTimeMillis() / 1000) % 90000 + 10000;
        int randomSuffix = (int) (Math.random() * 900 + 100);
        return "NIN-" + timestampSec;
    }

    // ─── Serializable wrapper for Gson ───

    private static class SerializableOrder {
        String id;
        String orderNumber;
        double total;
        double subtotal;
        double discount;
        String paymentMethod;
        String paymentDetails;
        String status;
        String createdAt;
        String branchId;
        String branchName;
        String branchCity;
        String branchAddress;
        String notes;
        List<SerializableItem> items;

        static SerializableOrder fromOrder(Order o) {
            SerializableOrder so = new SerializableOrder();
            so.id = o.getId();
            so.orderNumber = o.getOrderNumber();
            so.total = o.getTotalValue();
            so.subtotal = o.getSubtotal();
            so.discount = o.getDiscount();
            so.paymentMethod = o.getPaymentMethod();
            so.paymentDetails = o.getPaymentDetails();
            so.status = o.getStatus().toApiString();
            so.createdAt = o.getCreatedAt();
            so.branchId = o.getBranchId();
            so.branchName = o.getBranchName();
            so.branchCity = o.getBranchCity();
            so.branchAddress = o.getBranchAddress();
            so.notes = o.getNotes();
            so.items = new ArrayList<>();
            if (o.getItems() != null) {
                for (CartItem ci : o.getItems()) {
                    SerializableItem si = new SerializableItem();
                    si.productId = ci.getProduct().getId();
                    si.productName = ci.getProduct().getName();
                    si.productImage = ci.getProduct().getImageUrl();
                    si.price = ci.getProduct().getPriceValue();
                    si.quantity = ci.getQuantity();
                    so.items.add(si);
                }
            }
            return so;
        }

        Order toOrder() {
            List<CartItem> cartItems = new ArrayList<>();
            if (items != null) {
                for (SerializableItem si : items) {
                    Product p = new Product(
                            si.productId != null ? si.productId : "",
                            si.productName != null ? si.productName : "Producto",
                            "", si.price, null, si.productImage, false, 10);
                    cartItems.add(new CartItem(p, si.quantity));
                }
            }
            Order order = new Order(id, orderNumber, cartItems, total,
                    paymentMethod != null ? paymentMethod : "Efectivo",
                    OrderStatus.fromApiString(status), createdAt);
            order.setSubtotal(subtotal);
            order.setDiscount(discount);
            order.setPaymentDetails(paymentDetails);
            order.setBranchId(branchId);
            order.setBranchName(branchName);
            order.setBranchCity(branchCity);
            order.setBranchAddress(branchAddress);
            order.setNotes(notes);
            return order;
        }
    }

    private static class SerializableItem {
        String productId;
        String productName;
        String productImage;
        double price;
        int quantity;
    }
}