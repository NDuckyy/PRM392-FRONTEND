package com.example.prm392_frontend;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONObject;

import java.io.IOException;

import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "cart_channel";
    private static final int NOTIFICATION_ID = 1001;

    // Mock / API toggle
    private static final boolean USE_MOCK = true;
    private static final int MOCK_CART_COUNT = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        // Android 13+ permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                return;
            }
        }

        if (USE_MOCK) {
            updateCartBadge(MOCK_CART_COUNT);
        } else {
            fetchCartCountFromApi();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Cart Notifications";
            String description = "Show number of items in cart";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateCartBadge(int cartCount) {
        Log.d("CartBadge", "Cart count = " + cartCount);

        // Dùng notification nhẹ để hệ thống "cập nhật badge" (nếu launcher yêu cầu)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Cart updated")
                .setContentText("Your cart now has " + cartCount + " items")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setNumber(cartCount); // dùng setNumber để báo số lượng

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // Áp dụng badge số lượng cart thực tế
        if (cartCount > 0) {
            ShortcutBadger.applyCount(getApplicationContext(), cartCount);
        } else {
            ShortcutBadger.removeCount(getApplicationContext());
        }
    }

    private void fetchCartCountFromApi() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://run.mocky.io/v3/62fd0f90-54b6-4d3b-894f-f37a9bd18e88"; // {"count": 5}

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("CartBadge", "API error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        int cartCount = json.getInt("count");
                        runOnUiThread(() -> updateCartBadge(cartCount));
                    } catch (Exception e) {
                        Log.e("CartBadge", "JSON error: " + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (USE_MOCK) {
                updateCartBadge(MOCK_CART_COUNT);
            } else {
                fetchCartCountFromApi();
            }
        }
    }
}
