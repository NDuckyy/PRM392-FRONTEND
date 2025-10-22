package com.example.prm392_frontend.api;

import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.OrderRequest;
import com.example.prm392_frontend.models.OrderResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OrderApi {

    @POST("/api/order")
    Call<ApiResponse<OrderResponse>> create(@Header("Authorization") String authToken, @Body OrderRequest orderRequest);
}
