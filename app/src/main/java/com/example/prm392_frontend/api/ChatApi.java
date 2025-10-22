package com.example.prm392_frontend.api;

import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.ConversationSummary;
import com.example.prm392_frontend.models.MessageResponse;
import com.example.prm392_frontend.models.SendMessageRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatApi {

    @POST("/api/chat/send")
    Call<ApiResponse<MessageResponse>> sendMessage(
            @Header("Authorization") String authorization,
            @Body SendMessageRequest request
    );

    @GET("/api/chat/messages")
    Call<ApiResponse<List<MessageResponse>>> getMessages(
            @Header("Authorization") String authorization,
            @Query("conversationId") String conversationId,
            @Query("limit") Integer limit
    );

    @GET("/api/chat/conversations")
    Call<ApiResponse<List<ConversationSummary>>> getConversations(
            @Header("Authorization") String authorization,
            @Query("limit") Integer limit
    );
}
