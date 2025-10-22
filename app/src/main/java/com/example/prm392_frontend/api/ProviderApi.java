package com.example.prm392_frontend.api;

import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.RegisterProviderRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ProviderApi {
    @POST("/api/providers")
    Call<ApiResponse<Object>> registerProvider(@Body RegisterProviderRequest request);
}
