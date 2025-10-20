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
    public static final String EXTRA_ORDER_ID = "ORDER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        setupWebView();
        setupClickListeners();
        setupOnBackPressed();

        int orderId = 2;

        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không có ID đơn hàng để thanh toán.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Bắt đầu quá trình lấy URL và tải nó
        fetchPaymentUrlAndLoad(orderId);
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

                // Kiểm tra xem URL có phải là Deep Link trả về của chúng ta không
                // Ví dụ: prm392://payment/result?vnp_ResponseCode=00...
                if (url.startsWith("prm392://")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                    startActivity(intent);
                    finish(); // Đóng PaymentActivity hiện tại
                    return true; // Báo cho WebView biết chúng ta đã xử lý URL này
                }
                // Đối với các URL khác (bên trong trang VNPay), để WebView tự xử lý
                return false;
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
    private void fetchPaymentUrlAndLoad(int orderId) {
        progressBar.setVisibility(View.VISIBLE);
        webViewPayment.setVisibility(View.GONE);

        PaymentApi paymentApi = ApiClient.getPaymentUrl();

        paymentApi.getPaymentUrl(orderId).enqueue(new Callback<ApiResponse<String>>() {
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
