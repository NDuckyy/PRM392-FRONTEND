package com.example.prm392_frontend.api;

import com.example.prm392_frontend.models.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PaymentApi {

    @POST("/api/payment/url/{orderId}")
    Call<ApiResponse<String>> getPaymentUrl(@Header("Authorization") String authToken, @Path("orderId") Integer orderId);
}
