package com.example.prm392_frontend.api;

import com.example.prm392_frontend.models.LocationResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
public interface LocationApi {
    @GET("api/location/{providerName}")
    Call<LocationResponse> getLocation(@Path("providerName") String name);
}
