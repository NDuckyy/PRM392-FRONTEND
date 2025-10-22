package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class ConversationSummary {
    @SerializedName("conversationId")
    private String conversationId;

    @SerializedName("lastMessage")
    private String lastMessage;

    @SerializedName("lastSender")
    private String lastSender;

    @SerializedName("updatedAtMillis")
    private long updatedAtMillis;

    public ConversationSummary() { }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastSender() {
        return lastSender;
    }

    public void setLastSender(String lastSender) {
        this.lastSender = lastSender;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }

    public void setUpdatedAtMillis(long updatedAtMillis) {
        this.updatedAtMillis = updatedAtMillis;
    }
}
