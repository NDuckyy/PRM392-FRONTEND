package com.example.prm392_frontend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log; // Thêm import Log
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

    private static final String TAG = "PaymentActivity"; // Thêm TAG để log
    private MaterialToolbar topAppBar;
    private WebView webViewPayment;
    private ProgressBar progressBar;
    private AuthHelper authHelper;
    private int currentOrderId; // Biến để lưu orderId hiện tại

    public static final String EXTRA_ORDER_ID = "ORDER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        setupWebView();
        setupClickListeners();
        setupOnBackPressed();

        authHelper = new AuthHelper(this);
        // Lấy và lưu orderId vào biến của class
        currentOrderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);

        if (currentOrderId == -1) {
            Toast.makeText(this, "Lỗi: Không có ID đơn hàng để thanh toán.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String token = authHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập hoặc phiên đã hết hạn.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String authHeader = "Bearer " + token;
        fetchPaymentUrlAndLoad(authHeader, currentOrderId);
    }

    //... (Các phương thức initViews, setupClickListeners, setupOnBackPressed không đổi)

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        webViewPayment = findViewById(R.id.webViewPayment);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
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

    private void setupWebView() {
        webViewPayment.getSettings().setJavaScriptEnabled(true);
        webViewPayment.getSettings().setDomStorageEnabled(true);
        webViewPayment.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

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

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString();

                if (url.startsWith("prm392://payment/result")) {
                    String responseCode = uri.getQueryParameter("vnp_ResponseCode");

                    if ("00".equals(responseCode)) {
                        // Giao dịch thành công, gọi API cập nhật trạng thái đơn hàng
                        Toast.makeText(PaymentActivity.this, "Thanh toán thành công! Đang cập nhật đơn hàng...", Toast.LENGTH_SHORT).show();
                        updateOrderStatusOnServer(uri);
                    } else {
                        // Giao dịch thất bại
                        String message = getVnpayErrorMessage(responseCode);
                        Toast.makeText(PaymentActivity.this, "Giao dịch thất bại: " + message, Toast.LENGTH_LONG).show();
                        finish(); // Đóng Activity và quay về giỏ hàng
                    }

                    return true;
                }

                return false;
            }
        });
    }

    /**
     * Phương thức mới: Gọi API để cập nhật trạng thái thanh toán của đơn hàng trên server.
     * @param returnUri Uri trả về từ VNPAY, chứa các thông tin giao dịch.
     */
    private void updateOrderStatusOnServer(Uri returnUri) {
        String token = authHelper.getToken();
        if (token == null) {
            Toast.makeText(this, "Lỗi: Phiên đăng nhập hết hạn. Không thể cập nhật đơn hàng.", Toast.LENGTH_LONG).show();
            // Dù lỗi, vẫn nên chuyển sang trang thành công để người dùng biết họ đã trả tiền.
            // Backend sẽ phải có cơ chế xử lý khác (ví dụ: IPN của VNPAY).
            navigateToSuccessScreen(returnUri);
            return;
        }

        String authHeader = "Bearer " + token;

        // Gọi API từ ApiClient
        ApiClient.updateOrderPaymentStatus(authHeader, currentOrderId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Cập nhật trạng thái đơn hàng thành công trên server.");
                } else {
                    // Log lỗi nếu không cập nhật được, nhưng không chặn người dùng.
                    Log.e(TAG, "Lỗi khi cập nhật trạng thái đơn hàng. Mã lỗi: " + response.code());
                    Toast.makeText(PaymentActivity.this, "Lưu ý: Có lỗi khi cập nhật trạng thái đơn hàng.", Toast.LENGTH_SHORT).show();
                }
                // Dù thành công hay thất bại, vẫn chuyển người dùng đến màn hình thành công.
                navigateToSuccessScreen(returnUri);
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                // Log lỗi kết nối, nhưng không chặn người dùng.
                Log.e(TAG, "Lỗi kết nối khi cập nhật trạng thái đơn hàng.", t);
                Toast.makeText(PaymentActivity.this, "Lưu ý: Có lỗi kết nối khi cập nhật đơn hàng.", Toast.LENGTH_SHORT).show();
                // Dù thành công hay thất bại, vẫn chuyển người dùng đến màn hình thành công.
                navigateToSuccessScreen(returnUri);
            }
        });
    }

    /**
     * Phương thức mới: Chuyển hướng đến màn hình PaymentSuccessActivity.
     * @param data Uri để truyền sang cho màn hình thành công.
     */
    private void navigateToSuccessScreen(Uri data) {
        Intent intent = new Intent(PaymentActivity.this, PaymentSuccessActivity.class);
        intent.setData(data); // Truyền dữ liệu sang để hiển thị chi tiết nếu cần
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Đóng PaymentActivity
    }

    private String getVnpayErrorMessage(String responseCode) {
        switch (responseCode != null ? responseCode : "") {
            case "07": return "Trừ tiền thành công nhưng giao dịch bị nghi ngờ gian lận.";
            case "09": return "Thẻ/Tài khoản chưa đăng ký dịch vụ Internet Banking.";
            case "10": return "Xác thực không thành công.";
            case "11": return "Giao dịch đã hết hạn.";
            case "12": return "Thẻ/Tài khoản bị khóa.";
            case "13": return "Nhập sai OTP.";
            case "24": return "Hủy giao dịch.";
            case "51": return "Tài khoản không đủ số dư.";
            case "65": return "Tài khoản đã vượt quá hạn mức giao dịch trong ngày.";
            default: return "Giao dịch không thành công. Mã lỗi: " + responseCode;
        }
    }

    private void fetchPaymentUrlAndLoad(String authen, int orderId) {
        progressBar.setVisibility(View.VISIBLE);
        webViewPayment.setVisibility(View.GONE);

        PaymentApi paymentApi = ApiClient.getPaymentUrl();
        paymentApi.getPaymentUrl(authen, orderId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    String paymentUrl = response.body().getData();
                    webViewPayment.loadUrl(paymentUrl);
                } else {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Không thể lấy link thanh toán. Mã lỗi: " + response.code();
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Toast.makeText(PaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
}
