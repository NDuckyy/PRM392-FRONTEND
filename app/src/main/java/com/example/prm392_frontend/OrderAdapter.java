package com.example.prm392_frontend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    private final List<UserProfileActivity.OrderItem> data;

    public OrderAdapter(List<UserProfileActivity.OrderItem> data) {
        this.data = data;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        UserProfileActivity.OrderItem it = data.get(pos);
        h.tvTitle.setText(it.code);
        h.tvSub.setText(UserProfileActivity.fmtDate(it.date) + " • " + it.status);
        h.tvRight.setText(UserProfileActivity.fmtMoney(it.total));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvSub, tvRight;
        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvSub   = v.findViewById(R.id.tvSub);
            tvRight = v.findViewById(R.id.tvRight);
        }
    }
}
