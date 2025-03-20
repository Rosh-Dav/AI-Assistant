package com.example.oneai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private final List<Message> messages = new ArrayList<>();
    private Markwon markwon; // Markwon instance for rendering Markdown

    public ChatAdapter(Markwon markwon) {
        this.markwon = markwon;
    }

    public void addMessage(String message, boolean isUser) {
        messages.add(new Message(message, isUser));
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.user_message_item, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.ai_message_item, parent, false);
            return new AIMessageViewHolder(view, markwon);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message.text);
        } else if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).bind(message.text);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class Message {
        String text;
        boolean isUser;

        Message(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
        }
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.userMessageText);
        }

        void bind(String text) {
            messageText.setText(text);
        }
    }

    static class AIMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final Markwon markwon;

        AIMessageViewHolder(View itemView, Markwon markwon) {
            super(itemView);
            this.messageText = itemView.findViewById(R.id.aiMessageText);
            this.markwon = markwon;
        }

        void bind(String text) {
            // Use Markwon to render Markdown text
            markwon.setMarkdown(messageText, text);
        }
    }
}