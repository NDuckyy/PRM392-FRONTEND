package com.example.prm392_frontend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_frontend.models.ConversationSummary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private List<ConversationSummary> conversations;
    private OnConversationClickListener listener;
    private String currentUserId;

    public interface OnConversationClickListener {
        void onConversationClick(ConversationSummary conversation);
    }

    public ConversationAdapter(List<ConversationSummary> conversations, OnConversationClickListener listener, String currentUserId) {
        this.conversations = conversations;
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversationSummary conversation = conversations.get(position);
        holder.bind(conversation, listener, currentUserId);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void updateConversations(List<ConversationSummary> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvConversationId;
        private TextView tvLastMessage;
        private TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConversationId = itemView.findViewById(R.id.tvConversationId);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(ConversationSummary conversation, OnConversationClickListener listener, String currentUserId) {
            // Extract meaningful name from conversationId (format: userId-providerId)
            String displayName = extractDisplayName(conversation.getConversationId(), currentUserId);
            tvConversationId.setText(displayName);

            tvLastMessage.setText(conversation.getLastMessage() != null ?
                    conversation.getLastMessage() : "No messages yet");

            // Format time
            String timeText = formatTime(conversation.getUpdatedAtMillis());
            tvTime.setText(timeText);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }

        private String extractDisplayName(String conversationId, String currentUserId) {
            if (conversationId == null) return "Unknown";

            // Format is usually: userId-providerId
            String[] parts = conversationId.split("-");
            if (parts.length >= 2) {
                // Show the other person's name (not current user)
                String otherPersonId = parts[0].equals(currentUserId) ? parts[1] : parts[0];
                return "Chat with User " + otherPersonId;
            }
            return conversationId;
        }

        private String formatTime(long millis) {
            Date date = new Date(millis);
            Date now = new Date();

            long diff = now.getTime() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(date);
            } else if (hours > 0) {
                return hours + "h ago";
            } else if (minutes > 0) {
                return minutes + "m ago";
            } else {
                return "Just now";
            }
        }
    }
}
