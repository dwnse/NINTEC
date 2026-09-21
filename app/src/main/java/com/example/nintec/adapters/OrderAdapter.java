package com.example.nintec.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.R;
import com.example.nintec.models.Order;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvId.setText(holder.itemView.getContext().getString(R.string.order_id_format, order.getId()));
        holder.tvDate.setText(holder.itemView.getContext().getString(R.string.order_date_format, order.getCreatedAt()));
        holder.tvTotal.setText(order.getTotal());

        switch (order.getStatus()) {
            case COMPLETED:
                holder.tvStatus.setText(R.string.order_status_completed);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_success);
                break;
            case PENDING:
                holder.tvStatus.setText(R.string.order_status_pending);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_new);
                break;
            case REJECTED:
                holder.tvStatus.setText(R.string.order_status_rejected);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_error);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvStatus, tvDate, tvTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_order_id);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
        }
    }
}