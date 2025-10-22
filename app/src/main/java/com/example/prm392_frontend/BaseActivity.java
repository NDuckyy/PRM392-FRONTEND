package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_frontend.utils.AuthHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    private BottomNavigationView navigationBar;
    private AuthHelper authHelper;

    @Override
    public void setContentView(int layoutResID) {
        // Create a wrapper layout with bottom navigation
        FrameLayout wrapper = new FrameLayout(this);

        // Inflate child activity's layout
        getLayoutInflater().inflate(layoutResID, wrapper, true);

        // Inflate bottom navigation
        getLayoutInflater().inflate(R.layout.navigation_bar, wrapper, true);

        super.setContentView(wrapper);

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        navigationBar = findViewById(R.id.navigation_bar);
        authHelper = new AuthHelper(this);

        if (navigationBar != null) {
            navigationBar.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Navigate to Provider Dashboard if PROVIDER, otherwise ProductList
                    String role = authHelper.getRole();
                    if ("PROVIDER".equalsIgnoreCase(role)) {
                        if (!(this instanceof ProviderDashboardActivity)) {
                            navigateToActivity(ProviderDashboardActivity.class);
                        }
                    } else {
                        if (!(this instanceof ProductListActivity)) {
                            navigateToActivity(ProductListActivity.class);
                        }
                    }
                    return true;
                } else if (itemId == R.id.nav_products) {
                    if (!(this instanceof ProductListActivity)) {
                        navigateToActivity(ProductListActivity.class);
                    }
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    // TODO: Navigate to CartActivity when created
                    // if (!(this instanceof CartActivity)) {
                    //     navigateToActivity(CartActivity.class);
                    // }
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    if (!(this instanceof ConversationListActivity)) {
                        navigateToActivity(ConversationListActivity.class);
                    }
                    return true;
                } else if (itemId == R.id.nav_profile) {
                     if (!(this instanceof UserProfileActivity)) {
                         navigateToActivity(UserProfileActivity.class);
                     }
                    return true;
                }
                return false;
            });

            // Highlight current tab
            highlightCurrentTab();
        }
    }

    private void highlightCurrentTab() {
        if (navigationBar == null) return;

        if (this instanceof ProductListActivity) {
            navigationBar.setSelectedItemId(R.id.nav_products);
        } else if (this instanceof UserProfileActivity) {
            navigationBar.setSelectedItemId(R.id.nav_profile);
        } else if (this instanceof ProviderDashboardActivity) {
            navigationBar.setSelectedItemId(R.id.nav_home);
        } else if (this instanceof ConversationListActivity) {
            navigationBar.setSelectedItemId(R.id.nav_messages);
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigationBar != null) {
            highlightCurrentTab();
        }
    }
}
