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
        
        double price = item.getProduct().getPriceValue();
        holder.tvSubtotal.setText(com.example.nintec.models.Product.formatPrice(price * item.getQuantity()));

        try {
            if (item.getProduct().getImageUrl() != null && !item.getProduct().getImageUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(item.getProduct().getImageUrl())
                        .placeholder(R.mipmap.ic_launcher_foreground)
                        .error(R.mipmap.ic_launcher_foreground)
                        .into(holder.imgProduct);
            } else if (item.getProduct().getImageResource() != 0) {
                holder.imgProduct.setImageResource(item.getProduct().getImageResource());
            } else {
                holder.imgProduct.setImageResource(R.mipmap.ic_launcher_foreground);
            }
        } catch (Exception e) {
            holder.imgProduct.setImageResource(R.mipmap.ic_launcher_foreground);
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