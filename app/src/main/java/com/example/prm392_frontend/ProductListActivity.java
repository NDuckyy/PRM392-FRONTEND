package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private MaterialToolbar toolbar;
    private ChipGroup filterChips;
    private String currentCategory = "All";
    private String currentSort = "None";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.products_recycler_view);
        filterChips = findViewById(R.id.filter_chips);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        allProducts = ProductDataSource.getProducts();
        filteredProducts = new ArrayList<>(allProducts);

        adapter = new ProductAdapter(filteredProducts, product -> {
            Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        recyclerView.setAdapter(adapter);

        setupToolbar();
        setupFilterChips();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_list, menu);

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
            if (id == R.id.action_sort) {
                showSortDialog();
                return true;
            } else if (id == R.id.action_filter) {
                showFilterDialog();
                return true;
            }
            return false;
        });
    }

    private void setupFilterChips() {
        filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                currentCategory = "All";
            } else if (checkedId == R.id.chip_electronics) {
                currentCategory = "Electronics";
            } else if (checkedId == R.id.chip_sports) {
                currentCategory = "Sports";
            } else if (checkedId == R.id.chip_fashion) {
                currentCategory = "Fashion";
            } else if (checkedId == R.id.chip_home) {
                currentCategory = "Home";
            }
            applyFiltersAndSort();
        });
    }

    private void showSortDialog() {
        String[] sortOptions = {"Price: Low to High", "Price: High to Low", "Rating: High to Low", "Popularity"};
        new AlertDialog.Builder(this)
                .setTitle("Sort By")
                .setItems(sortOptions, (dialog, which) -> {
                    currentSort = sortOptions[which];
                    applyFiltersAndSort();
                })
                .show();
    }

    private void showFilterDialog() {
        String[] filterOptions = {"All", "Electronics", "Sports", "Fashion", "Home"};
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
                    updateChipSelection();
                    applyFiltersAndSort();
                    dialog.dismiss();
                })
                .show();
    }

    private void updateChipSelection() {
        switch (currentCategory) {
            case "All":
                filterChips.check(R.id.chip_all);
                break;
            case "Electronics":
                filterChips.check(R.id.chip_electronics);
                break;
            case "Sports":
                filterChips.check(R.id.chip_sports);
                break;
            case "Fashion":
                filterChips.check(R.id.chip_fashion);
                break;
            case "Home":
                filterChips.check(R.id.chip_home);
                break;
        }
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
            case "Rating: High to Low":
                Collections.sort(filteredProducts, (p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));
                break;
            case "Popularity":
                Collections.sort(filteredProducts, (p1, p2) -> Integer.compare(p2.getPopularity(), p1.getPopularity()));
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
