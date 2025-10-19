package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.CategoryResponse;
import com.example.prm392_frontend.models.ProductResponse;
import com.example.prm392_frontend.utils.AuthHelper;
import com.example.prm392_frontend.utils.ProductMapper;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListActivity extends BaseActivity {

    private static final String TAG = "ProductListActivity";

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private List<String> categoryNames;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private TextView errorText;
    private AuthHelper authHelper;
    private String currentCategory = "All";
    private String currentSort = "None";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        authHelper = new AuthHelper(this);

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.products_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        errorText = findViewById(R.id.error_text);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        allProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();
        categoryNames = new ArrayList<>();

        adapter = new ProductAdapter(filteredProducts, product -> {
            Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        recyclerView.setAdapter(adapter);

        setupToolbar();

        // Fetch categories first, then products
        fetchCategoriesFromApi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_list, menu);

        // Update Login/Logout menu item based on auth state
        MenuItem authItem = menu.findItem(R.id.action_auth);
        if (authHelper.isLoggedIn()) {
            authItem.setTitle("Logout");
        } else {
            authItem.setTitle("Login");
        }

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setQueryHint("Search products...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchQuery = query;
                    applyFiltersAndSort();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    searchQuery = newText;
                    applyFiltersAndSort();
                    return true;
                }
            });
        }

        return true;
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_auth) {
                handleAuthAction();
                return true;
            } else if (id == R.id.action_sort) {
                showSortDialog();
                return true;
            } else if (id == R.id.action_filter) {
                showFilterDialog();
                return true;
            }
            return false;
        });
    }

    private void handleAuthAction() {
        if (authHelper.isLoggedIn()) {
            // Logout
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        authHelper.logout();
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        // Refresh menu to show Login
                        invalidateOptionsMenu();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            // Login - navigate to LoginActivity
            Intent intent = new Intent(ProductListActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh menu when returning to this activity (in case user logged in)
        invalidateOptionsMenu();
    }


    private void showSortDialog() {
        String[] sortOptions = {"Price: Low to High", "Price: High to Low"};
        new AlertDialog.Builder(this)
                .setTitle("Sort By")
                .setItems(sortOptions, (dialog, which) -> {
                    currentSort = sortOptions[which];
                    applyFiltersAndSort();
                })
                .show();
    }

    private void showFilterDialog() {
        // Build filter options: "All" + categories
        List<String> filterOptionsList = new ArrayList<>();
        filterOptionsList.add("All");
        filterOptionsList.addAll(categoryNames);

        String[] filterOptions = filterOptionsList.toArray(new String[0]);

        int currentIndex = 0;
        for (int i = 0; i < filterOptions.length; i++) {
            if (filterOptions[i].equals(currentCategory)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Filter by Category")
                .setSingleChoiceItems(filterOptions, currentIndex, (dialog, which) -> {
                    currentCategory = filterOptions[which];
                    applyFiltersAndSort();
                    dialog.dismiss();
                })
                .show();
    }

    private void applyFiltersAndSort() {
        // Filter by category
        if (currentCategory.equals("All")) {
            filteredProducts = new ArrayList<>(allProducts);
        } else {
            filteredProducts = allProducts.stream()
                    .filter(p -> p.getCategory().equals(currentCategory))
                    .collect(Collectors.toList());
        }

        // Filter by search query
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String query = searchQuery.toLowerCase().trim();
            filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(query) ||
                                 p.getDescription().toLowerCase().contains(query) ||
                                 p.getCategory().toLowerCase().contains(query) ||
                                 p.getBrand().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }

        // Sort
        switch (currentSort) {
            case "Price: Low to High":
                Collections.sort(filteredProducts, Comparator.comparingDouble(Product::getPrice));
                break;
            case "Price: High to Low":
                Collections.sort(filteredProducts, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                break;
        }

        adapter = new ProductAdapter(filteredProducts, product -> {
            Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        recyclerView.setAdapter(adapter);
    }

    private void fetchCategoriesFromApi() {
        ApiClient.getProductApi().getAllCategories().enqueue(new Callback<ApiResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CategoryResponse>>> call, @NonNull Response<ApiResponse<List<CategoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CategoryResponse> categories = response.body().getData();
                    if (categories != null) {
                        categoryNames.clear();
                        for (CategoryResponse category : categories) {
                            categoryNames.add(category.getCategoryName());
                        }
                        Log.d(TAG, "Fetched " + categoryNames.size() + " categories from API");
                    }
                    // Now fetch products
                    fetchProductsFromApi();
                } else {
                    Log.e(TAG, "Failed to fetch categories: " + response.code());
                    // Still try to fetch products even if categories fail
                    fetchProductsFromApi();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CategoryResponse>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Categories API call failed", t);
                // Still try to fetch products even if categories fail
                fetchProductsFromApi();
            }
        });
    }

    private void fetchProductsFromApi() {
        showLoading(true);

        ApiClient.getProductApi().getAllProducts().enqueue(new Callback<ApiResponse<List<ProductResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ProductResponse>>> call, @NonNull Response<ApiResponse<List<ProductResponse>>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ProductResponse> productResponses = response.body().getData();

                    if (productResponses != null) {
                        // Convert API response to Product models
                        allProducts = ProductMapper.fromResponseList(productResponses);
                        filteredProducts = new ArrayList<>(allProducts);

                        Log.d(TAG, "Fetched " + allProducts.size() + " products from API");

                        // Update adapter
                        applyFiltersAndSort();

                        if (allProducts.isEmpty()) {
                            showError("No products available");
                        }
                    } else {
                        Log.e(TAG, "Product data is null");
                        showError("No products available");
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Unknown error";
                    Log.e(TAG, "API response not successful: " + response.code() + " - " + errorMsg);
                    showError("Failed to load products: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ProductResponse>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "API call failed", t);
                showError("Network error: " + t.getMessage());
                Toast.makeText(ProductListActivity.this, "Failed to fetch products: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        errorText.setVisibility(View.GONE);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
