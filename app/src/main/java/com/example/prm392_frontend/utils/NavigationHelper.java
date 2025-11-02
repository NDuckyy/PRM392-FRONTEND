package com.example.prm392_frontend.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.example.prm392_frontend.CartActivity;
import com.example.prm392_frontend.ConversationListActivity;
import com.example.prm392_frontend.ProductListActivity;
import com.example.prm392_frontend.ProviderDashboardActivity;
import com.example.prm392_frontend.R;
import com.example.prm392_frontend.UserProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Helper class to manage bottom navigation bar across activities
 * Usage: NavigationHelper.setup(activity, R.id.navigation_bar);
 */
public class NavigationHelper {

    private static final String TAG = "NavigationHelper";

    /**
     * Setup bottom navigation for an activity
     * @param activity The current activity
     * @param navBarId Resource ID of the BottomNavigationView in the layout
     */
    public static void setup(Activity activity, int navBarId) {
        BottomNavigationView navigationBar = activity.findViewById(navBarId);
        if (navigationBar == null) {
            Log.w(TAG, "Navigation bar not found in activity: " + activity.getClass().getSimpleName());
            return;
        }

        AuthHelper authHelper = new AuthHelper(activity);

        // Set up navigation listener
        navigationBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return handleHomeNavigation(activity, authHelper);
            } else if (itemId == R.id.nav_products) {
                return navigateIfNeeded(activity, ProductListActivity.class);
            } else if (itemId == R.id.nav_cart) {
                return navigateIfNeeded(activity, CartActivity.class);
            } else if (itemId == R.id.nav_messages) {
                return navigateIfNeeded(activity, ConversationListActivity.class);
            } else if (itemId == R.id.nav_profile) {
                return navigateIfNeeded(activity, UserProfileActivity.class);
            }

            return false;
        });

        // Highlight current tab
        highlightCurrentTab(activity, navigationBar, authHelper);
    }

    /**
     * Handle home navigation based on user role
     */
    private static boolean handleHomeNavigation(Activity activity, AuthHelper authHelper) {
        String role = authHelper.getRole();
        if ("PROVIDER".equalsIgnoreCase(role)) {
            return navigateIfNeeded(activity, ProviderDashboardActivity.class);
        } else {
            return navigateIfNeeded(activity, ProductListActivity.class);
        }
    }

    /**
     * Navigate to target activity if not already there
     */
    private static boolean navigateIfNeeded(Activity currentActivity, Class<?> targetClass) {
        if (currentActivity.getClass().equals(targetClass)) {
            // Already on this activity, don't navigate
            return false;
        }

        Intent intent = new Intent(currentActivity, targetClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        currentActivity.startActivity(intent);
        return true;
    }

    /**
     * Highlight the current tab based on activity
     */
    private static void highlightCurrentTab(Activity activity, BottomNavigationView navigationBar, AuthHelper authHelper) {
        String className = activity.getClass().getSimpleName();
        Log.d(TAG, "Highlighting tab for: " + className);

        int selectedItemId = -1;

        if (className.equals("ProductListActivity")) {
            selectedItemId = R.id.nav_products;
        } else if (className.equals("UserProfileActivity")) {
            selectedItemId = R.id.nav_profile;
        } else if (className.equals("ProviderDashboardActivity")) {
            selectedItemId = R.id.nav_home;
        } else if (className.equals("ConversationListActivity")) {
            selectedItemId = R.id.nav_messages;
        } else if (className.equals("CartActivity")) {
            selectedItemId = R.id.nav_cart;
        }

        if (selectedItemId != -1) {
            final int itemToSelect = selectedItemId;
            navigationBar.post(() -> {
                navigationBar.setSelectedItemId(itemToSelect);
                Log.d(TAG, "Selected item: " + itemToSelect);
            });
        }
    }

    /**
     * Call this in onResume() to update the selected tab
     */
    public static void updateSelectedTab(Activity activity, int navBarId) {
        BottomNavigationView navigationBar = activity.findViewById(navBarId);
        if (navigationBar != null) {
            AuthHelper authHelper = new AuthHelper(activity);
            highlightCurrentTab(activity, navigationBar, authHelper);
        }
    }
}
