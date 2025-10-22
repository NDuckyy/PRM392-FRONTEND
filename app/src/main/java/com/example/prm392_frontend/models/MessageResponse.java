package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class MessageResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("conversationId")
    private String conversationId;

    @SerializedName("senderType")
    private String senderType;

    @SerializedName("senderId")
    private String senderId;

    @SerializedName("message")
    private String message;

    @SerializedName("sentAtMillis")
    private long sentAtMillis;

    public MessageResponse() { }

    public MessageResponse(String id, String conversationId, String senderType,
                          String senderId, String message, long sentAtMillis) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.message = message;
        this.sentAtMillis = sentAtMillis;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public long getSentAtMillis() {
        return sentAtMillis;
    }

    public void setSentAtMillis(long sentAtMillis) {
        this.sentAtMillis = sentAtMillis;
    }
}
