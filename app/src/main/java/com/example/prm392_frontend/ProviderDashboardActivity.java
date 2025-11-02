package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.api.ProductApi;
import com.example.prm392_frontend.databinding.ActivityProviderDashboardBinding;
import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.ProductResponse;
import com.example.prm392_frontend.utils.AuthHelper;
import com.example.prm392_frontend.utils.ProductMapper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProviderDashboardActivity extends AppCompatActivity implements ProviderProductAdapter.OnProductActionListener {
    private static final String TAG = "ProviderDashboard";
    private static final int REQUEST_CODE_ADD_PRODUCT = 1001;
    private static final int REQUEST_CODE_EDIT_PRODUCT = 1002;

    private ActivityProviderDashboardBinding binding;
    private ProviderProductAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private AuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProviderDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authHelper = new AuthHelper(this);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadProducts();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Update welcome text with username
        String username = authHelper.getUsername();
        if (username != null) {
            binding.tvWelcome.setText("Welcome, " + username + "!");
        }
    }

    private void setupRecyclerView() {
        adapter = new ProviderProductAdapter(products, this);
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_PRODUCT);
        });

        binding.btnRefresh.setOnClickListener(v -> loadProducts());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void loadProducts() {
        setLoading(true);

        ProductApi productApi = ApiClient.getProductApi();
        productApi.getAllProducts().enqueue(new Callback<ApiResponse<List<ProductResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ProductResponse>>> call,
                                   Response<ApiResponse<List<ProductResponse>>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ProductResponse>> apiResponse = response.body();

                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        List<ProductResponse> productResponses = apiResponse.getData();
                        products.clear();

                        // Get current user's username to filter products
                        String currentUsername = authHelper.getUsername();
                        Log.d(TAG, "Current username: " + currentUsername);

                        // Filter products by provider's username
                        for (ProductResponse pr : productResponses) {
                            // Check if product has provider and provider has user
                            if (pr.getProvider() != null &&
                                pr.getProvider().getUser() != null &&
                                pr.getProvider().getUser().getUsername() != null) {

                                String productUsername = pr.getProvider().getUser().getUsername();
                                Log.d(TAG, "Product: " + pr.getProductName() + ", Provider username: " + productUsername);

                                // Only add products from current provider
                                if (currentUsername != null && currentUsername.equals(productUsername)) {
                                    products.add(ProductMapper.toProduct(pr));
                                }
                            }
                        }

                        adapter.updateProducts(products);
                        updateEmptyState();

                        Log.d(TAG, "Loaded " + products.size() + " products for provider: " + currentUsername);
                    } else {
                        Toast.makeText(ProviderDashboardActivity.this,
                                "Failed to load products: " + apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProviderDashboardActivity.this,
                            "Failed to load products: Error " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ProductResponse>>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Failed to load products", t);
                Toast.makeText(ProviderDashboardActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (products.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.rvProducts.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.fabAddProduct.setEnabled(!isLoading);
        binding.btnRefresh.setEnabled(!isLoading);
    }

    @Override
    public void onEditClick(Product product) {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra("product", product);
        intent.putExtra("mode", "edit");
        startActivityForResult(intent, REQUEST_CODE_EDIT_PRODUCT);
    }

    @Override
    public void onDeleteClick(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + product.getProductName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProduct(Product product) {
        setLoading(true);

        ProductApi productApi = ApiClient.getAuthenticatedClient(this).create(ProductApi.class);
        productApi.deleteProduct(product.getId()).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call,
                                   Response<ApiResponse<String>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();

                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(ProviderDashboardActivity.this,
                                "Product deleted successfully",
                                Toast.LENGTH_SHORT).show();
                        loadProducts(); // Reload list
                    } else {
                        Toast.makeText(ProviderDashboardActivity.this,
                                "Failed to delete: " + apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProviderDashboardActivity.this,
                            "Failed to delete: Error " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Failed to delete product", t);
                Toast.makeText(ProviderDashboardActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CODE_ADD_PRODUCT || requestCode == REQUEST_CODE_EDIT_PRODUCT)
                && resultCode == RESULT_OK) {
            // Reload products after adding/editing
            loadProducts();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
