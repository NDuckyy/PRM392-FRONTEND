package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.prm392_frontend.databinding.ActivityUserProfileBinding;
import com.example.prm392_frontend.utils.AuthHelper;
import com.example.prm392_frontend.utils.AuthInterceptor;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class UserProfileActivity extends BaseActivity {

    private static final String API_ORDERS  = "https://prm392-backend.nducky.id.vn/api/order";
    private static final String API_PROFILE = "https://prm392-backend.nducky.id.vn/api/users/profile";
    private static final String TAG = "UserProfileActivity";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ActivityUserProfileBinding ui;
    private final Gson gson = new Gson();
    private OkHttpClient http;
    private final ArrayList<OrderItem> orders = new ArrayList<>();
    private OrderAdapter orderAdapter;
    private boolean isEditMode = false;
    private UserProfile currentUser;
    private AuthHelper auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(ui.getRoot());

        auth = new AuthHelper(getApplicationContext());
        Log.i(TAG, "isLoggedIn=" + auth.isLoggedIn());

        String rawToken = auth.getToken();
        if (!auth.isLoggedIn() || rawToken == null || rawToken.trim().isEmpty()) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        HttpLoggingInterceptor httpLogger = new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg));
        httpLogger.setLevel(HttpLoggingInterceptor.Level.BODY);

        http = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(auth))
                .addInterceptor(chain -> {
                    Request r0 = chain.request();
                    Log.i(TAG, "➡️ " + r0.method() + " " + r0.url());
                    Log.i(TAG, "Authorization = " + r0.header("Authorization"));
                    return chain.proceed(r0);
                })
                .addInterceptor(httpLogger)
                .build();

        setupToolbar();
        setupTabs();
        setupOrders();
        fetchProfile();
        fetchOrders();

        ui.fabEditSave.setOnClickListener(v -> {
            if (isEditMode) {
                if (!validateInputs()) return;
                if (currentUser == null) currentUser = new UserProfile();
                currentUser.email = text(ui.edtEmail);
                currentUser.phoneNumber = text(ui.edtPhone);
                currentUser.address = text(ui.edtAddress);
                updateProfile(currentUser);
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
            updateProfile(currentUser);
        });

        ui.btnBecomeProvider.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, BecomeProviderActivity.class);
            startActivity(intent);
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
    }

    private void fetchProfile() {
        Request req = new Request.Builder().url(HttpUrl.parse(API_PROFILE)).get().build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(UserProfileActivity.this, "Profile error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override public void onResponse(Call call, Response resp) throws IOException {
                String body = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(UserProfileActivity.this, "Profile failed: " + resp.code(), Toast.LENGTH_LONG).show());
                    return;
                }
                JsonObject data = gson.fromJson(body, JsonObject.class)
                        .getAsJsonObject("data");

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
            }
        });
    }

    private void updateProfile(UserProfile u) {
        JsonObject payload = new JsonObject();
        payload.addProperty("email", safe(u.email));
        payload.addProperty("phoneNumber", safe(u.phoneNumber));
        payload.addProperty("address", safe(u.address));
        RequestBody rb = RequestBody.create(gson.toJson(payload), JSON);
        Request req = new Request.Builder().url(HttpUrl.parse(API_PROFILE)).put(rb).build();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(UserProfileActivity.this, "Cập nhật lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override public void onResponse(Call call, Response resp) throws IOException {
                runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show());
                fetchProfile();
            }
        });
    }

    private void fetchOrders() {
        Request req = new Request.Builder().url(HttpUrl.parse(API_ORDERS)).get().build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(UserProfileActivity.this, "Orders error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override public void onResponse(Call call, Response resp) throws IOException {
                String body = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) return;
                JsonObject root = gson.fromJson(body, JsonObject.class);
                JsonArray arr = root.getAsJsonArray("data");

                ArrayList<OrderItem> fresh = new ArrayList<>();
                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();
                    JsonObject user = o.getAsJsonObject("userID");
                    OrderItem item = new OrderItem();
                    item.username = optString(user, "username");
                    item.email = optString(user, "email");
                    item.phone = optString(user, "phoneNumber");
                    item.address = optString(user, "address");
                    item.role = optString(user, "role");
                    item.paymentMethod = optString(o, "paymentMethod");
                    item.billingAddress = optString(o, "billingAddress");
                    item.orderStatus = optString(o, "orderStatus");
                    item.orderDate = parseIsoDate(optString(o, "orderDate"));
                    item.details = new ArrayList<>();
                    JsonArray orderDetails = o.getAsJsonArray("orderDetails");
                    if (orderDetails != null) {
                        for (JsonElement dEl : orderDetails) {
                            JsonObject d = dEl.getAsJsonObject();
                            item.details.add(new OrderDetail(
                                    optInt(d, "id"),
                                    optInt(d, "productId"),
                                    optString(d, "productName"),
                                    optInt(d, "quantity"),
                                    d.has("unitPrice") ? d.get("unitPrice").getAsLong() : 0L
                            ));
                        }
                    }
                    fresh.add(item);
                }

                runOnUiThread(() -> {
                    orders.clear();
                    orders.addAll(fresh);
                    orderAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void fillUser(UserProfile u) {
        ui.toolbar.setTitle(safe(u.username));
        ui.edtUsername.setText(safe(u.username));
        ui.edtEmail.setText(safe(u.email));
        ui.edtPhone.setText(safe(u.phoneNumber));
        ui.edtAddress.setText(safe(u.address));
        ui.btnRoleChip.setText(TextUtils.isEmpty(u.role) ? "ROLE" : u.role);
    }

    private void setEditMode(boolean enable) {
        isEditMode = enable;
        ui.edtEmail.setEnabled(enable);
        ui.edtPhone.setEnabled(enable);
        ui.edtAddress.setEnabled(enable);
        ui.fabEditSave.setText(enable ? "Save" : "Edit");
    }

    private boolean validateInputs() {
        String email = text(ui.edtEmail);
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ui.tilEmail.setError("Email không hợp lệ");
            return false;
        }
        return true;
    }

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
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(iso);
        } catch (Exception e) {
            return new Date();
        }
    }

    static class UserProfile {
        int id;
        String username;
        String email;
        String phoneNumber;
        String address;
        String role;
    }

    static class OrderItem {
        String username, email, phone, address, role;
        String paymentMethod, billingAddress, orderStatus;
        Date orderDate;
        ArrayList<OrderDetail> details;
    }

    static class OrderDetail {
        int id, productId, quantity;
        String productName;
        long unitPrice;
        OrderDetail(int id, int productId, String name, int qty, long price) {
            this.id = id; this.productId = productId;
            this.productName = name; this.quantity = qty; this.unitPrice = price;
        }
    }
}
