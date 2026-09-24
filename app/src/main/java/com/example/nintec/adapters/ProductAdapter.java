package com.example.nintec.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.R;
import com.example.nintec.models.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onBuyClick(Product product);
        default void onAddToCartClick(Product product) {}
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getPrice());
        
        if (product.getOldPrice() != null) {
            holder.tvOldPrice.setText(product.getOldPrice());
            holder.tvOldPrice.setVisibility(View.VISIBLE);
            holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvOldPrice.setVisibility(View.GONE);
        }

        if (product.isNew()) {
            holder.tvBadgeNew.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadgeNew.setVisibility(View.GONE);
        }

        try {
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.mipmap.ic_launcher_foreground)
                        .error(R.mipmap.ic_launcher_foreground)
                        .into(holder.imgProduct);
            } else if (product.getImageResource() != 0) {
                holder.imgProduct.setImageResource(product.getImageResource());
            } else {
                holder.imgProduct.setImageResource(R.mipmap.ic_launcher_foreground);
            }
        } catch (Exception e) {
            holder.imgProduct.setImageResource(R.mipmap.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });

        holder.btnBuy.setOnClickListener(v -> {
            if (listener != null) listener.onBuyClick(product);
        });

        if (holder.btnAddCart != null) {
            holder.btnAddCart.setOnClickListener(v -> {
                if (listener != null) listener.onAddToCartClick(product);
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvBadgeNew;
        TextView tvName;
        TextView tvPrice;
        TextView tvOldPrice;
        Button btnBuy;
        ImageView btnAddCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvBadgeNew = itemView.findViewById(R.id.tv_badge_new);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvOldPrice = itemView.findViewById(R.id.tv_product_old_price);
            btnBuy = itemView.findViewById(R.id.btn_buy);
            btnAddCart = itemView.findViewById(R.id.btn_card_add_cart);
        }
    }
}