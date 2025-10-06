package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.databinding.ActivityRegisterBinding;
import com.example.prm392_frontend.models.AuthResponse;
import com.example.prm392_frontend.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRoleDropdown();
        setupListeners();
    }

    private void setupRoleDropdown() {
        String[] roles = {"CUSTOMER", "SELLER", "ADMIN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                roles
        );
        binding.actvRole.setAdapter(adapter);
        binding.actvRole.setText("CUSTOMER", false);
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> handleRegister());

        binding.tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login screen
        });
    }

    private void handleRegister() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String role = binding.actvRole.getText().toString().trim();

        // Validation
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

        // Clear errors
        binding.tilUsername.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilPhoneNumber.setError(null);
        binding.tilAddress.setError(null);

        // Show progress
        setLoading(true);

        // API call
        RegisterRequest request = new RegisterRequest(username, email, password, phoneNumber, address, role);
        ApiClient.getAuthApi().register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Toast.makeText(RegisterActivity.this,
                            "Registration successful! Please login.",
                            Toast.LENGTH_LONG).show();

                    // Navigate back to login
                    finish();
                } else {
                    String errorMsg = "Registration failed";
                    if (response.code() == 400) {
                        errorMsg = "Username or email already exists";
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
        binding.etUsername.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.etPhoneNumber.setEnabled(!isLoading);
        binding.etAddress.setEnabled(!isLoading);
        binding.actvRole.setEnabled(!isLoading);
        binding.tvLogin.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
