package com.example.prm392_frontend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_frontend.models.MessageResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<MessageResponse> messages;
    private String currentUserId;

    public MessageAdapter(List<MessageResponse> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        MessageResponse message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageResponse message = messages.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(MessageResponse message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void updateMessages(List<MessageResponse> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvTime;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(MessageResponse message) {
            tvMessage.setText(message.getMessage());
            tvTime.setText(formatTime(message.getSentAtMillis()));
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvTime;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(MessageResponse message) {
            tvMessage.setText(message.getMessage());
            tvTime.setText(formatTime(message.getSentAtMillis()));
        }
    }

    private static String formatTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
