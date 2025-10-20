package com.example.prm392_frontend.api;

import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.CartItemUpdateRequest;
import com.example.prm392_frontend.models.CartItemUpdateResponse;
import com.example.prm392_frontend.models.CartResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CartApi {

    @GET("cart/current-user")
    Call<ApiResponse<CartResponse>> getCurrentUserCart(@Header("Authorization") String authToken);

    @PUT("cart/update/{cartItem}")
    Call<ApiResponse<CartItemUpdateResponse>> cartUpdateQuantity(@Path("cartItemId") Integer cartItemId, @Body CartItemUpdateRequest request);
}
