package com.example.nintec.repositories;

import com.example.nintec.models.Order;
import com.example.nintec.models.OrderStatus;
import com.example.nintec.models.CartItem;
import com.example.nintec.models.Product;
import com.example.nintec.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderRepository {
    private static OrderRepository instance;
    private List<Order> orders;

    private OrderRepository() {
        orders = new ArrayList<>();
        initDemoData();
    }

    public static synchronized OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    private void initDemoData() {
        // TODO: remove demo orders when backend is implemented
        List<CartItem> items1 = new ArrayList<>();
        items1.add(new CartItem(new Product("1", "MacBook Pro M3 Max", "Laptops", "Bs 2.499,00", "Bs 2.999,00", R.mipmap.ic_launcher_foreground, true, 5), 1));
        
        List<CartItem> items2 = new ArrayList<>();
        items2.add(new CartItem(new Product("2", "iPhone 15 Pro Titanium", "Celulares", "Bs 1.099,00", "Bs 1.199,00", R.mipmap.ic_launcher_foreground, true, 8), 1));
        items2.add(new CartItem(new Product("3", "Audífonos Sony WH-1000XM5", "Audio", "Bs 349,00", null, R.mipmap.ic_launcher_foreground, false, 12), 1));

        orders.add(new Order("NIN-0001", items1, "Bs 2.499,00", "VISA", OrderStatus.COMPLETED, "20/09/2026"));
        orders.add(new Order("NIN-0002", items2, "Bs 1.448,00", "MasterCard", OrderStatus.PENDING, "22/09/2026"));
        orders.add(new Order("NIN-0003", items1, "Bs 2.499,00", "QR", OrderStatus.REJECTED, "23/09/2026"));
    }

    public void addOrder(Order order) {
        orders.add(0, order); // Add to beginning for "most recent first"
    }

    public List<Order> getOrders() {
        return orders;
    }

    public Order getOrderById(String id) {
        for (Order o : orders) {
            if (o.getId().equals(id)) return o;
        }
        return null;
    }
}