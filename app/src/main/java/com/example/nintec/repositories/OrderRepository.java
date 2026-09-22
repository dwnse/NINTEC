package com.example.nintec.repositories;

import com.example.nintec.R;
import com.example.nintec.managers.SessionManager;
import com.example.nintec.models.CartItem;
import com.example.nintec.models.Order;
import com.example.nintec.models.OrderStatus;
import com.example.nintec.models.Product;
import com.example.nintec.network.SupabaseClient;
import com.example.nintec.network.dto.OrderDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {
    private static OrderRepository instance;
    private List<Order> orders;

    public interface OrderListCallback {
        void onSuccess(List<Order> orders);
        void onError(String error);
    }

    private OrderRepository() {
        orders = new ArrayList<>();
        initFallbackData();
    }

    public static synchronized OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    private void initFallbackData() {
        List<CartItem> items1 = new ArrayList<>();
        items1.add(new CartItem(new Product("1", "MacBook Pro M3 Max", "Laptops", 2499.00, 2999.00, R.mipmap.ic_launcher_foreground, true, 5), 1));
        
        List<CartItem> items2 = new ArrayList<>();
        items2.add(new CartItem(new Product("2", "iPhone 15 Pro Titanium", "Celulares", 1099.00, 1199.00, R.mipmap.ic_launcher_foreground, true, 8), 1));
        items2.add(new CartItem(new Product("3", "Audífonos Sony WH-1000XM5", "Audio", 349.00, null, R.mipmap.ic_launcher_foreground, false, 12), 1));

        orders.add(new Order("NIN-0001", items1, 2499.00, "VISA", OrderStatus.COMPLETED, "20/09/2026"));
        orders.add(new Order("NIN-0002", items2, 1448.00, "MasterCard", OrderStatus.PENDING, "22/09/2026"));
        orders.add(new Order("NIN-0003", items1, 2499.00, "QR", OrderStatus.REJECTED, "23/09/2026"));
    }

    /**
     * Fetch user orders from Supabase REST API.
     */
    public void fetchOrders(OrderListCallback callback) {
        String userId = SessionManager.getInstance().getUserId();
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onSuccess(orders);
            return;
        }

        SupabaseClient.getInstance().getDataService()
                .getOrders("*,order_items(*)", "eq." + userId, "created_at.desc")
                .enqueue(new Callback<List<OrderDto>>() {
                    @Override
                    public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Order> list = new ArrayList<>();
                            for (OrderDto dto : response.body()) {
                                list.add(dto.toOrder());
                            }
                            orders = list;
                            if (callback != null) callback.onSuccess(orders);
                        } else {
                            if (callback != null) callback.onSuccess(orders);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                        if (callback != null) callback.onSuccess(orders);
                    }
                });
    }

    public void addOrder(Order order) {
        orders.add(0, order);
    }

    public List<Order> getOrders() {
        return orders;
    }

    public Order getOrderById(String id) {
        for (Order o : orders) {
            if (o.getId().equals(id) || (o.getOrderNumber() != null && o.getOrderNumber().equals(id))) return o;
        }
        return null;
    }
}