package com.example.nintec.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.R;
import com.example.nintec.models.CartItem;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<CartItem> items;

    public OrderItemAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.tvName.setText(item.getProduct().getName());
        holder.tvQty.setText(holder.itemView.getContext().getString(R.string.quantity_label, item.getQuantity()));
        
        double price = 0;
        try {
            price = Double.parseDouble(item.getProduct().getPrice().replaceAll("[^0-9.]", ""));
        } catch (Exception ignored) {}
        holder.tvSubtotal.setText(String.format(Locale.getDefault(), "Bs %.2f", price * item.getQuantity()));

        if (item.getProduct().getImageResource() != 0) {
            holder.imgProduct.setImageResource(item.getProduct().getImageResource());
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvQty, tvSubtotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_order_product);
            tvName = itemView.findViewById(R.id.tv_order_product_name);
            tvQty = itemView.findViewById(R.id.tv_order_product_qty);
            tvSubtotal = itemView.findViewById(R.id.tv_order_product_subtotal);
        }
    }
}