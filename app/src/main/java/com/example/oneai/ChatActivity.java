package com.example.oneai;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.noties.markwon.Markwon;

public class ChatActivity extends AppCompatActivity {

    private ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    private EditText inputEditText;
    private ImageButton sendButton;
    private Markwon markwon; // Markwon instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        // Initialize Markwon
        markwon = Markwon.create(this);

        // Initialize views
        recyclerView = findViewById(R.id.chatRecyclerView);
        inputEditText = findViewById(R.id.chatInputEditText);
        sendButton = findViewById(R.id.sendChatButton);

        // Setup RecyclerView
        chatAdapter = new ChatAdapter(markwon); // Pass Markwon to the adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Messages appear from bottom to top
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        // Add a welcome message
        chatAdapter.addMessage("Hello! How can I help you today?", false);

        // Setup send button click listener
        sendButton.setOnClickListener(v -> {
            String message = inputEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                // Add user message
                chatAdapter.addMessage(message, true);

                // Clear input field
                inputEditText.setText("");

                // Scroll to the bottom
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);

                // Process message and respond
                processUserMessage(message);
            }
        });
    }

    private void processUserMessage(String message) {
        // Simulate processing delay
        recyclerView.postDelayed(() -> {
            // Add AI response
            chatAdapter.addMessage("I received: " + message, false);

            // Scroll to the bottom
            recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        }, 500);
    }
}