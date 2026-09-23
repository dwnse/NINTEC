package com.example.nintec.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.R;
import com.example.nintec.models.Order;
import com.example.nintec.models.OrderStatus;
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

        holder.tvId.setText(holder.itemView.getContext().getString(R.string.order_id_format, order.getOrderNumber()));
        holder.tvDate.setText(order.getCreatedAt());
        holder.tvTotal.setText(order.getTotal());

        // Items summary
        holder.tvItemsSummary.setText(order.getItemsSummary());

        // Status badge with dynamic styling
        OrderStatus status = order.getStatus();
        holder.tvStatus.setText(status.getDisplayName());
        switch (status) {
            case COMPLETED:
            case DELIVERED:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_success);
                break;
            case CANCELLED:
            case REJECTED:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_error);
                break;
            case PROCESSING:
            case CONFIRMED:
            case SHIPPED:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_new);
                break;
            case PENDING:
            default:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_new);
                break;
        }

        // Branch badge
        if (order.getBranchName() != null && !order.getBranchName().isEmpty()) {
            holder.tvBranch.setVisibility(View.VISIBLE);
            holder.tvBranch.setText("• " + order.getBranchName());
        } else {
            holder.tvBranch.setVisibility(View.GONE);
        }

        // Payment method
        if (order.getPaymentMethod() != null && !order.getPaymentMethod().isEmpty()) {
            holder.tvPayment.setVisibility(View.VISIBLE);
            holder.tvPayment.setText("• " + order.getPaymentMethod());
        } else {
            holder.tvPayment.setVisibility(View.GONE);
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
        TextView tvId, tvStatus, tvDate, tvTotal, tvItemsSummary, tvBranch, tvPayment;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_order_id);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
            tvItemsSummary = itemView.findViewById(R.id.tv_order_items_summary);
            tvBranch = itemView.findViewById(R.id.tv_order_branch);
            tvPayment = itemView.findViewById(R.id.tv_order_payment);
        }
    }
}