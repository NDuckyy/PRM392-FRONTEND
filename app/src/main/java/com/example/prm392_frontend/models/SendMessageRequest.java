package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class SendMessageRequest {
    @SerializedName("conversationId")
    private String conversationId;

    @SerializedName("senderType")
    private String senderType;

    @SerializedName("senderId")
    private String senderId;

    @SerializedName("message")
    private String message;

    @SerializedName("userFcmToken")
    private String userFcmToken;

    public SendMessageRequest() { }

    public SendMessageRequest(String conversationId, String senderType, String senderId, String message) {
        this.conversationId = conversationId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserFcmToken() {
        return userFcmToken;
    }

    public void setUserFcmToken(String userFcmToken) {
        this.userFcmToken = userFcmToken;
    }
}
