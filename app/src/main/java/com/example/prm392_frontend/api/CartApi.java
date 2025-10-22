package com.example.prm392_frontend.api;

import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.CartAddRequest;
import com.example.prm392_frontend.models.CartItemUpdateRequest;
import com.example.prm392_frontend.models.CartItemUpdateResponse;
import com.example.prm392_frontend.models.CartResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CartApi {

    @POST("/cart/add")
    Call<ApiResponse<Object>> addProductToCart(@Header("Authorization") String authToken, @Body CartAddRequest request);

    @GET("cart/current-user")
    Call<ApiResponse<CartResponse>> getCurrentUserCart(@Header("Authorization") String authToken);

    @PUT("cart/update/{cartItemId}")
    Call<ApiResponse<CartItemUpdateResponse>> cartUpdateQuantity(@Header("Authorization") String authToken,@Path("cartItemId") Integer cartItemId, @Body CartItemUpdateRequest request);

    @DELETE("/cart/item/{cartItemId}")
    Call<ApiResponse<Object>> cartDeleteItem(@Header("Authorization") String authToken,@Path("cartItemId") Integer cartItemId);

    @DELETE("cart/clear/current-user")
    Call<ApiResponse<Object>> cartClearAllItems(@Header("Authorization") String authToken);
}
