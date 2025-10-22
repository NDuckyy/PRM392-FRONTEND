package com.example.prm392_frontend.utils;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final AuthHelper auth;

    public AuthInterceptor(@NonNull AuthHelper auth) {
        this.auth = auth;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = auth.getToken();

        Request.Builder builder = original.newBuilder()
                .header("accept", "*/*");

        boolean hadAuthHeader = false;
        if (token != null && !token.trim().isEmpty()) {
            if (!token.startsWith("Bearer ")) token = "Bearer " + token;
            builder.header("Authorization", token);
            hadAuthHeader = true;
        }

        Response resp = chain.proceed(builder.build());

        // Chỉ logout nếu:
        // - Request NÀY có Authorization (hadAuthHeader == true)
        // - Và server trả 401 (token hết hạn/không hợp lệ)
        if (hadAuthHeader && resp.code() == 401) {
            // Tùy dòng đời app: có thể phát broadcast để chuyển sang Login
            auth.logout();
        }

        return resp;
    }

}
