package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_frontend.utils.AuthHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    private BottomNavigationView navigationBar;
    private AuthHelper authHelper;

    @Override
    public void setContentView(int layoutResID) {
        // Create a simple container
        android.widget.RelativeLayout wrapper = new android.widget.RelativeLayout(this);
        wrapper.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // 1) Inflate content - fill parent
        View content = getLayoutInflater().inflate(layoutResID, null, false);
        android.widget.RelativeLayout.LayoutParams contentParams =
            new android.widget.RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            );
        content.setLayoutParams(contentParams);
        content.setId(View.generateViewId());
        wrapper.addView(content);

        // 2) Inflate bottom nav - align parent bottom
        View navRoot = getLayoutInflater().inflate(R.layout.navigation_bar, null, false);
        android.widget.RelativeLayout.LayoutParams navParams =
            new android.widget.RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        navParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
        navRoot.setLayoutParams(navParams);
        wrapper.addView(navRoot);

        super.setContentView(wrapper);
        setupBottomNavigation();

        BottomNavigationView navigationBar = navRoot.findViewById(R.id.navigation_bar);

        // 3) Khi nav đo xong, cộng padding đáy cho content
        navigationBar.post(() -> {
            int navH = navigationBar.getHeight();
            content.setPadding(
                    content.getPaddingLeft(),
                    content.getPaddingTop(),
                    content.getPaddingRight(),
                    content.getPaddingBottom() + navH
            );
        });

        // 4) Nếu máy dùng gesture bar, cộng luôn system inset đáy
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            int sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            int navH = navigationBar.getHeight();
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    Math.max(v.getPaddingBottom(), sysBottom) + navH
            );
            return insets;
        });
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
                        if (!this.getClass().equals(ProductListActivity.class)) {
                            navigateToActivity(ProductListActivity.class);
                        }
                    }
                    return true;
                } else if (itemId == R.id.nav_products) {
                    if (!this.getClass().equals(ProductListActivity.class)) {
                        navigateToActivity(ProductListActivity.class);
                    }
                    return true;
                } else if (itemId == R.id.nav_cart) {
                     if (!(this instanceof CartActivity)) {
                         navigateToActivity(CartActivity.class);
                     }
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

        if (this.getClass().equals(ProductListActivity.class)) {
            navigationBar.setSelectedItemId(R.id.nav_products);
        } else if (this instanceof UserProfileActivity) {
            navigationBar.setSelectedItemId(R.id.nav_profile);
        } else if (this instanceof ProviderDashboardActivity) {
            navigationBar.setSelectedItemId(R.id.nav_home);
        } else if (this instanceof ConversationListActivity) {
            navigationBar.setSelectedItemId(R.id.nav_messages);
        } else if (this instanceof CartActivity) {
            navigationBar.setSelectedItemId(R.id.nav_cart);
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
