package com.example.prm392_frontend.utils;

import android.util.Log;

import com.example.prm392_frontend.models.ConversationSummary;
import com.example.prm392_frontend.models.MessageResponse;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manage Firestore realtime listeners for chat
 */
public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private static final String COLLECTION_CONVERSATIONS = "conversations";
    private static final String COLLECTION_MESSAGES = "messages";

    private final FirebaseFirestore db;
    private ListenerRegistration messageListener;
    private ListenerRegistration conversationListener;

    public FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Listen to realtime messages in a conversation
     */
    public void listenToMessages(String conversationId, OnMessagesUpdateListener listener) {
        // Remove old listener if exists
        stopListening();

        Log.d(TAG, "Starting to listen to conversation: " + conversationId);

        // Listen to messages collection with realtime updates
        messageListener = db.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(COLLECTION_MESSAGES)
                .orderBy("sentAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error.getMessage(), error);
                        listener.onError(error.getMessage());
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<MessageResponse> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            MessageResponse msg = documentToMessage(doc);
                            if (msg != null) {
                                messages.add(msg);
                            }
                        }
                        Log.d(TAG, "Received " + messages.size() + " messages from Firestore");
                        listener.onMessagesUpdated(messages);
                    } else {
                        Log.d(TAG, "No messages in conversation yet");
                        listener.onMessagesUpdated(new ArrayList<>());
                    }
                });
    }

    /**
     * Listen to realtime conversations list for a specific user
     */
    public void listenToConversations(int limit, String currentUserId, OnConversationsUpdateListener listener) {
        // Remove old listener if exists
        stopConversationListening();

        Log.d(TAG, "Starting to listen to conversations for user: " + currentUserId);

        // Listen to conversations collection with realtime updates
        // Filter conversations where conversationId contains currentUserId
        conversationListener = db.collection(COLLECTION_CONVERSATIONS)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Conversations listen failed: " + error.getMessage(), error);
                        listener.onError(error.getMessage());
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<ConversationSummary> conversations = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String conversationId = doc.getId();

                            // Parse conversationId (format: userId-providerId)
                            // Check if current user is one of the participants
                            if (conversationId != null && isUserInConversation(conversationId, currentUserId)) {
                                ConversationSummary conv = documentToConversation(doc);
                                if (conv != null) {
                                    conversations.add(conv);
                                }
                            }
                        }
                        Log.d(TAG, "Received " + conversations.size() + " conversations for user " + currentUserId);
                        listener.onConversationsUpdated(conversations);
                    } else {
                        Log.d(TAG, "No conversations yet");
                        listener.onConversationsUpdated(new ArrayList<>());
                    }
                });
    }

    /**
     * Check if the current user is a participant in the conversation
     * ConversationId format: userId-providerId
     */
    private boolean isUserInConversation(String conversationId, String currentUserId) {
        if (conversationId == null || currentUserId == null) {
            return false;
        }

        String[] parts = conversationId.split("-");
        if (parts.length >= 2) {
            // Check if currentUserId matches either part
            return parts[0].equals(currentUserId) || parts[1].equals(currentUserId);
        }

        return false;
    }

    /**
     * Stop listening to messages
     */
    public void stopListening() {
        if (messageListener != null) {
            Log.d(TAG, "Stopping message listener");
            messageListener.remove();
            messageListener = null;
        }
    }

    /**
     * Stop listening to conversations
     */
    public void stopConversationListening() {
        if (conversationListener != null) {
            Log.d(TAG, "Stopping conversation listener");
            conversationListener.remove();
            conversationListener = null;
        }
    }

    /**
     * Stop all listeners
     */
    public void stopAllListeners() {
        stopListening();
        stopConversationListening();
    }

    /**
     * Convert Firestore document to MessageResponse
     */
    private MessageResponse documentToMessage(DocumentSnapshot doc) {
        try {
            String id = doc.getId();
            String conversationId = doc.getString("conversationId");
            String senderType = doc.getString("senderType");
            String senderId = doc.getString("senderId");
            String message = doc.getString("message");

            // Get timestamp - Firestore uses Timestamp object
            long sentAtMillis = System.currentTimeMillis();
            Object sentAtObj = doc.get("sentAt");

            if (sentAtObj instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) sentAtObj;
                sentAtMillis = timestamp.toDate().getTime();
            } else if (sentAtObj instanceof Long) {
                sentAtMillis = (Long) sentAtObj;
            }

            return new MessageResponse(
                    id,
                    conversationId,
                    senderType != null ? senderType : "USER",
                    senderId != null ? senderId : "unknown",
                    message != null ? message : "",
                    sentAtMillis
            );
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message document: " + doc.getId(), e);
            return null;
        }
    }

    /**
     * Convert Firestore document to ConversationSummary
     */
    private ConversationSummary documentToConversation(DocumentSnapshot doc) {
        try {
            String conversationId = doc.getId();
            String lastMessage = doc.getString("lastMessage");
            String lastSender = doc.getString("lastSender");

            // Get timestamp
            long updatedAtMillis = System.currentTimeMillis();
            Object updatedAtObj = doc.get("updatedAt");

            if (updatedAtObj instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) updatedAtObj;
                updatedAtMillis = timestamp.toDate().getTime();
            } else if (updatedAtObj instanceof Long) {
                updatedAtMillis = (Long) updatedAtObj;
            }

            ConversationSummary summary = new ConversationSummary();
            summary.setConversationId(conversationId);
            summary.setLastMessage(lastMessage != null ? lastMessage : "");
            summary.setLastSender(lastSender != null ? lastSender : "");
            summary.setUpdatedAtMillis(updatedAtMillis);

            return summary;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing conversation document: " + doc.getId(), e);
            return null;
        }
    }

    /**
     * Callback interface for message updates
     */
    public interface OnMessagesUpdateListener {
        void onMessagesUpdated(List<MessageResponse> messages);
        void onError(String error);
    }

    /**
     * Callback interface for conversation list updates
     */
    public interface OnConversationsUpdateListener {
        void onConversationsUpdated(List<ConversationSummary> conversations);
        void onError(String error);
    }
}
