package com.example.prm392_frontend.api;

import com.example.prm392_frontend.models.AuthResponse;
import com.example.prm392_frontend.models.LoginRequest;
import com.example.prm392_frontend.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("/api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("/api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
}
