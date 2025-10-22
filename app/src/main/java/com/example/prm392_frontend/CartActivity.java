package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.CartItemUpdateRequest;
import com.example.prm392_frontend.models.CartItemUpdateResponse;
import com.example.prm392_frontend.models.CartResponse;
import com.example.prm392_frontend.models.OrderRequest;
import com.example.prm392_frontend.models.OrderResponse;
import com.example.prm392_frontend.utils.AuthHelper;
// Sửa lại import, không cần import CartAdapter 2 lần
// import com.example.prm392_frontend.CartAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartAdapterListener {

    // REFACTOR: Chuyển token ra làm biến toàn cục để dễ dàng thay đổi
    private final String hardcodedToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJxdWFuIiwicm9sZSI6IlVTRVIiLCJleHAiOjE3NjEwNDQwODMsInVzZXJJZCI6OSwiaWF0IjoxNzYxMDQwNDgzfQ.xYWyu-1d9y8shkgahVACw1Z1DT7vE-SdsaufxnX4FR0";

    private MaterialToolbar topAppBar;
    private RecyclerView recyclerViewCartItems;
    private LinearLayout emptyCartView;
    private TextView textViewTotalPrice;
    private EditText editTextAddress;
    private CheckBox checkboxUseSavedAddress;
    private RadioButton radioButtonOnline;
    private RadioButton radioButtonCOD;
    private Button buttonPlaceOrder;
    private MaterialCardView bottomBar;
    private MaterialCardView deliveryInfoCard;

    private CartAdapter cartAdapter;
    private AuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        initViews();
        initComponents();
        setupRecyclerView(); // <-- Sửa lỗi khởi tạo Adapter trong hàm này
        setupClickListeners();
        fetchCartData();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        emptyCartView = findViewById(R.id.emptyCartView);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        editTextAddress = findViewById(R.id.editTextAddress);
        checkboxUseSavedAddress = findViewById(R.id.checkboxUseSavedAddress);
        radioButtonOnline = findViewById(R.id.radioButtonOnline);
        radioButtonCOD = findViewById(R.id.radioButtonCOD);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
        bottomBar = findViewById(R.id.bottomBar);
        deliveryInfoCard = findViewById(R.id.deliveryInfoCard);
    }

    private void initComponents() {
        authHelper = new AuthHelper(this);
    }

    // ====================================================================
    // SỬA LỖI GỐC NẰM Ở ĐÂY
    // ====================================================================
    private void setupRecyclerView() {
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(this));
        // Sửa lại cách khởi tạo adapter để gọi đúng constructor
        // `this` ở đây chính là `CartActivity` đang implement `CartAdapterListener`
        cartAdapter = new CartAdapter(this);
        recyclerViewCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_all) {
                showClearAllConfirmationDialog();
                return true;
            }
            return false;
        });

        buttonPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
        checkboxUseSavedAddress.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextAddress.setEnabled(!isChecked);
            if (isChecked) {
                // editTextAddress.setText(authHelper.getUserAddress()); // Ví dụ
                editTextAddress.setText("Địa chỉ đã lưu");
            } else {
                editTextAddress.setText("");
            }
        });
    }

    private void fetchCartData() {
        showLoading(true);
        String token = hardcodedToken;
        if (token == null || token.isEmpty()) {
            showEmptyView("Vui lòng đăng nhập để xem giỏ hàng.");
            return;
        }
        String authHeader = "Bearer " + token;

        ApiClient.getCartApi().getCurrentUserCart(authHeader).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartResponse>> call, Response<ApiResponse<CartResponse>> response) {
                // Đây là nơi lỗi được kích hoạt, nhưng nguyên nhân là do adapter khởi tạo sai
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CartResponse cartResponse = response.body().getData();
                    if (cartResponse != null && cartResponse.getCartItemResponses() != null && !cartResponse.getCartItemResponses().isEmpty()) {
                        showDataView();
                        cartAdapter.updateData(cartResponse.getCartItemResponses());
                        updateTotalPrice(cartResponse.getTotalPrice());
                    } else {
                        showEmptyView("Giỏ hàng của bạn đang trống");
                    }
                } else {
                    showEmptyView("Không thể tải giỏ hàng. Lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CartResponse>> call, Throwable t) {
                showEmptyView("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    @Override
    public void onUpdateQuantity(int cartItemId, int newQuantity) {
        showLoading(true);
        String token = hardcodedToken;
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
            showDataView(); // Quay lại trạng thái hiển thị dữ liệu
            return;
        }
        String authHeader = "Bearer " + token;
        CartItemUpdateRequest request = new CartItemUpdateRequest(newQuantity);

        ApiClient.cartUpdateQuantity(authHeader, cartItemId, request).enqueue(new Callback<ApiResponse<CartItemUpdateResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartItemUpdateResponse>> call, Response<ApiResponse<CartItemUpdateResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CartActivity.this, "Cập nhật giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CartActivity.this, "Cập nhật thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
                fetchCartData(); // Load lại để đồng bộ tổng tiền
            }

            @Override
            public void onFailure(Call<ApiResponse<CartItemUpdateResponse>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                fetchCartData();
            }
        });
    }

    @Override
    public void onDeleteItem(int cartItemId) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> performDeleteItem(cartItemId))
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performDeleteItem(int cartItemId) {
        showLoading(true);
        String token = hardcodedToken;
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
            showDataView();
            return;
        }
        String authHeader = "Bearer " + token;

        ApiClient.cartDeleteItem(authHeader, cartItemId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CartActivity.this, "Đã xóa sản phẩm thành công.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CartActivity.this, "Xóa sản phẩm thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
                fetchCartData();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                fetchCartData();
            }
        });
    }

    private void handlePlaceOrder() {
        if (cartAdapter.getItemCount() == 0) {
            Toast.makeText(this, "Giỏ hàng trống, không thể đặt hàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        String address = editTextAddress.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng.", Toast.LENGTH_SHORT).show();
            editTextAddress.requestFocus();
            return;
        }
        String token = hardcodedToken;
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn.", Toast.LENGTH_LONG).show();
            return;
        }
        final String authHeader = "Bearer " + token;
        final String paymentMethod = radioButtonOnline.isChecked() ? "VNPAY" : "COD";
        showLoading(true);

        OrderRequest orderRequest = new OrderRequest(address, paymentMethod);
        ApiClient.createOrder(authHeader, orderRequest).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderResponse>> call, Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if ("COD".equals(paymentMethod)) {
                        showLoading(false);
                        Toast.makeText(CartActivity.this, "Đặt hàng COD thành công!", Toast.LENGTH_LONG).show();
                        fetchCartData();
                        finish();
                    } else {
                        int orderId = response.body().getData().getOrderId();
                        // Chuyển sang PaymentActivity và truyền orderId
                        Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                        intent.putExtra(PaymentActivity.EXTRA_ORDER_ID, orderId);
                        startActivity(intent);
                        finish();
                        // Không nên finish() ở đây ngay, để người dùng có thể quay lại
                    }
                } else {
                    showDataView(); // Hiển thị lại UI
                    String errorMessage = (response.body() != null && response.body().getMessage() != null) ? response.body().getMessage() : "Đặt hàng thất bại.";
                    Toast.makeText(CartActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderResponse>> call, Throwable t) {
                showDataView(); // Hiển thị lại UI
                Toast.makeText(CartActivity.this, "Lỗi kết nối khi tạo đơn hàng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm này không còn cần thiết vì đã chuyển logic sang PaymentActivity
    // private void fetchPaymentUrl(String authHeader, int orderId) { ... }

    private void showClearAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng?")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> performClearAllItems())
                .setNegativeButton("Hủy bỏ", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performClearAllItems() {
        showLoading(true);
        String token = hardcodedToken;
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
            showDataView();
            return;
        }
        String authHeader = "Bearer " + token;

        ApiClient.cartClearAllItems(authHeader).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CartActivity.this, "Đã dọn sạch giỏ hàng.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CartActivity.this, "Xóa giỏ hàng thất bại.", Toast.LENGTH_SHORT).show();
                }
                fetchCartData();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                fetchCartData();
            }
        });
    }

    // --- CÁC HÀM TIỆN ÍCH QUẢN LÝ GIAO DIỆN ---

    private void updateTotalPrice(double price) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textViewTotalPrice.setText(currencyFormatter.format(price));
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            recyclerViewCartItems.setVisibility(View.GONE);
            deliveryInfoCard.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            emptyCartView.setVisibility(View.GONE);
            // Có thể thêm một ProgressIndicator ở giữa màn hình nếu muốn
        }
    }

    private void showDataView() {
        emptyCartView.setVisibility(View.GONE);
        recyclerViewCartItems.setVisibility(View.VISIBLE);
        deliveryInfoCard.setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);
    }

    private void showEmptyView(String message) {
        recyclerViewCartItems.setVisibility(View.GONE);
        deliveryInfoCard.setVisibility(View.GONE);
        bottomBar.setVisibility(View.GONE);
        emptyCartView.setVisibility(View.VISIBLE);
        // Giả sử TextView trong empty_cart.xml có ID là textViewEmptyMessage
        TextView emptyMessageText = emptyCartView.findViewById(R.id.textViewEmptyMessage);
        if (emptyMessageText != null) {
            emptyMessageText.setText(message);
        }
    }
}
