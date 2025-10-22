package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.api.ProviderApi;
import com.example.prm392_frontend.databinding.ActivityBecomeProviderBinding;
import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.RegisterProviderRequest;
import com.example.prm392_frontend.utils.AuthHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BecomeProviderActivity extends AppCompatActivity {
    private static final String TAG = "BecomeProviderActivity";
    private ActivityBecomeProviderBinding binding;
    private AuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBecomeProviderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authHelper = new AuthHelper(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnRegisterProvider.setOnClickListener(v -> handleProviderRegistration());
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private void handleProviderRegistration() {
        String providerName = binding.etProviderName.getText().toString().trim();

        // Validation
        if (providerName.isEmpty()) {
            binding.tilProviderName.setError("Store/Business name is required");
            return;
        }

        if (providerName.length() < 3) {
            binding.tilProviderName.setError("Name must be at least 3 characters");
            return;
        }

        // Clear error
        binding.tilProviderName.setError(null);

        // Show progress
        setLoading(true);

        // Create request
        RegisterProviderRequest request = new RegisterProviderRequest(providerName);

        // Get ProviderApi instance with authentication
        ProviderApi providerApi = ApiClient.getAuthenticatedClient(this).create(ProviderApi.class);

        // API call
        providerApi.registerProvider(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                setLoading(false);

                Log.d(TAG, "Provider registration response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    Log.d(TAG, "API Response code: " + apiResponse.getCode() + ", message: " + apiResponse.getMessage());

                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(BecomeProviderActivity.this,
                            "Successfully registered as provider! Please login again.",
                            Toast.LENGTH_LONG).show();

                        Log.d(TAG, "Logging out and redirecting to login...");
                        // Clear auth data and navigate to login
                        authHelper.logout();
                        navigateToLogin();
                    } else {
                        Toast.makeText(BecomeProviderActivity.this,
                            "Registration failed: " + apiResponse.getMessage(),
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Registration failed with HTTP code: " + response.code());
                    String errorMsg = "Registration failed: ";
                    if (response.code() == 400) {
                        errorMsg += "Invalid request. Please check your input.";
                    } else if (response.code() == 401) {
                        errorMsg += "Unauthorized. Please login again.";
                        authHelper.logout();
                        navigateToLogin();
                        return;
                    } else if (response.code() == 409) {
                        errorMsg += "You are already registered as a provider.";
                    } else {
                        errorMsg += "Error code " + response.code();
                    }
                    Toast.makeText(BecomeProviderActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Provider registration failed", t);
                Toast.makeText(BecomeProviderActivity.this,
                    "Error: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(BecomeProviderActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegisterProvider.setEnabled(!isLoading);
        binding.btnCancel.setEnabled(!isLoading);
        binding.etProviderName.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
