package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.example.prm392_frontend.models.CartItemResponse;
import com.example.prm392_frontend.models.CartItemUpdateRequest;
import com.example.prm392_frontend.models.CartItemUpdateResponse;
import com.example.prm392_frontend.models.CartResponse;
import com.example.prm392_frontend.models.OrderRequest;
import com.example.prm392_frontend.models.OrderResponse;
import com.example.prm392_frontend.models.ProductResponse;
import com.example.prm392_frontend.models.User;
import com.example.prm392_frontend.utils.AuthHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartAdapterListener {

    private MaterialToolbar topAppBar;
    private RecyclerView recyclerViewCartItems;
    private LinearLayout emptyCartView;
    private TextView textViewTotalPrice;
    private EditText editTextAddress;
    private RadioButton radioButtonOnline;
    private RadioButton radioButtonCOD;
    private Button buttonPlaceOrder;
    private MaterialCardView bottomBar;

    private CartAdapter cartAdapter;
    private AuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        initViews();
        initComponents();
        setupRecyclerView();
        setupClickListeners();
        loadUserData();
        fetchCartData();
    }

    private void loadUserData() {
        String token = authHelper.getToken();
        if (token == null || token.isEmpty()) {
            return;
        }
        String authHeader = "Bearer " + token;

        ApiClient.getUserApi().getUserProfile(authHeader).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User userProfile = response.body().getData();

                    if (userProfile != null && userProfile.getAddress() != null && !userProfile.getAddress().isEmpty()) {
                        runOnUiThread(() -> {
                            editTextAddress.setText(userProfile.getAddress());
                        });
                    }
                } else {
                    android.util.Log.e("CartActivity", "Không thể tải thông tin người dùng. Lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                android.util.Log.e("CartActivity", "Lỗi mạng khi tải thông tin người dùng: " + t.getMessage());
            }
        });
    }


    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        emptyCartView = findViewById(R.id.emptyCartView);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        editTextAddress = findViewById(R.id.editTextAddress);
        radioButtonOnline = findViewById(R.id.radioButtonOnline);
        radioButtonCOD = findViewById(R.id.radioButtonCOD);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
        bottomBar = findViewById(R.id.bottomBar);
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Gọi fetchCartData() mỗi khi activity quay trở lại
        // Điều này đảm bảo dữ liệu giỏ hàng luôn được làm mới
        fetchCartData();
    }

    private void fetchCartData() {
        showLoading(true);
        String token = authHelper.getToken();
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

    /**
     * Phương thức này đã được cập nhật để gọi API lấy chi tiết sản phẩm,
     * sau đó mới kiểm tra stockQuantity.
     * @param cartItemId ID của mục trong giỏ hàng.
     * @param newQuantity Số lượng mới mà người dùng muốn cập nhật.
     */
    @Override
    public void onUpdateQuantity(int cartItemId, int newQuantity) {
        // Bước 1: Tìm sản phẩm trong adapter để lấy Product ID
        CartItemResponse itemToUpdate = cartAdapter.findItemById(cartItemId);

        if (itemToUpdate == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        int productId = itemToUpdate.getProductId();

        // Bước 2: Gọi API để lấy thông tin chi tiết của sản phẩm (bao gồm cả stockQuantity)
        showLoading(true);
        ApiClient.getProductById(productId).enqueue(new Callback<ApiResponse<ProductResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductResponse>> call, Response<ApiResponse<ProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ProductResponse productDetails = response.body().getData();
                    int stockQuantity = productDetails.getStockQuantity();

                    // Bước 3: So sánh số lượng muốn mua với số lượng trong kho
                    if (newQuantity > stockQuantity) {
                        // Nếu vượt quá, hiển thị lỗi và không cập nhật
                        showErrorToast(stockQuantity);
                        // Hoàn tác lại số lượng trên giao diện
                        cartAdapter.revertItemQuantity(cartItemId);
                        showDataView(); // Ẩn loading
                    } else {
                        // Nếu hợp lệ, gọi API để cập nhật giỏ hàng
                        performCartUpdate(cartItemId, newQuantity, stockQuantity);
                    }
                } else {
                    // Xử lý lỗi khi không lấy được thông tin sản phẩm
                    Toast.makeText(CartActivity.this, "Không thể kiểm tra tồn kho. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    cartAdapter.revertItemQuantity(cartItemId);
                    showDataView();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductResponse>> call, Throwable t) {
                // Xử lý lỗi kết nối mạng
                Toast.makeText(CartActivity.this, "Lỗi kết nối khi kiểm tra tồn kho.", Toast.LENGTH_SHORT).show();
                cartAdapter.revertItemQuantity(cartItemId);
                showDataView();
            }
        });
    }

    /**
     * Phương thức mới: Thực hiện việc gọi API để cập nhật giỏ hàng sau khi đã kiểm tra tồn kho.
     * @param cartItemId ID của mục trong giỏ hàng
     * @param newQuantity Số lượng mới hợp lệ
     * @param stockQuantity Số lượng tồn kho (để hiển thị lỗi nếu API cập nhật thất bại)
     */
    private void performCartUpdate(int cartItemId, int newQuantity, int stockQuantity) {
        String token = authHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
            showDataView();
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
                    // Nếu API cập nhật thất bại (có thể do người khác vừa mua hết hàng)
                    showErrorToast(stockQuantity);
                }
                // Luôn tải lại toàn bộ giỏ hàng để đồng bộ trạng thái mới nhất từ server
                fetchCartData();
            }

            @Override
            public void onFailure(Call<ApiResponse<CartItemUpdateResponse>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                fetchCartData(); // Tải lại để quay về trạng thái cũ
            }
        });
    }


    /**
     * Phương thức mới: Hiển thị một Toast tùy chỉnh với thông báo lỗi và số lượng tồn kho.
     * @param stockAvailable Số lượng sản phẩm thực tế còn trong kho.
     */
    private void showErrorToast(int stockAvailable) {
        // Inflate layout tùy chỉnh từ file activity_error_cart.xml
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.activity_error_cart, null);

        // Tìm các TextView trong layout đã inflate
        TextView textErrorMessage = layout.findViewById(R.id.textViewErrorMessage);
        TextView textStockInfo = layout.findViewById(R.id.textViewStockInfo);

        // Thiết lập nội dung cho các TextView
        textErrorMessage.setText("Số lượng vượt quá giới hạn!");
        textStockInfo.setText("Chỉ còn " + stockAvailable + " sản phẩm trong kho.");

        // Tạo và hiển thị Toast
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0); // Hiển thị ở giữa màn hình
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
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
        String token = authHelper.getToken();
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
        String token = authHelper.getToken();
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
                        // Trong handlePlaceOrder() của CartActivity.java
                    } else {
                        int orderId = response.body().getData().getOrderId();
                        // Chuyển sang PaymentActivity và truyền orderId
                        Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                        intent.putExtra(PaymentActivity.EXTRA_ORDER_ID, orderId);
                        startActivity(intent);

                        // BỎ DÒNG finish() Ở ĐÂY.
                        // Việc đóng CartActivity sẽ được xử lý bởi PaymentActivity
                        // khi thanh toán thành công, như vậy sẽ cho phép người dùng
                        // nhấn "Back" từ màn hình thanh toán để quay lại giỏ hàng nếu muốn.
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
        String token = authHelper.getToken();
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
            bottomBar.setVisibility(View.GONE);
            emptyCartView.setVisibility(View.GONE);
            // Có thể thêm một ProgressIndicator ở giữa màn hình nếu muốn
        }
    }

    private void showDataView() {
        emptyCartView.setVisibility(View.GONE);
        recyclerViewCartItems.setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);
    }

    private void showEmptyView(String message) {
        recyclerViewCartItems.setVisibility(View.GONE);
        bottomBar.setVisibility(View.GONE);
        emptyCartView.setVisibility(View.VISIBLE);
        // Giả sử TextView trong empty_cart.xml có ID là textViewEmptyMessage
        TextView emptyMessageText = emptyCartView.findViewById(R.id.textViewEmptyMessage);
        if (emptyMessageText != null) {
            emptyMessageText.setText(message);
        }
    }
}
