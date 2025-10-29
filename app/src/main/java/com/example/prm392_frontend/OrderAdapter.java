package com.example.prm392_frontend;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    private final List<UserProfileActivity.OrderItem> data;

    public OrderAdapter(List<UserProfileActivity.OrderItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(ctx, 16), dp(ctx, 12), dp(ctx, 16), dp(ctx, 12));
        TextView tv = new TextView(ctx);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        root.addView(tv);
        return new VH(root, tv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        UserProfileActivity.OrderItem it = data.get(pos);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        StringBuilder sb = new StringBuilder();
        sb.append("👤 User: ").append(it.username).append(" (").append(it.email).append(")\n")
                .append("📞 ").append(it.phone).append(" | 🏠 ").append(it.address).append("\n")
                .append("💳 Payment: ").append(it.paymentMethod).append("\n")
                .append("📅 Date: ").append(df.format(it.orderDate)).append("\n")
                .append("📦 Status: ").append(it.orderStatus).append("\n\n")
                .append("🛒 Order Details:\n");

        if (it.details != null && !it.details.isEmpty()) {
            for (UserProfileActivity.OrderDetail d : it.details) {
                sb.append(" • ")
                        .append(d.productName)
                        .append(" x").append(d.quantity)
                        .append(" (₫").append(d.unitPrice).append(")\n");
            }
        } else {
            sb.append("   No items.\n");
        }

        h.tv.setText(sb.toString());
        // ✅ Sửa lỗi: truyền đúng kiểu tham số cho setTypeface
        h.tv.setTypeface(Typeface.MONOSPACE);
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tv;
        VH(@NonNull LinearLayout itemView, TextView tv) {
            super(itemView);
            this.tv = tv;
        }
    }

    private static int dp(Context c, int dp) {
        float d = c.getResources().getDisplayMetrics().density;
        return (int) (dp * d + 0.5f);
    }
}
