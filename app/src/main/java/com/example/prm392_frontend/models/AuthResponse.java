package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("code")    private int code;
    @SerializedName("message") private String message;
    @SerializedName("data")    private Data data;

    public static class Data {
        @SerializedName("token")    private String token;
        @SerializedName("username") private String username;
        @SerializedName("role")     private String role;
    }

    // Getters “thông minh”
    public int getCode()              { return code; }
    public String getMessage()        { return message; }
    public Data getData()             { return data; }

    public String getToken()          { return data != null ? data.token    : null; }
    public String getUsername()       { return data != null ? data.username : null; }
    public String getRole()           { return data != null ? data.role     : null; }
}
