package com.example.nintec.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nintec.R;
import com.example.nintec.models.Branch;
import java.util.List;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {

    private List<Branch> branchList;
    private OnBranchClickListener listener;

    public interface OnBranchClickListener {
        void onBranchClick(Branch branch);
        void onViewMapClick(Branch branch);
    }

    public BranchAdapter(List<Branch> branchList, OnBranchClickListener listener) {
        this.branchList = branchList;
        this.listener = listener;
    }

    public void updateList(List<Branch> newList) {
        this.branchList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_branch, parent, false);
        return new BranchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
        Branch branch = branchList.get(position);
        holder.tvName.setText(branch.getName());
        holder.tvAddress.setText(branch.getAddress());
        holder.tvHours.setText(branch.getOpeningHours());

        if (branch.getImageUrl() != null && !branch.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(branch.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher_foreground)
                    .error(R.mipmap.ic_launcher_foreground)
                    .into(holder.imgPhoto);
        } else {
            holder.imgPhoto.setImageResource(R.mipmap.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBranchClick(branch);
        });

        holder.btnViewMap.setOnClickListener(v -> {
            if (listener != null) listener.onViewMapClick(branch);
        });
    }

    @Override
    public int getItemCount() {
        return branchList == null ? 0 : branchList.size();
    }

    static class BranchViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvHours;
        Button btnViewMap;
        ImageView imgPhoto;

        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_branch_name);
            tvAddress = itemView.findViewById(R.id.tv_branch_address);
            tvHours = itemView.findViewById(R.id.tv_branch_hours);
            btnViewMap = itemView.findViewById(R.id.btn_branch_view_map);
            imgPhoto = itemView.findViewById(R.id.img_branch_photo);
        }
    }
}