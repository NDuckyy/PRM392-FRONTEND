package com.example.prm392_frontend;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.api.ProductApi;
import com.example.prm392_frontend.databinding.ActivityAddEditProductBinding;
import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.CategoryResponse;
import com.example.prm392_frontend.models.CreateOrUpdateProductRequest;
import com.example.prm392_frontend.models.ProductResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditProductActivity extends AppCompatActivity {
    private static final String TAG = "AddEditProduct";

    private ActivityAddEditProductBinding binding;
    private Product existingProduct;
    private boolean isEditMode = false;

    private Map<String, Integer> categoryMap = new HashMap<>();
    private List<String> categoryNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkMode();
        setupToolbar();
        loadCategories();
        setupListeners();
    }

    private void checkMode() {
        existingProduct = getIntent().getParcelableExtra("product");
        String mode = getIntent().getStringExtra("mode");
        isEditMode = "edit".equals(mode) && existingProduct != null;

        if (isEditMode) {
            fillProductData();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(isEditMode ? "Edit Product" : "Add Product");
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> saveProduct());
    }

    private void loadCategories() {
        ProductApi productApi = ApiClient.getProductApi();
        productApi.getAllCategories().enqueue(new Callback<ApiResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CategoryResponse>>> call,
                                   Response<ApiResponse<List<CategoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<CategoryResponse>> apiResponse = response.body();

                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        List<CategoryResponse> categories = apiResponse.getData();
                        categoryNames.clear();
                        categoryMap.clear();

                        for (CategoryResponse cat : categories) {
                            categoryNames.add(cat.getCategoryName());
                            categoryMap.put(cat.getCategoryName(), cat.getId());
                        }

                        setupCategoryDropdown();

                        // If edit mode, set category after loading
                        if (isEditMode && existingProduct != null && existingProduct.getCategoryID() != null) {
                            binding.actvCategory.setText(existingProduct.getCategoryID().getCategoryName(), false);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CategoryResponse>>> call, Throwable t) {
                Log.e(TAG, "Failed to load categories", t);
                Toast.makeText(AddEditProductActivity.this,
                        "Failed to load categories",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        binding.actvCategory.setAdapter(adapter);
    }

    private void fillProductData() {
        if (existingProduct == null) return;

        binding.etProductName.setText(existingProduct.getProductName());
        binding.etPrice.setText(String.valueOf(existingProduct.getPrice()));
        binding.etImageURL.setText(existingProduct.getImageURL());
        binding.etBriefDescription.setText(existingProduct.getBriefDescription());
        binding.etFullDescription.setText(existingProduct.getFullDescription());
        binding.etTechnicalSpec.setText(existingProduct.getTechnicalSpecifications());
    }

    private void saveProduct() {
        // Validate inputs
        String productName = binding.etProductName.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        String category = binding.actvCategory.getText().toString().trim();
        String imageURL = binding.etImageURL.getText().toString().trim();
        String briefDesc = binding.etBriefDescription.getText().toString().trim();
        String fullDesc = binding.etFullDescription.getText().toString().trim();
        String techSpec = binding.etTechnicalSpec.getText().toString().trim();

        // Validation
        if (productName.isEmpty()) {
            binding.tilProductName.setError("Product name is required");
            return;
        }
        if (priceStr.isEmpty()) {
            binding.tilPrice.setError("Price is required");
            return;
        }
        if (category.isEmpty()) {
            binding.tilCategory.setError("Category is required");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                binding.tilPrice.setError("Price must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilPrice.setError("Invalid price");
            return;
        }

        Integer categoryId = categoryMap.get(category);
        if (categoryId == null) {
            binding.tilCategory.setError("Invalid category selected");
            return;
        }

        // Clear errors
        binding.tilProductName.setError(null);
        binding.tilPrice.setError(null);
        binding.tilCategory.setError(null);

        // Create request
        CreateOrUpdateProductRequest request = new CreateOrUpdateProductRequest(
                productName,
                briefDesc.isEmpty() ? null : briefDesc,
                fullDesc.isEmpty() ? null : fullDesc,
                techSpec.isEmpty() ? null : techSpec,
                price,
                imageURL.isEmpty() ? null : imageURL,
                categoryId
        );

        if (isEditMode) {
            updateProduct(request);
        } else {
            createProduct(request);
        }
    }

    private void createProduct(CreateOrUpdateProductRequest request) {
        setLoading(true);

        ProductApi productApi = ApiClient.getAuthenticatedClient(this).create(ProductApi.class);
        productApi.createProduct(request).enqueue(new Callback<ApiResponse<ProductResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductResponse>> call,
                                   Response<ApiResponse<ProductResponse>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ProductResponse> apiResponse = response.body();

                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(AddEditProductActivity.this,
                                "Product created successfully!",
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddEditProductActivity.this,
                                "Failed: " + apiResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AddEditProductActivity.this,
                            "Failed to create product: Error " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductResponse>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Failed to create product", t);
                Toast.makeText(AddEditProductActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct(CreateOrUpdateProductRequest request) {
        if (existingProduct == null) return;

        setLoading(true);

        ProductApi productApi = ApiClient.getAuthenticatedClient(this).create(ProductApi.class);
        productApi.updateProduct(existingProduct.getId(), request)
                .enqueue(new Callback<ApiResponse<ProductResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ProductResponse>> call,
                                           Response<ApiResponse<ProductResponse>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<ProductResponse> apiResponse = response.body();

                            if (apiResponse.getCode() == 200) {
                                Toast.makeText(AddEditProductActivity.this,
                                        "Product updated successfully!",
                                        Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(AddEditProductActivity.this,
                                        "Failed: " + apiResponse.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(AddEditProductActivity.this,
                                    "Failed to update product: Error " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ProductResponse>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Failed to update product", t);
                        Toast.makeText(AddEditProductActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!isLoading);
        binding.etProductName.setEnabled(!isLoading);
        binding.etPrice.setEnabled(!isLoading);
        binding.actvCategory.setEnabled(!isLoading);
        binding.etImageURL.setEnabled(!isLoading);
        binding.etBriefDescription.setEnabled(!isLoading);
        binding.etFullDescription.setEnabled(!isLoading);
        binding.etTechnicalSpec.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
