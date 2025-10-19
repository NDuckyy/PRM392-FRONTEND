package com.example.prm392_frontend.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthHelper {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences sharedPreferences;

    public AuthHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get authentication token
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    /**
     * Get logged in username
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    /**
     * Get user role
     */
    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    /**
     * Save authentication data
     */
    public void saveAuthData(String token, String username, String role) {
        boolean hasToken = token != null && !token.trim().isEmpty();
        sharedPreferences.edit()
                .putString("token", hasToken ? token : null)
                .putString("username", username)
                .putString("role", role)
                .putBoolean("is_logged_in", hasToken)
                .apply(); // hoặc .commit() nếu muốn đồng bộ
    }


    /**
     * Clear authentication data (logout)
     */
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
