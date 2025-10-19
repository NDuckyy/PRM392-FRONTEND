package com.example.prm392_frontend;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Space;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.prm392_frontend.databinding.ActivityUserProfileBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class UserProfileActivity extends BaseActivity {

    // ====== CONFIG API ======
    private static final String API_ORDERS  = "https://prm392-backend.nducky.id.vn/api/order";
    private static final String API_PROFILE = "https://prm392-backend.nducky.id.vn/api/users/profile";
    // Token mới bạn đưa:
    private static final String BEARER_TOKEN =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdHJpbmciLCJyb2xlIjoiVVNFUiIsImV4cCI6MTc2MDg2NTg2MCwidXNlcklkIjo3LCJpYXQiOjE3NjA4NjIyNjB9.Ht9OVGKd0vRQ5MEKkFyVPkEFSPZJBwQ3mT0uwfz4ZvM";
    private static final String TAG = "UserProfileActivity";

    private ActivityUserProfileBinding ui;
    private final Gson gson = new Gson();
    private OkHttpClient http;

    private final ArrayList<OrderItem> orders = new ArrayList<>();
    private OrderAdapter orderAdapter;

    private boolean isEditMode = false;
    private UserProfile currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate() BEFORE setContentView");
        super.onCreate(savedInstanceState);
        ui = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(ui.getRoot());
        Log.i(TAG, "onCreate() AFTER setContentView");

        // OkHttp + logging
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg));
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        http = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request req = chain.request().newBuilder()
                            .header("Authorization", "Bearer " + BEARER_TOKEN)
                            .header("accept", "*/*")
                            .build();
                    Log.i(TAG, "➡️ " + req.method() + " " + req.url());
                    return chain.proceed(req);
                })
                .addInterceptor(logInterceptor)
                .build();

        setupToolbar();
        setupTabs();
        setupOrders();

        // gọi API
        fetchProfile();
        fetchOrders();

        // Edit/Save
        ui.fabEditSave.setOnClickListener(v -> {
            if (isEditMode) {
                if (!validateInputs()) return;
                if (currentUser == null) currentUser = new UserProfile();
                currentUser.email = ui.edtEmail.getText() == null ? "" : ui.edtEmail.getText().toString().trim();
                currentUser.phoneNumber = ui.edtPhone.getText() == null ? "" : ui.edtPhone.getText().toString().trim();
                currentUser.address = ui.edtAddress.getText() == null ? "" : ui.edtAddress.getText().toString().trim();
                Toast.makeText(this, "Saved (local)!", Toast.LENGTH_SHORT).show();
                setEditMode(false);
            } else {
                setEditMode(true);
            }
        });

        ui.btnEdit.setOnClickListener(v -> setEditMode(true));
        ui.btnSave.setOnClickListener(v -> {
            if (!validateInputs()) return;
            if (currentUser == null) currentUser = new UserProfile();
            currentUser.email = text(ui.edtEmail);
            currentUser.phoneNumber = text(ui.edtPhone);
            currentUser.address = text(ui.edtAddress);
            Toast.makeText(this, "Saved (local)!", Toast.LENGTH_SHORT).show();
            setEditMode(false);
        });
    }

    private void setupToolbar() {
        setSupportActionBar(ui.toolbar);
        ui.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupTabs() {
        TabLayout.Tab t1 = ui.tabLayout.newTab().setText("Profile");
        TabLayout.Tab t2 = ui.tabLayout.newTab().setText("Orders");
        ui.tabLayout.addTab(t1);
        ui.tabLayout.addTab(t2);

        ui.tabProfile.setVisibility(View.VISIBLE);
        ui.tabOrders.setVisibility(View.GONE);
        ui.fabEditSave.setVisibility(View.VISIBLE);

        ui.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                boolean isProfile = tab.getPosition() == 0;
                ui.tabProfile.setVisibility(isProfile ? View.VISIBLE : View.GONE);
                ui.tabOrders.setVisibility(isProfile ? View.GONE : View.VISIBLE);
                ui.fabEditSave.setVisibility(isProfile ? View.VISIBLE : View.GONE);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void setupOrders() {
        ui.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(orders);
        ui.rvOrders.setAdapter(orderAdapter);

        ui.btnRefreshOrders.setOnClickListener(v -> fetchOrders());
        ui.btnFilterOrders.setOnClickListener(v -> Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show());
        ui.btnViewAllOrders.setOnClickListener(v -> Toast.makeText(this, "View all orders", Toast.LENGTH_SHORT).show());
    }

    // ================= PROFILE =================
    private void fetchProfile() {
        Log.i(TAG, "➡️ fetchProfile(): start");
        Request req = new Request.Builder()
                .url(HttpUrl.parse(API_PROFILE))
                .get()
                .build();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "💥 fetchProfile(): failure " + e.getClass().getSimpleName() + " " + e.getMessage(), e);
                runOnUiThread(() ->
                        Toast.makeText(UserProfileActivity.this, "Profile error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override public void onResponse(Call call, Response resp) throws IOException {
                String body = resp.body() != null ? resp.body().string() : "";
                Log.i(TAG, "✅ fetchProfile(): code=" + resp.code() + ", body=" + body);
                if (!resp.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(UserProfileActivity.this, "Profile failed: " + resp.code(), Toast.LENGTH_LONG).show());
                    return;
                }
                try {
                    JsonObject root = gson.fromJson(body, JsonObject.class);
                    JsonObject data = root != null && root.has("data") && root.get("data").isJsonObject()
                            ? root.getAsJsonObject("data")
                            : null;
                    if (data == null) {
                        runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "Profile empty data", Toast.LENGTH_LONG).show());
                        return;
                    }
                    UserProfile u = new UserProfile();
                    u.id = optInt(data, "id");
                    u.username = optString(data, "username");
                    u.email = optString(data, "email");
                    u.phoneNumber = optString(data, "phoneNumber");
                    u.address = optString(data, "address");
                    u.role = optString(data, "role");
                    currentUser = u;

                    runOnUiThread(() -> {
                        fillUser(u);
                        setEditMode(false);
                    });
                } catch (Exception ex) {
                    Log.e(TAG, "💥 fetchProfile(): parse error " + ex.getMessage(), ex);
                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "Profile parse error", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void fillUser(UserProfile u) {
        ui.toolbar.setTitle(!TextUtils.isEmpty(u.username) ? u.username : "User Center");
        ui.edtUsername.setText(safe(u.username));
        ui.edtEmail.setText(safe(u.email));
        ui.edtPhone.setText(safe(u.phoneNumber));
        ui.edtAddress.setText(safe(u.address));
//        ui.edtRole.setText(safe(u.role));
        ui.btnRoleChip.setText(TextUtils.isEmpty(u.role) ? "ROLE" : u.role);
    }

    private void setEditMode(boolean enable) {
        isEditMode = enable;
        ui.edtEmail.setEnabled(enable);
        ui.edtPhone.setEnabled(enable);
        ui.edtAddress.setEnabled(enable);
        if (enable) {
            ui.fabEditSave.setText("Save");
            ui.fabEditSave.setIconResource(android.R.drawable.ic_menu_save);
        } else {
            ui.fabEditSave.setText("Edit");
            ui.fabEditSave.setIconResource(android.R.drawable.ic_menu_edit);
        }
    }

    private boolean validateInputs() {
        ui.tilEmail.setError(null);
        ui.tilPhone.setError(null);
        String email = text(ui.edtEmail);
        String phone = text(ui.edtPhone);

        boolean ok = true;
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ui.tilEmail.setError("Email không hợp lệ");
            ok = false;
        }
        if (!TextUtils.isEmpty(phone) && !phone.matches("^[0-9]{8,15}$")) {
            ui.tilPhone.setError("Số điện thoại 8-15 chữ số");
            ok = false;
        }
        return ok;
    }

    // ================= ORDERS =================
    private void fetchOrders() {
        Log.i(TAG, "➡️ fetchOrders(): start");
        ui.emptyOrders.setVisibility(View.GONE);
        ui.rvOrders.setVisibility(View.GONE);

        Request req = new Request.Builder()
                .url(HttpUrl.parse(API_ORDERS))
                .get()
                .build();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "💥 fetchOrders(): failure " + e.getClass().getSimpleName() + " " + e.getMessage(), e);
                runOnUiThread(() -> {
                    renderOrders();
                    Toast.makeText(UserProfileActivity.this, "Orders error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override public void onResponse(Call call, Response resp) throws IOException {
                String body = resp.body() != null ? resp.body().string() : "";
                Log.i(TAG, "✅ fetchOrders(): code=" + resp.code() + ", body=" + body);
                if (!resp.isSuccessful()) {
                    runOnUiThread(() -> {
                        renderOrders();
                        Toast.makeText(UserProfileActivity.this, "Orders failed: " + resp.code(), Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                try {
                    JsonObject root = gson.fromJson(body, JsonObject.class);
                    JsonArray arr = root != null && root.has("data") && root.get("data").isJsonArray()
                            ? root.getAsJsonArray("data")
                            : new JsonArray();

                    ArrayList<OrderItem> fresh = new ArrayList<>();
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject o = arr.get(i).getAsJsonObject();

                        String orderStatus   = optString(o, "orderStatus");
                        String orderDateStr  = optString(o, "orderDate");
                        String paymentMethod = optString(o, "paymentMethod");

                        // userID object -> username (nếu có)
                        String username = "";
                        if (o.has("userID") && o.get("userID").isJsonObject()) {
                            username = optString(o.getAsJsonObject("userID"), "username");
                        }

                        Date date = parseIsoDate(orderDateStr);
                        String codeStr = !TextUtils.isEmpty(paymentMethod) ? paymentMethod :
                                (TextUtils.isEmpty(username) ? "-" : username);

                        long total = o.has("totalPrice") ? o.get("totalPrice").getAsLong() : 0L;
                        int items  = o.has("itemsCount") ? o.get("itemsCount").getAsInt() : 0;

                        fresh.add(new OrderItem(codeStr, date, orderStatus, total, items));
                    }

                    runOnUiThread(() -> {
                        orders.clear();
                        orders.addAll(fresh);
                        orderAdapter.notifyDataSetChanged();
                        renderOrders();
                    });
                } catch (Exception ex) {
                    Log.e(TAG, "💥 fetchOrders(): parse error " + ex.getMessage(), ex);
                    runOnUiThread(() -> {
                        renderOrders();
                        Toast.makeText(UserProfileActivity.this, "Orders parse error", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void renderOrders() {
        boolean empty = orders.isEmpty();
        ui.emptyOrders.setVisibility(empty ? View.VISIBLE : View.GONE);
        ui.rvOrders.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ================= Helpers =================
    private static String text(com.google.android.material.textfield.TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
    private static String safe(String v) { return v == null ? "" : v; }

    private static String optString(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
    private static int optInt(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : -1;
    }

    private static Date parseIsoDate(String iso) {
        if (TextUtils.isEmpty(iso)) return new Date();
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd HH:mm:ss"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(iso);
            } catch (ParseException ignore) {}
        }
        return new Date();
    }

    public static String fmtMoney(long v) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(v) + "đ";
    }
    public static String fmtDate(Date d) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
    }

    // =============== Models nhỏ gọn để hiển thị ===============
    static class UserProfile {
        int id;
        String username;
        String email;
        String phoneNumber;
        String address;
        String role;
    }

    static class OrderItem {
        public final String code;
        public final Date date;
        public final String status;
        public final long total;
        public final int items;
        public OrderItem(String code, Date date, String status, long total, int items) {
            this.code = code;
            this.date = date;
            this.status = status;
            this.total = total;
            this.items = items;
        }
    }
}
