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
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        markwon = Markwon.create(this);

        recyclerView = findViewById(R.id.chatRecyclerView);
        inputEditText = findViewById(R.id.chatInputEditText);
        sendButton = findViewById(R.id.sendChatButton);

        chatAdapter = new ChatAdapter(markwon);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        chatAdapter.addMessage("Hello! How can I help you today?", false);

        sendButton.setOnClickListener(v -> {
            String message = inputEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                chatAdapter.addMessage(message, true);

                inputEditText.setText("");

                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);

                processUserMessage(message);
            }
        });
    }

    private void processUserMessage(String message) {
        recyclerView.postDelayed(() -> {
            chatAdapter.addMessage("I received: " + message, false);

            recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        }, 500);
    }
}