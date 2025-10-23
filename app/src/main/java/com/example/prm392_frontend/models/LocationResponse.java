package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class LocationResponse {
    @SerializedName("id")
    public int id;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName("address")
    public String address;

    @SerializedName("provider")
    public Provider provider;

    public static class Provider {
        @SerializedName("id")
        public int id;

        @SerializedName("providerName")
        public String providerName;
    }
}
