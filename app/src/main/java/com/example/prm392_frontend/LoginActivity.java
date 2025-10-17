package com.example.prm392_frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.databinding.ActivityLoginBinding;
import com.example.prm392_frontend.models.AuthResponse;
import com.example.prm392_frontend.models.LoginRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> handleLogin());

        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            binding.tilUsername.setError("Username is required");
            return;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            return;
        }

        // Clear errors
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);

        // Show progress
        setLoading(true);

        // API call
        LoginRequest request = new LoginRequest(username, password);
        ApiClient.getAuthApi().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    // Save authentication data
                    saveAuthData(authResponse);

                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    String errorMsg = "Login failed: ";
                    if (response.code() == 401) {
                        errorMsg += "Invalid username or password";
                    } else if (response.code() == 404) {
                        errorMsg += "User not found";
                    } else {
                        errorMsg += "Error code " + response.code();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAuthData(AuthResponse response) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", response.getToken());
        editor.putString("username", response.getUsername());
        editor.putString("role", response.getRole());
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean("is_logged_in", false);
    }

    private void navigateToMain() {
        // Check if we came from ProductDetailsActivity
        boolean returnToProduct = getIntent().getBooleanExtra("return_to_product", false);

        if (returnToProduct) {
            // Return to ProductDetailsActivity with success result
            setResult(RESULT_OK);
            finish();
        } else {
            // Normal login flow - just finish and return to previous activity (ProductListActivity)
            // Since ProductListActivity is the LAUNCHER, it will be in the back stack
            finish();
        }
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.etUsername.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.tvRegister.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
