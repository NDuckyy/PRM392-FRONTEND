package com.example.prm392_frontend.api;


import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.User;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.Call;

public interface UserApi {
    @GET("users/profile")
    Call<ApiResponse<User>> getUserProfile(@Header("Authorization") String authToken);
}
