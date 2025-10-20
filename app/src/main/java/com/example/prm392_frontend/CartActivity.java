package com.example.prm392_frontend;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.api.CartApi;
import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.CartItemUpdateRequest;
import com.example.prm392_frontend.models.CartItemUpdateResponse;
import com.example.prm392_frontend.models.CartResponse;
import com.example.prm392_frontend.utils.AuthHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartAdapterListener {

    private MaterialToolbar topAppBar;
    private RecyclerView recyclerViewCartItems;
    private View bottomBar;
    private View deliveryInfoCard;
    private TextView textViewTotalPrice;
    private Button buttonPlaceOrder;
    private RadioButton radioButtonCOD, radioButtonOnline;
    private CheckBox checkboxUseSavedAddress;
    private TextInputEditText editTextAddress;
    private ProgressBar progressBar;
    private View emptyCartView;

    private CartAdapter cartAdapter;
    private AuthHelper authHelper;
    private CartApi cartApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Khởi tạo AuthHelper để làm việc với token
        authHelper = new AuthHelper(this);
        // Khởi tạo CartApi
        cartApi = ApiClient.getCartApi();


        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Bắt đầu quá trình lấy dữ liệu từ API
        fetchCartData();
    }

    /**
     * Ánh xạ tất cả các view từ layout XML vào biến Java.
     */
    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        bottomBar = findViewById(R.id.bottomBar);
        deliveryInfoCard = findViewById(R.id.deliveryInfoCard);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
        radioButtonCOD = findViewById(R.id.radioButtonCOD);
        radioButtonOnline = findViewById(R.id.radioButtonOnline);
        checkboxUseSavedAddress = findViewById(R.id.checkboxUseSavedAddress);
        editTextAddress = findViewById(R.id.editTextAddress);
        progressBar = findViewById(R.id.progressBar);
        emptyCartView = findViewById(R.id.emptyCartView);
    }

    /**
     * Cài đặt RecyclerView và Adapter để sẵn sàng hiển thị dữ liệu.
     */
    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, new ArrayList<>(), this);
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCartItems.setAdapter(cartAdapter);
    }

    /**
     * Thiết lập các sự kiện click cho các nút và view.
     */
    private void setupClickListeners() {
        // Nút "Back" trên thanh tiêu đề
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Nút "Đặt Hàng"
        buttonPlaceOrder.setOnClickListener(v -> handlePlaceOrder());

        // Xử lý khi chọn/bỏ chọn "Sử dụng địa chỉ đã lưu"
        checkboxUseSavedAddress.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextAddress.setEnabled(!isChecked);
            if (isChecked) {
                // TODO: Lấy và hiển thị địa chỉ đã lưu của người dùng từ SharedPreferences hoặc API
//                editTextAddress.setText(authHelper.getUserAddress()); // Ví dụ lấy từ AuthHelper
                editTextAddress.setText("address");
            } else {
                editTextAddress.setText("");
            }
        });
    }

    /**
     * Hàm chính để lấy dữ liệu giỏ hàng từ API.
     */
    private void fetchCartData() {
        showLoading(true);
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJxdWFuIiwicm9sZSI6IlVTRVIiLCJleHAiOjE3NjA4OTU0MTUsInVzZXJJZCI6OSwiaWF0IjoxNzYwODkxODE1fQ.Rm8KB1hY_f9eHaEjE-0yWK95jK8CFeNJDOtv5S1OP2s";
//        String token = authHelper.getToken();
        if (token == null || token.isEmpty()){
            showEmptyView("Vui lòng đăng nhập để xem giỏ hàng.");
            return;
        }

        String authHeader = "Bearer " + token;

        cartApi.getCurrentUserCart(authHeader).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartResponse>> call, Response<ApiResponse<CartResponse>> response) {
                showLoading(false);
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
                    showEmptyView("Không thể tải giỏ hàng. Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CartResponse>> call, Throwable t) {
                showLoading(false);
                showEmptyView("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    // ====================================================================
    // Sửa đổi 4: Implement các phương thức từ CartAdapter.CartAdapterListener
    // ====================================================================

    @Override
    public void onUpdateQuantity(int cartItemId, int newQuantity) {
        showLoading(true); // Hiển thị loading khi bắt đầu gọi API

        String token = authHelper.getToken();
        if (token == null || token.isEmpty()){
            Toast.makeText(this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }
        String authHeader = "Bearer " + token;

        CartItemUpdateRequest request = new CartItemUpdateRequest(newQuantity);

        cartApi.cartUpdateQuantity(cartItemId, request).enqueue(new Callback<ApiResponse<CartItemUpdateResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartItemUpdateResponse>> call, Response<ApiResponse<CartItemUpdateResponse>> response) {
                // API đã phản hồi, dù thành công hay thất bại, ta cũng sẽ fetch lại toàn bộ giỏ hàng
                // để đảm bảo dữ liệu (tổng tiền, số lượng) là mới nhất từ server.
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CartActivity.this, "Cập nhật giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CartActivity.this, "Cập nhật thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
                // Luôn gọi lại fetchCartData để làm mới toàn bộ giao diện
                fetchCartData();
            }

            @Override
            public void onFailure(Call<ApiResponse<CartItemUpdateResponse>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Dù lỗi cũng nên fetch lại để giao diện quay về trạng thái đúng từ server
                fetchCartData();
            }
        });
    }

    @Override
    public void onDeleteItem(int cartItemId) {
        // TODO: Viết logic gọi API xóa sản phẩm khỏi giỏ hàng ở đây
        // Sau khi gọi API xóa thành công, cũng gọi lại fetchCartData() để làm mới.
        Toast.makeText(this, "Chức năng xóa đang được phát triển", Toast.LENGTH_SHORT).show();
    }


    /**
     * Xử lý logic khi nhấn nút "Đặt Hàng"
     */
    private void handlePlaceOrder() {
        if (cartAdapter.getItemCount() == 0) {
            Toast.makeText(this, "Giỏ hàng trống, không thể đặt hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = editTextAddress.getText().toString().trim();
        if (!checkboxUseSavedAddress.isChecked() && address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (radioButtonOnline.isChecked()) {
            Toast.makeText(this, "Chức năng thanh toán online đang được phát triển", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đặt hàng COD thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cập nhật và định dạng lại tổng tiền.
     */
    private void updateTotalPrice(double price) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textViewTotalPrice.setText(currencyFormatter.format(price));
    }

    // --- Các hàm quản lý trạng thái hiển thị của giao diện ---

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (isLoading) {
            recyclerViewCartItems.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            deliveryInfoCard.setVisibility(View.GONE);
            if (emptyCartView != null) emptyCartView.setVisibility(View.GONE);
        }
    }

    private void showDataView() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (emptyCartView != null) emptyCartView.setVisibility(View.GONE);

        recyclerViewCartItems.setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);
        deliveryInfoCard.setVisibility(View.VISIBLE);
    }

    private void showEmptyView(String message) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        recyclerViewCartItems.setVisibility(View.GONE);
        bottomBar.setVisibility(View.GONE);
        deliveryInfoCard.setVisibility(View.GONE);

        if (emptyCartView != null) {
            emptyCartView.setVisibility(View.VISIBLE);
            if (emptyCartView instanceof TextView) {
                ((TextView) emptyCartView).setText(message);
            }
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}
