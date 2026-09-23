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

import com.example.nintec.R;
import com.example.nintec.models.Category;
import com.example.nintec.repositories.ProductRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoriesFragment extends Fragment {

    private RecyclerView rvCategories;
    private CategoriesAdapter adapter;
    private final List<Category> categoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_categories, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_categories_back);
        rvCategories = view.findViewById(R.id.rv_admin_categories);
        FloatingActionButton fabAddCategory = view.findViewById(R.id.fab_add_category);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoriesAdapter(categoryList, new OnCategoryClickListener() {
            @Override
            public void onEdit(Category category) {
                AdminCategoryEditFragment editFragment = AdminCategoryEditFragment.newInstance(category.getId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDelete(Category category) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Eliminar Categoría")
                        .setMessage("¿Estás seguro de que deseas eliminar esta categoría?")
                        .setPositiveButton("Sí", (dialog, which) -> deleteCategory(category))
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        rvCategories.setAdapter(adapter);

        fabAddCategory.setOnClickListener(v -> {
            AdminCategoryEditFragment addFragment = AdminCategoryEditFragment.newInstance(null);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadCategories();

        return view;
    }

    private void loadCategories() {
        ProductRepository.getInstance().fetchCategories(new ProductRepository.CategoryListCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (isAdded()) {
                    categoryList.clear();
                    categoryList.addAll(categories);
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

    private void deleteCategory(Category category) {
        ProductRepository.getInstance().deleteCategory(category.getId(), new ProductRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Categoría eliminada", Toast.LENGTH_SHORT).show();
                    loadCategories();
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

    public interface OnCategoryClickListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    private static class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

        private final List<Category> categories;
        private final OnCategoryClickListener listener;

        public CategoriesAdapter(List<Category> categories, OnCategoryClickListener listener) {
            this.categories = categories;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_category, parent, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.bind(category, listener);
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        static class CategoryViewHolder extends RecyclerView.ViewHolder {

            private final ImageView imgIcon;
            private final TextView txtName;
            private final TextView txtSlug;
            private final ImageView btnEdit;
            private final ImageView btnDelete;

            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                imgIcon = itemView.findViewById(R.id.img_category_icon);
                txtName = itemView.findViewById(R.id.txt_category_name);
                txtSlug = itemView.findViewById(R.id.txt_category_slug);
                btnEdit = itemView.findViewById(R.id.btn_edit_category);
                btnDelete = itemView.findViewById(R.id.btn_delete_category);
            }

            public void bind(Category category, OnCategoryClickListener listener) {
                txtName.setText(category.getName());
                txtSlug.setText(category.getSlug());

                // Set icon resource if it exists
                int resId = itemView.getContext().getResources().getIdentifier(
                        category.getIconName(), "drawable", itemView.getContext().getPackageName());
                if (resId != 0) {
                    imgIcon.setImageResource(resId);
                } else {
                    imgIcon.setImageResource(R.drawable.ic_nav_catalog);
                }

                btnEdit.setOnClickListener(v -> listener.onEdit(category));
                btnDelete.setOnClickListener(v -> listener.onDelete(category));
            }
        }
    }
}