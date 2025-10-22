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
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private WebView webViewPayment;
    private ProgressBar progressBar;

    private final String hardcodedToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJxdWFuIiwicm9sZSI6IlVTRVIiLCJleHAiOjE3NjEwNDQwODMsInVzZXJJZCI6OSwiaWF0IjoxNzYxMDQwNDgzfQ.xYWyu-1d9y8shkgahVACw1Z1DT7vE-SdsaufxnX4FR0";
    public static final String EXTRA_ORDER_ID = "ORDER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        setupWebView();
        setupClickListeners();
        setupOnBackPressed();

        int orderId = 6;

        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không có ID đơn hàng để thanh toán.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Bắt đầu quá trình lấy URL và tải nó
        String authHeader = "Bearer " + hardcodedToken;
        fetchPaymentUrlAndLoad(authHeader,orderId);
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
            // Trong PaymentActivity.java
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.startsWith("prm392://")) {
                    // Khi thanh toán xong, VNPay sẽ gọi về URL này.
                    // Chuyển sang một Activity khác để hiển thị kết quả.
                    Intent intent = new Intent(PaymentActivity.this, PaymentSuccessActivity.class);
                    intent.setData(request.getUrl());

                    // ====================================================================
                    // SỬA LỖI Ở ĐÂY
                    // ====================================================================
                    // Thêm cờ này để xóa tất cả các Activity nằm trên đầu của Activity mới
                    // trong cùng một task. Cụ thể ở đây nó sẽ xóa CartActivity.
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);

                    // Sau khi đã start Activity mới với cờ CLEAR_TOP,
                    // chúng ta cũng đóng luôn Activity hiện tại.
                    finish(); // Dòng này bây giờ đã đúng

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
     * @param orderId ID của đơn hàng cần thanh toán.
     */
    private void fetchPaymentUrlAndLoad(String authen,int orderId) {
        progressBar.setVisibility(View.VISIBLE);
        webViewPayment.setVisibility(View.GONE);

        PaymentApi paymentApi = ApiClient.getPaymentUrl();

        paymentApi.getPaymentUrl(authen,orderId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Lấy URL từ trong đối tượng "data"
                    String paymentUrl = response.body().getData();

                    // =================== ĐIỂM THAY ĐỔI QUAN TRỌNG ===================
                    // Ra lệnh cho WebView tải nội dung từ URL đã nhận được
                    webViewPayment.loadUrl(paymentUrl);
                    // =============================================================

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PaymentActivity.this, "Không thể lấy link thanh toán. Mã lỗi: " + response.code(), Toast.LENGTH_LONG).show();
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
                if (webViewPayment.canGoBack()) {
                    webViewPayment.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
