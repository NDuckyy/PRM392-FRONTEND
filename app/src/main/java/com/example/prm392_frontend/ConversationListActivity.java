package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_frontend.models.ConversationSummary;
import com.example.prm392_frontend.utils.AuthHelper;
import com.example.prm392_frontend.utils.FirestoreManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class ConversationListActivity extends AppCompatActivity
        implements ConversationAdapter.OnConversationClickListener,
                   FirestoreManager.OnConversationsUpdateListener {
    private static final String TAG = "ConversationList";

    private RecyclerView rvConversations;
    private ConversationAdapter adapter;
    private List<ConversationSummary> conversations = new ArrayList<>();
    private ProgressBar progressBar;
    private View emptyState;
    private AuthHelper authHelper;
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        authHelper = new AuthHelper(this);
        firestoreManager = new FirestoreManager();

        setupToolbar();
        setupViews();

        // Check if user is logged in
        if (!authHelper.isLoggedIn()) {
            showLoginRequired();
            return;
        }

        setupRecyclerView();
        startRealtimeListener();
    }

    /**
     * Show message when user is not logged in
     */
    private void showLoginRequired() {
        progressBar.setVisibility(View.GONE);
        rvConversations.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);

        // Update empty state message to show login required
        TextView emptyMessage = emptyState.findViewById(R.id.tvEmptyState);
        if (emptyMessage != null) {
            emptyMessage.setText("Please login to view your messages");
        }
    }

    /**
     * Start Firestore realtime listener for conversations
     */
    private void startRealtimeListener() {
        String currentUserId = authHelper.getUsername();
        Log.d(TAG, "Starting Firestore realtime listener for conversations");
        setLoading(true);
        firestoreManager.listenToConversations(50, currentUserId, this);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        rvConversations = findViewById(R.id.rvConversations);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        String currentUserId = authHelper.getUsername();
        adapter = new ConversationAdapter(conversations, this, currentUserId);
        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvConversations.setAdapter(adapter);
    }

    /**
     * Firestore callback - Called when conversations are updated in realtime
     */
    @Override
    public void onConversationsUpdated(List<ConversationSummary> newConversations) {
        runOnUiThread(() -> {
            setLoading(false);
            conversations.clear();
            conversations.addAll(newConversations);
            adapter.updateConversations(conversations);
            updateEmptyState();

            Log.d(TAG, "Realtime update: " + conversations.size() + " conversations");
        });
    }

    /**
     * Firestore callback - Called when there's an error
     */
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            setLoading(false);
            Log.e(TAG, "Firestore error: " + error);
            Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvConversations.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvConversations.setVisibility(View.VISIBLE);
        }
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onConversationClick(ConversationSummary conversation) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("conversationId", conversation.getConversationId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check login status when activity resumes (e.g., after logout)
        if (!authHelper.isLoggedIn()) {
            // User logged out, clear conversations and show login required
            if (firestoreManager != null) {
                firestoreManager.stopAllListeners();
            }
            conversations.clear();
            if (adapter != null) {
                adapter.updateConversations(conversations);
            }
            showLoginRequired();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop listening to Firestore when activity is destroyed
        if (firestoreManager != null) {
            firestoreManager.stopAllListeners();
        }
    }
}
