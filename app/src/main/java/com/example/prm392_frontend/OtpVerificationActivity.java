package com.example.prm392_frontend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.models.AuthResponse;
import com.example.prm392_frontend.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {

    private static final String OTP_CHANNEL_ID = "otp_channel";
    private static final int OTP_NOTIFICATION_ID = 1002;
    private EditText etOtp;
    private Button btnVerifyOtp;
    private ProgressBar progressBar;

    private TextView tvResendCode;

    private String mockOtp;
    private RegisterRequest registerRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);
        etOtp = findViewById(R.id.et_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        progressBar = findViewById(R.id.progress_bar);
        tvResendCode = findViewById(R.id.tv_resend_code);
        mockOtp = getIntent().getStringExtra("MOCK_OTP");
        registerRequest = (RegisterRequest) getIntent().getSerializableExtra("REGISTER_REQUEST");
        if (mockOtp == null || registerRequest == null) {
            Toast.makeText(this, "Lỗi: Không nhận được dữ liệu đăng ký.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvResendCode.setOnClickListener(v -> resendCode());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
    }


    private void resendCode() {
        java.util.Random random = new java.util.Random();
        int otpNumber = 100000 + random.nextInt(900000);
        mockOtp = String.valueOf(otpNumber);
        Toast.makeText(this, "Mã OTP mới của bạn là: " + mockOtp, Toast.LENGTH_LONG).show();
        sendOtpNotification(mockOtp);
        etOtp.setText("");
    }


    private void verifyOtp() {
        String enteredOtp = etOtp.getText().toString().trim();
        if (enteredOtp.equals(mockOtp)) {
            finalizeRegistration();
        } else {
            Toast.makeText(this, "Mã OTP không chính xác!", Toast.LENGTH_SHORT).show();
        }
    }


    private void finalizeRegistration() {
        setLoading(true);
        ApiClient.getAuthApi().register(registerRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    Toast.makeText(OtpVerificationActivity.this,
                            "Đăng ký thành công! Vui lòng đăng nhập.",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    String errorMsg = "Đăng ký thất bại";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg += ": " + response.body().getMessage();
                    } else if (response.code() == 400) {
                        errorMsg = "Username hoặc email đã tồn tại";
                    }
                    Toast.makeText(OtpVerificationActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(OtpVerificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnVerifyOtp.setEnabled(!isLoading);
        etOtp.setEnabled(!isLoading);
    }


    private void sendOtpNotification(String otp) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, OTP_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Mã xác thực mới của bạn")
                .setContentText("Mã OTP là: " + otp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(OTP_NOTIFICATION_ID, builder.build());
    }

}
