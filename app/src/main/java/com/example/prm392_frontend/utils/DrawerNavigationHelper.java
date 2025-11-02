package com.example.prm392_frontend.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.prm392_frontend.CartActivity;
import com.example.prm392_frontend.ConversationListActivity;
import com.example.prm392_frontend.LoginActivity;
import com.example.prm392_frontend.ProductListActivity;
import com.example.prm392_frontend.ProviderDashboardActivity;
import com.example.prm392_frontend.R;
import com.example.prm392_frontend.UserProfileActivity;
import com.google.android.material.navigation.NavigationView;

/**
 * Helper class to manage navigation drawer across activities
 * Usage: DrawerNavigationHelper.setup(activity, drawerLayout, navigationView, toolbar);
 */
public class DrawerNavigationHelper {

    private static final String TAG = "DrawerNavHelper";

    /**
     * Setup navigation drawer for an activity
     */
    public static void setup(Activity activity, DrawerLayout drawerLayout,
                            NavigationView navigationView, Toolbar toolbar) {
        if (drawerLayout == null || navigationView == null) {
            Log.w(TAG, "DrawerLayout or NavigationView is null");
            return;
        }

        AuthHelper authHelper = new AuthHelper(activity);

        // Setup toolbar with drawer toggle
        if (toolbar != null && activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // Update header with user info
        updateHeader(navigationView, authHelper);

        // Show/hide Provider Dashboard based on role
        updateMenuVisibility(navigationView, authHelper);

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = handleNavigationItemSelected(activity, item, authHelper);
            drawerLayout.closeDrawer(GravityCompat.START);
            return handled;
        });
    }

    /**
     * Update menu visibility based on user role
     */
    private static void updateMenuVisibility(NavigationView navigationView, AuthHelper authHelper) {
        boolean isLoggedIn = authHelper.isLoggedIn();

        // Show Provider Dashboard only for PROVIDER role
        MenuItem providerDashboard = navigationView.getMenu().findItem(R.id.nav_provider_dashboard);
        if (providerDashboard != null) {
            boolean isProvider = "PROVIDER".equalsIgnoreCase(authHelper.getRole());
            providerDashboard.setVisible(isLoggedIn && isProvider);
        }

        // Show Login only when NOT logged in
        MenuItem login = navigationView.getMenu().findItem(R.id.nav_login);
        if (login != null) {
            login.setVisible(!isLoggedIn);
        }

        // Show Logout only when logged in
        MenuItem logout = navigationView.getMenu().findItem(R.id.nav_logout);
        if (logout != null) {
            logout.setVisible(isLoggedIn);
        }
    }

    /**
     * Update drawer header with user information
     */
    private static void updateHeader(NavigationView navigationView, AuthHelper authHelper) {
        android.view.View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView titleView = headerView.findViewById(R.id.nav_header_title);
            TextView subtitleView = headerView.findViewById(R.id.nav_header_subtitle);

            if (authHelper.isLoggedIn()) {
                String username = authHelper.getUsername();

                if (titleView != null) {
                    titleView.setText("Welcome, " + (username != null ? username : "User") + "!");
                }
                if (subtitleView != null) {
                    subtitleView.setText(authHelper.getRole() != null ? authHelper.getRole() : "");
                }
            } else {
                if (titleView != null) {
                    titleView.setText("Welcome!");
                }
                if (subtitleView != null) {
                    subtitleView.setText("Please login");
                }
            }
        }
    }

    /**
     * Handle navigation item selection
     */
    private static boolean handleNavigationItemSelected(Activity activity, MenuItem item,
                                                        AuthHelper authHelper) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_products) {
            return navigateIfNeeded(activity, ProductListActivity.class);
        } else if (itemId == R.id.nav_provider_dashboard) {
            return navigateIfNeeded(activity, ProviderDashboardActivity.class);
        } else if (itemId == R.id.nav_cart) {
            return navigateIfNeeded(activity, CartActivity.class);
        } else if (itemId == R.id.nav_messages) {
            return navigateIfNeeded(activity, ConversationListActivity.class);
        } else if (itemId == R.id.nav_profile) {
            return navigateIfNeeded(activity, UserProfileActivity.class);
        } else if (itemId == R.id.nav_login) {
            return handleLogin(activity);
        } else if (itemId == R.id.nav_logout) {
            return handleLogout(activity, authHelper);
        }

        return false;
    }

    /**
     * Handle login
     */
    private static boolean handleLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        return true;
    }

    /**
     * Handle logout
     */
    private static boolean handleLogout(Activity activity, AuthHelper authHelper) {
        authHelper.logout();
        Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to ProductList instead of Login
        Intent intent = new Intent(activity, ProductListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        return true;
    }

    /**
     * Navigate to target activity if not already there
     */
    private static boolean navigateIfNeeded(Activity currentActivity, Class<?> targetClass) {
        if (currentActivity.getClass().equals(targetClass)) {
            return false;
        }

        Intent intent = new Intent(currentActivity, targetClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        currentActivity.startActivity(intent);
        return true;
    }

    /**
     * Handle back press to close drawer if open
     */
    public static boolean onBackPressed(DrawerLayout drawerLayout) {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }
}
