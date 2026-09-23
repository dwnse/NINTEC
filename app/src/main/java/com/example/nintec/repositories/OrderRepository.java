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
    }

    public static synchronized OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    // --- Deleted initFallbackData ---

    /**
     * Fetch user orders from Supabase REST API.
     */
    public void fetchOrders(OrderListCallback callback) {
        String userId = SessionManager.getInstance().getUserId();
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError("No hay sesión activa");
            return;
        }

        SupabaseClient.getInstance().getDataService()
                .getOrders("*,order_items(*)", "eq." + userId, "created_at.desc")
                .enqueue(new Callback<List<OrderDto>>() {
                    @Override
                    public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Order> list = new ArrayList<>();
                            for (OrderDto dto : response.body()) {
                                list.add(dto.toOrder());
                            }
                            orders = list;
                            if (callback != null) callback.onSuccess(orders);
                        } else {
                            if (callback != null) callback.onError("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
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