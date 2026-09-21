package com.example.nintec.managers;

import com.example.nintec.models.CartItem;
import com.example.nintec.models.Product;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private List<CartItem> cartItems;
    private List<CartChangeListener> listeners;

    public interface CartChangeListener {
        void onCartChanged(int totalItemCount);
    }

    private CartManager() {
        cartItems = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addListener(CartChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            listener.onCartChanged(getItemCount());
        }
    }

    public void removeListener(CartChangeListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void addProduct(Product product, int quantity) {
        if (product == null || quantity <= 0) return;

        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                notifyListeners();
                return;
            }
        }

        cartItems.add(new CartItem(product, quantity));
        notifyListeners();
    }

    public void removeItem(CartItem item) {
        if (cartItems.remove(item)) {
            notifyListeners();
        }
    }

    public void updateQuantity(CartItem item, int newQuantity) {
        if (item != null && newQuantity > 0) {
            item.setQuantity(newQuantity);
            notifyListeners();
        } else if (item != null && newQuantity == 0) {
            removeItem(item);
        }
    }

    public List<CartItem> getItems() {
        return cartItems;
    }

    public int getItemCount() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            String priceStr = item.getProduct().getPrice().replaceAll("[^0-9.]", "");
            try {
                double price = Double.parseDouble(priceStr);
                total += price * item.getQuantity();
            } catch (Exception ignored) {}
        }
        return total;
    }

    public void clear() {
        cartItems.clear();
        notifyListeners();
    }

    private void notifyListeners() {
        int count = getItemCount();
        for (CartChangeListener listener : listeners) {
            listener.onCartChanged(count);
        }
    }
}