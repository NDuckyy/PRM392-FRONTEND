package com.example.prm392_frontend.api;

import android.content.Context;

import com.example.prm392_frontend.utils.AuthHelper;
import com.example.prm392_frontend.utils.AuthInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://prm392-backend.nducky.id.vn";
    private static Retrofit retrofit = null;
    private static Retrofit authenticatedRetrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getAuthenticatedClient(Context context) {
        AuthHelper authHelper = new AuthHelper(context);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(authHelper))
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static AuthApi getAuthApi() {
        return getClient().create(AuthApi.class);
    }

    public static ProductApi getProductApi() {
        return getClient().create(ProductApi.class);
    }
}
