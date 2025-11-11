package com.example.prm392_frontend;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log; // Thêm import Log
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.api.PaymentApi;
import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.utils.AuthHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private static final String PAYMENT_CHANNEL_ID = "payment_notifications";
    private static final int PAYMENT_NOTIFICATION_ID = 1234;
    private Uri lastPaymentUri = null;
    private TextToSpeech tts;
    private static final String TAG = "PaymentActivity";
    private MaterialToolbar topAppBar;
    private WebView webViewPayment;
    private ProgressBar progressBar;
    private AuthHelper authHelper;
    private int currentOrderId;

    public static final String EXTRA_ORDER_ID = "ORDER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        setupWebView();
        setupClickListeners();
        setupOnBackPressed();
        initializeTTS();
        createPaymentNotificationChannel();

        authHelper = new AuthHelper(this);
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

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

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
        webViewPayment.getSettings().setSupportMultipleWindows(true);
        webViewPayment.getSettings().setAllowFileAccess(true);
        webViewPayment.getSettings().setUseWideViewPort(true);
        webViewPayment.getSettings().setLoadWithOverviewMode(true);
        webViewPayment.getSettings().setSupportZoom(true);
        webViewPayment.getSettings().setBuiltInZoomControls(true);
        webViewPayment.getSettings().setDisplayZoomControls(false);
        webViewPayment.setWebChromeClient(new android.webkit.WebChromeClient());
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
                    lastPaymentUri = uri;
                    String responseCode = uri.getQueryParameter("vnp_ResponseCode");

                    if ("00".equals(responseCode)) {
                        String amountString = uri.getQueryParameter("vnp_Amount");
                        long amountValue = 0;
                        if (amountString != null) {
                            try {
                                amountValue = Long.parseLong(amountString) / 100;
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Không thể parse số tiền từ VNPAY: " + amountString);
                            }
                        }
                        Toast.makeText(PaymentActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();

                        speak("Thanh toán thành công " + amountValue + " đồng", "PAYMENT_SUCCESS");

                        showPaymentNotification("Giao dịch thành công","Tài khoản bị trừ "+ amountValue +" đồng");

                    } else {
                        String message = getVnpayErrorMessage(responseCode);
                        speak(message, "PAYMENT_FAIL");
                        Toast.makeText(PaymentActivity.this, "Giao dịch thất bại: " + message, Toast.LENGTH_LONG).show();
                        finish();
                    }
                    return true;
                }

                return false;
            }

        });
    }
    private void initializeTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale vietnamese = new Locale("vi", "VN");
                int result = tts.setLanguage(vietnamese);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Ngôn ngữ Tiếng Việt không được hỗ trợ.");
                } else {
                    Log.i("TTS", "TextToSpeech đã sẵn sàng!");
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            runOnUiThread(() -> {
                                if ("PAYMENT_SUCCESS".equals(utteranceId)) {
                                    if (lastPaymentUri != null) {
                                        updateOrderStatusOnServer(lastPaymentUri);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(String utteranceId) {
                            // Lỗi khi nói
                        }
                    });
                }
            } else {
                Log.e("TTS", "Khởi tạo TextToSpeech thất bại!");
            }
        });
    }


    private void speak(String textToSpeak, String utteranceId) {
        if (tts != null && tts.getEngines().size() > 0) {
            Bundle params = new Bundle();
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        }
    }


    /**
     * Phương thức mới: Gọi API để cập nhật trạng thái thanh toán của đơn hàng trên server.
     * @param returnUri Uri trả về từ VNPAY, chứa các thông tin giao dịch.
     */
    private void updateOrderStatusOnServer(Uri returnUri) {
        String token = authHelper.getToken();
        if (token == null) {
            Toast.makeText(this, "Lỗi: Phiên đăng nhập hết hạn. Không thể cập nhật đơn hàng.", Toast.LENGTH_LONG).show();
            navigateToSuccessScreen(returnUri);
            return;
        }

        String authHeader = "Bearer " + token;

        ApiClient.updateOrderPaymentStatus(authHeader, currentOrderId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Cập nhật trạng thái đơn hàng thành công trên server.");
                } else {
                    Log.e(TAG, "Lỗi khi cập nhật trạng thái đơn hàng. Mã lỗi: " + response.code());
                    Toast.makeText(PaymentActivity.this, "Lưu ý: Có lỗi khi cập nhật trạng thái đơn hàng.", Toast.LENGTH_SHORT).show();
                }
                navigateToSuccessScreen(returnUri);
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối khi cập nhật trạng thái đơn hàng.", t);
                Toast.makeText(PaymentActivity.this, "Lưu ý: Có lỗi kết nối khi cập nhật đơn hàng.", Toast.LENGTH_SHORT).show();
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
        intent.setData(data);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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

    private void createPaymentNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Thông báo Thanh toán";
            String description = "Hiển thị thông báo kết quả giao dịch";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(PAYMENT_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void showPaymentNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, PAYMENT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify(PAYMENT_NOTIFICATION_ID, builder.build());
    }

}
