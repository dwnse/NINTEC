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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemActionListener listener;

    public interface OnCartItemActionListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onRemove(CartItem item);
        void onProductClick(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemActionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void updateList(List<CartItem> newList) {
        this.cartItems = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvName.setText(item.getProduct().getName());
        holder.tvPrice.setText(item.getProduct().getPrice());
        holder.tvQty.setText(String.valueOf(item.getQuantity()));

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

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) listener.onIncrease(item);
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null) listener.onDecrease(item);
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems == null ? 0 : cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnRemove;
        TextView tvName, tvPrice, tvQty, btnPlus, btnMinus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_cart_product);
            tvName = itemView.findViewById(R.id.tv_cart_product_name);
            tvPrice = itemView.findViewById(R.id.tv_cart_product_price);
            tvQty = itemView.findViewById(R.id.tv_cart_qty_counter);
            btnPlus = itemView.findViewById(R.id.btn_cart_qty_plus);
            btnMinus = itemView.findViewById(R.id.btn_cart_qty_minus);
            btnRemove = itemView.findViewById(R.id.btn_cart_remove);
        }
    }
}