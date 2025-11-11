package com.example.prm392_frontend;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.prm392_frontend.databinding.ActivityRegisterBinding;
import com.example.prm392_frontend.models.RegisterRequest;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;

    private static final String OTP_CHANNEL_ID = "otp_channel";
    private static final int OTP_NOTIFICATION_ID = 1002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        createOtpNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }


    private void createOtpNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Mã Xác Thực OTP";
            String description = "Kênh dùng để gửi mã OTP xác thực tài khoản";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(OTP_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendOtpNotification(String otp) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, OTP_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Mã xác thực của bạn")
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


    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> handleRegister());

        binding.tvLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void handleRegister() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String role = "CUSTOMER";

        if (username.isEmpty()) {
            binding.tilUsername.setError("Username is required");
            return;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Valid email is required");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (phoneNumber.isEmpty()) {
            binding.tilPhoneNumber.setError("Phone number is required");
            return;
        }
        if (address.isEmpty()) {
            binding.tilAddress.setError("Address is required");
            return;
        }

        binding.tilUsername.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilPhoneNumber.setError(null);
        binding.tilAddress.setError(null);

        setLoading(true);

        RegisterRequest request = new RegisterRequest(username, email, password, phoneNumber, address, role);

        java.util.Random random = new java.util.Random();
        int otpNumber = 100000 + random.nextInt(900000);
        String mockOtp = String.valueOf(otpNumber);
        sendOtpNotification(mockOtp);
        Toast.makeText(this, "Mã OTP: " + mockOtp, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("MOCK_OTP", mockOtp);
        intent.putExtra("REGISTER_REQUEST", request);
        startActivity(intent);

//        ApiClient.getAuthApi().register(request).enqueue(new Callback<AuthResponse>() {
//            @Override
//            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
//                setLoading(false);
//
//                if (response.isSuccessful() && response.body() != null) {
//                    AuthResponse authResponse = response.body();
//                    Toast.makeText(RegisterActivity.this,
//                            "Registration successful! Please login.",
//                            Toast.LENGTH_LONG).show();
//
//                    // Navigate back to login
//                    finish();
//                } else {
//                    String errorMsg = "Registration failed";
//                    if (response.code() == 400) {
//                        errorMsg = "Username or email already exists";
//                    }
//                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<AuthResponse> call, Throwable t) {
//                setLoading(false);
//                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
        binding.etUsername.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.etPhoneNumber.setEnabled(!isLoading);
        binding.etAddress.setEnabled(!isLoading);
        binding.tvLogin.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
