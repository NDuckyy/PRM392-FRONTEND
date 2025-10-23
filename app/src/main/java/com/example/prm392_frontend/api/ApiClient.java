package com.example.prm392_frontend.api;

import android.content.Context;

import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.CartAddRequest; // Thêm import
import com.example.prm392_frontend.models.CartItemUpdateRequest;
import com.example.prm392_frontend.models.CartItemUpdateResponse;
import com.example.prm392_frontend.models.LocationResponse;
import com.example.prm392_frontend.models.OrderRequest;
import com.example.prm392_frontend.models.OrderResponse;
import com.example.prm392_frontend.utils.AuthHelper;
import com.example.prm392_frontend.utils.AuthInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Call;
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

    public static CartApi getCartApi() {
        return getClient().create(CartApi.class);
    }

    // PHƯƠNG THỨC CHO VIỆC THÊM SẢN PHẨM VÀO GIỎ HÀNG
    public static Call<ApiResponse<Object>> addProductToCart(String authToken, CartAddRequest request) {
        return getCartApi().addProductToCart(authToken, request);
    }

    public static CartApi cartUpdateQuantity(){
        return getClient().create(CartApi.class);
    }

    public static Call<ApiResponse<CartItemUpdateResponse>> cartUpdateQuantity(String authToken, int cartItemId, CartItemUpdateRequest request) {
        return getCartApi().cartUpdateQuantity(authToken, cartItemId, request);
    }

    // PHƯƠNG THỨC CHO VIỆC XÓA MỘT SẢN PHẨM TRONG GIỎ HÀNG
    public static Call<ApiResponse<Object>> cartDeleteItem(String token, int cartItemId) {
        return getCartApi().cartDeleteItem(token,cartItemId);
    }

    // PHƯƠNG THỨC CHO VIỆC XÓA TẤT CẢ SẢN PHẨM TRONG GIỎ HÀNG
    public static Call<ApiResponse<Object>> cartClearAllItems(String token) {
        return getCartApi().cartClearAllItems(token);
    }

    public static PaymentApi getPaymentUrl() {
        return getClient().create(PaymentApi.class);
    }

    public static Call<ApiResponse<String>> getPaymentUrl(String token,int orderId) {
        return getPaymentUrl().getPaymentUrl(token,orderId);
    }

    public static OrderApi getOrderApi() {
        return getClient().create(OrderApi.class);
    }

    public static Call<ApiResponse<OrderResponse>> createOrder(String authToken, OrderRequest orderRequest) {
        return getOrderApi().create(authToken, orderRequest);
    }

    public static LocationApi getLocationApi() {
        return getClient().create(LocationApi.class);
    }

    public static Call<LocationResponse> getLocationById(int id) {
        return getLocationApi().getLocation(id);
    }


}
