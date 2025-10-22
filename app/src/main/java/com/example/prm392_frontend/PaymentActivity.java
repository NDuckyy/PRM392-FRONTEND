package com.example.prm392_frontend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.api.PaymentApi;
import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.utils.AuthHelper;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private WebView webViewPayment;
    private ProgressBar progressBar;

    private AuthHelper authHelper;
    public static final String EXTRA_ORDER_ID = "ORDER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        setupWebView();
        setupClickListeners();
        setupOnBackPressed();

        // ====================================================================
        // SỬA 1: Khởi tạo authHelper để có thể lấy token
        // ====================================================================
        authHelper = new AuthHelper(this);

        // ====================================================================
        // SỬA 2: Lấy orderId được truyền từ CartActivity, không gán cứng
        // ====================================================================
        int orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);

        // Kiểm tra xem có nhận được orderId hợp lệ không
        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không có ID đơn hàng để thanh toán.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Bắt đầu quá trình lấy URL và tải nó
        String token = authHelper.getToken();

        // ====================================================================
        // SỬA 3: Kiểm tra token trước khi gọi API
        // ====================================================================
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập hoặc phiên đã hết hạn.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String authHeader = "Bearer " + token;
        fetchPaymentUrlAndLoad(authHeader, orderId);
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        webViewPayment = findViewById(R.id.webViewPayment);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupWebView() {
        // Bật JavaScript, rất quan trọng cho các trang thanh toán
        webViewPayment.getSettings().setJavaScriptEnabled(true);
        // Bật DOM Storage để lưu trữ dữ liệu tạm thời (một số trang yêu cầu)
        webViewPayment.getSettings().setDomStorageEnabled(true);

        webViewPayment.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                webViewPayment.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                webViewPayment.setVisibility(View.VISIBLE);
            }

            // Hàm này cực kỳ quan trọng để bắt Deep Link khi thanh toán xong
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.startsWith("prm392://")) {
                    // Khi thanh toán xong, VNPay sẽ gọi về URL này.
                    // Chuyển sang một Activity khác để hiển thị kết quả.
                    Intent intent = new Intent(PaymentActivity.this, PaymentSuccessActivity.class);
                    intent.setData(request.getUrl());

                    // Thêm cờ này để xóa các Activity trung gian (như CartActivity)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);

                    // Đóng Activity hiện tại sau khi đã chuyển hướng
                    finish();

                    return true; // Đã xử lý URL
                }
                return false; // URL bình thường, để WebView tự xử lý
            }

        });
    }

    private void setupClickListeners() {
        topAppBar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Gọi API để lấy URL thanh toán từ backend và sau đó tải nó lên WebView.
     * @param authen  Chuỗi token xác thực (ví dụ: "Bearer ...")
     * @param orderId ID của đơn hàng cần thanh toán.
     */
    private void fetchPaymentUrlAndLoad(String authen, int orderId) {
        progressBar.setVisibility(View.VISIBLE);
        webViewPayment.setVisibility(View.GONE);

        PaymentApi paymentApi = ApiClient.getPaymentUrl();

        paymentApi.getPaymentUrl(authen, orderId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    // Lấy URL từ trong đối tượng "data"
                    String paymentUrl = response.body().getData();
                    // Ra lệnh cho WebView tải nội dung từ URL đã nhận được
                    webViewPayment.loadUrl(paymentUrl);
                } else {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Không thể lấy link thanh toán. Mã lỗi: " + response.code();
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Toast.makeText(PaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    // Đóng activity sau một khoảng thời gian ngắn để người dùng đọc thông báo
                    new android.os.Handler().postDelayed(PaymentActivity.this::finish, 2000);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PaymentActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                new android.os.Handler().postDelayed(PaymentActivity.this::finish, 2000);
            }
        });
    }

    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Nếu WebView có thể quay lại trang trước đó (trong lịch sử của WebView)
                if (webViewPayment.canGoBack()) {
                    webViewPayment.goBack();
                } else {
                    // Nếu không, thực hiện hành vi quay lại mặc định (đóng Activity)
                    // Tắt callback này để tránh vòng lặp vô hạn và gọi lại onBackPressed
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
