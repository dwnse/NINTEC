package com.example.nintec.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nintec.R;
import com.example.nintec.models.Product;
import com.example.nintec.repositories.ProductRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsFragment extends Fragment {

    private RecyclerView rvProducts;
    private ProductsAdapter adapter;
    private final List<Product> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_products, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_products_back);
        rvProducts = view.findViewById(R.id.rv_admin_products);
        FloatingActionButton fabAddProduct = view.findViewById(R.id.fab_add_product);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductsAdapter(productList, new OnProductClickListener() {
            @Override
            public void onEdit(Product product) {
                AdminProductEditFragment editFragment = AdminProductEditFragment.newInstance(product.getId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDelete(Product product) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Eliminar Producto")
                        .setMessage("¿Estás seguro de que deseas eliminar este producto?")
                        .setPositiveButton("Sí", (dialog, which) -> deleteProduct(product))
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        rvProducts.setAdapter(adapter);

        fabAddProduct.setOnClickListener(v -> {
            AdminProductEditFragment addFragment = AdminProductEditFragment.newInstance(null);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadProducts();

        return view;
    }

    private void loadProducts() {
        ProductRepository.getInstance().fetchProducts(new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isAdded()) {
                    productList.clear();
                    productList.addAll(products);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteProduct(Product product) {
        ProductRepository.getInstance().deleteProduct(product.getId(), new ProductRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Producto eliminado", Toast.LENGTH_SHORT).show();
                    loadProducts();
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al eliminar: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public interface OnProductClickListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    private static class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {

        private final List<Product> products;
        private final OnProductClickListener listener;

        public ProductsAdapter(List<Product> products, OnProductClickListener listener) {
            this.products = products;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Product product = products.get(position);
            holder.bind(product, listener);
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        static class ProductViewHolder extends RecyclerView.ViewHolder {

            private final ImageView imgThumbnail;
            private final TextView txtName;
            private final TextView txtPrice;
            private final ImageView btnEdit;
            private final ImageView btnDelete;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                imgThumbnail = itemView.findViewById(R.id.img_product_thumbnail);
                txtName = itemView.findViewById(R.id.txt_product_name);
                txtPrice = itemView.findViewById(R.id.txt_product_price);
                btnEdit = itemView.findViewById(R.id.btn_edit_product);
                btnDelete = itemView.findViewById(R.id.btn_delete_product);
            }

            public void bind(Product product, OnProductClickListener listener) {
                txtName.setText(product.getName());
                txtPrice.setText(product.getPrice());

                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(product.getImageUrl())
                            .placeholder(R.color.surface_secondary)
                            .into(imgThumbnail);
                } else {
                    imgThumbnail.setImageResource(R.color.surface_secondary);
                }

                btnEdit.setOnClickListener(v -> listener.onEdit(product));
                btnDelete.setOnClickListener(v -> listener.onDelete(product));
            }
        }
    }
}