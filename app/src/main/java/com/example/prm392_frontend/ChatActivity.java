package com.example.prm392_frontend;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_frontend.api.ApiClient;
import com.example.prm392_frontend.api.ChatApi;
import com.example.prm392_frontend.models.ApiResponse;
import com.example.prm392_frontend.models.MessageResponse;
import com.example.prm392_frontend.models.SendMessageRequest;
import com.example.prm392_frontend.utils.AuthHelper;
import com.example.prm392_frontend.utils.FirestoreManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements FirestoreManager.OnMessagesUpdateListener {
    private static final String TAG = "ChatActivity";

    private RecyclerView rvMessages;
    private TextInputEditText etMessage;
    private FloatingActionButton btnSend;
    private MessageAdapter adapter;
    private List<MessageResponse> messages = new ArrayList<>();
    private AuthHelper authHelper;
    private FirestoreManager firestoreManager;

    private String conversationId;
    private String currentUserId;
    private String receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        authHelper = new AuthHelper(this);
        currentUserId = authHelper.getUsername();
        firestoreManager = new FirestoreManager();

        // Get conversation data from intent
        conversationId = getIntent().getStringExtra("conversationId");
        receiverId = getIntent().getStringExtra("receiverId");
        String receiverName = getIntent().getStringExtra("receiverName");

        // If no conversationId, create one
        if (conversationId == null && receiverId != null) {
            conversationId = currentUserId + "-" + receiverId;
        }

        setupToolbar(receiverName);
        setupViews();
        setupRecyclerView();

        // Start listening to realtime messages from Firestore
        if (conversationId != null) {
            startRealtimeListener();
        }

        btnSend.setOnClickListener(v -> sendMessage());
    }

    /**
     * Start Firestore realtime listener for messages
     */
    private void startRealtimeListener() {
        Log.d(TAG, "Starting Firestore realtime listener for: " + conversationId);
        firestoreManager.listenToMessages(conversationId, this);
    }

    private void setupToolbar(String receiverName) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (receiverName != null) {
            toolbar.setTitle("Chat with " + receiverName);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    /**
     * Firestore callback - Called when messages are updated in realtime
     */
    @Override
    public void onMessagesUpdated(List<MessageResponse> newMessages) {
        runOnUiThread(() -> {
            messages.clear();
            messages.addAll(newMessages);
            adapter.updateMessages(messages);
            scrollToBottom();

            Log.d(TAG, "Realtime update: " + messages.size() + " messages");
        });
    }

    /**
     * Firestore callback - Called when there's an error
     */
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Log.e(TAG, "Firestore error: " + error);
            Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText() != null ?
                etMessage.getText().toString().trim() : "";

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = authHelper.getToken();
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine sender type based on role
        String role = authHelper.getRole();
        String senderType = "PROVIDER".equals(role) ? "PROVIDER" : "USER";

        SendMessageRequest request = new SendMessageRequest(
                conversationId,
                senderType,
                currentUserId,
                messageText
        );

        btnSend.setEnabled(false);

        ChatApi chatApi = ApiClient.getAuthenticatedClient(this).create(ChatApi.class);
        chatApi.sendMessage("Bearer " + token, request)
                .enqueue(new Callback<ApiResponse<MessageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MessageResponse>> call,
                                   Response<ApiResponse<MessageResponse>> response) {
                btnSend.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<MessageResponse> apiResponse = response.body();

                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        // Message sent successfully
                        // Don't add message locally - let Firestore realtime listener handle it
                        etMessage.setText("");
                        Log.d(TAG, "Message sent successfully, waiting for Firestore update");
                    } else {
                        Toast.makeText(ChatActivity.this,
                                "Failed to send: " + apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChatActivity.this,
                            "Failed to send: Error " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MessageResponse>> call, Throwable t) {
                btnSend.setEnabled(true);
                Log.e(TAG, "Failed to send message", t);
                Toast.makeText(ChatActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop listening to Firestore when activity is destroyed
        if (firestoreManager != null) {
            firestoreManager.stopListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Optional: Stop listening when app goes to background to save resources
        // Uncomment if you want to stop realtime updates when chat is not visible
        // if (firestoreManager != null) {
        //     firestoreManager.stopListening();
        // }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optional: Resume listening when app comes back to foreground
        // Only needed if you stopped in onPause()
        // if (conversationId != null && firestoreManager != null) {
        //     startRealtimeListener();
        // }
    }
}
