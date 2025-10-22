package com.example.prm392_frontend;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    private final List<UserProfileActivity.OrderItem> data;

    public OrderAdapter(List<UserProfileActivity.OrderItem> data) {
        this.data = data;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();

        // Root: horizontal, padding 14dp, center_vertical
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setPadding(dp(ctx, 14), dp(ctx, 14), dp(ctx, 14), dp(ctx, 14));
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        root.setBaselineAligned(false);
        root.setDividerPadding(0);

        // Left container: vertical, weight=1
        LinearLayout left = new LinearLayout(ctx);
        left.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        left.setLayoutParams(lpLeft);

        // Title (tvTitle)
        TextView tvTitle = new TextView(ctx);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvTitle.setTypeface(null, Typeface.BOLD);

        // Sub (tvSub)
        TextView tvSub = new TextView(ctx);
        tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvSub.setAlpha(0.7f);
        LinearLayout.LayoutParams lpSub = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lpSub.topMargin = dp(ctx, 2);
        tvSub.setLayoutParams(lpSub);

        // Add to left
        left.addView(tvTitle);
        left.addView(tvSub);

        // Right text (tvRight)
        TextView tvRight = new TextView(ctx);
        tvRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvRight.setTypeface(null, Typeface.BOLD);

        // Add children
        root.addView(left);
        root.addView(tvRight);

        return new VH(root, tvTitle, tvSub, tvRight);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        UserProfileActivity.OrderItem it = data.get(pos);

        // Title: ưu tiên mã/PM (code), fallback "-"
        String title = (it.code == null || it.code.trim().isEmpty()) ? "-" : it.code.trim();
        h.tvTitle.setText(title);

        // Sub: dd/MM/yyyy • STATUS (nếu có)
        Date d = (it.date == null) ? new Date() : it.date;
        String dateStr = UserProfileActivity.fmtDate(d);
        String statusPart = (it.status == null || it.status.trim().isEmpty())
                ? "" : " • " + it.status.trim();
        h.tvSub.setText(dateStr + statusPart);

        // Right: total nếu có (>0), nếu không và có items thì "n items", ngược lại để trống
        if (it.total > 0) {
            h.tvRight.setText(UserProfileActivity.fmtMoney(it.total));
        } else if (it.items > 0) {
            h.tvRight.setText(it.items + " items");
        } else {
            h.tvRight.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public long getItemId(int position) {
        return position; // ổn định để tránh nháy khi notifyDataSetChanged
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvSub, tvRight;
        VH(@NonNull View itemView, TextView tvTitle, TextView tvSub, TextView tvRight) {
            super(itemView);
            this.tvTitle = tvTitle;
            this.tvSub = tvSub;
            this.tvRight = tvRight;
        }
    }

    private static int dp(Context c, int dp) {
        float d = c.getResources().getDisplayMetrics().density;
        return (int) (dp * d + 0.5f);
    }
}
