package com.example.prm392_frontend.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.prm392_frontend.R;

import org.json.JSONObject;

import java.io.IOException;

import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * BadgeHelper — đồng bộ số lượng sản phẩm trong giỏ hàng và hiển thị badge trên icon app.
 * Có thể chạy mock (test nhanh) hoặc gọi API thật (yêu cầu token đăng nhập).
 */
public class BadgeHelper {
    private static final String CHANNEL_ID = "cart_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final String COUNT_URL = "https://prm392-backend.nducky.id.vn/api/cart/count";

    // Chế độ mock để test nhanh (true = dùng dữ liệu giả)
    public static boolean USE_MOCK = true;
    public static int MOCK_CART_COUNT = 7;

    /**
     * Cập nhật hiển thị badge dựa vào số lượng cart.
     */
    public static void updateCartBadge(Context ctx, int cartCount) {
        ensureChannel(ctx);

        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);

        // Xóa thông báo cũ trước khi tạo mới (để launcher nhận biết thay đổi)
        nm.cancel(NOTIFICATION_ID);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Cart updated")
                .setContentText("Your cart now has " + cartCount + " items")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setNumber(cartCount);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            b.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);
        }

        try {
            if (canPost(ctx)) {
                nm.notify((int) System.currentTimeMillis(), b.build()); // Dùng ID mới mỗi lần
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Cập nhật badge icon
        if (cartCount > 0) {
            ShortcutBadger.applyCount(ctx.getApplicationContext(), cartCount);
        } else {
            ShortcutBadger.removeCount(ctx.getApplicationContext());
        }
    }


    /**
     * Gọi API thật để lấy số lượng cart hiện tại.
     * @param ctx context
     * @param bearerToken token đăng nhập (Bearer ...)
     */
    public static void syncFromApi(Context ctx, String bearerToken) {
        if (USE_MOCK) {
            updateCartBadge(ctx, MOCK_CART_COUNT);
            return;
        }

        OkHttpClient client = new OkHttpClient();

        Request req = new Request.Builder()
                .url(COUNT_URL)
                .addHeader("accept", "*/*")
                .addHeader("Authorization", "Bearer " + (bearerToken == null ? "" : bearerToken))
                .get()
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful() || resp.body() == null) return;

                try {
                    String body = resp.body().string();
                    JSONObject json = new JSONObject(body);

                    // API trả: { "code":200, "message":"...", "data":2 }
                    int count = json.optInt("data", 0);
                    updateCartBadge(ctx, count);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Phiên bản rút gọn — vẫn giữ lại để không ảnh hưởng code cũ.
     * Nếu muốn dùng API thật, gọi hàm có token bên trên.
     */
    public static void syncFromApi(Context ctx) {
        if (USE_MOCK) {
            updateCartBadge(ctx, MOCK_CART_COUNT);
        }
    }

    // ===== Internal helpers =====
    private static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID,
                        "Cart Notifications",
                        NotificationManager.IMPORTANCE_LOW
                );
                ch.setDescription("Show number of items in cart");
                ch.setShowBadge(true);
                nm.createNotificationChannel(ch);
            }
        }
    }

    private static boolean canPost(Context ctx) {
        if (Build.VERSION.SDK_INT < 33) return true;
        return ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}
