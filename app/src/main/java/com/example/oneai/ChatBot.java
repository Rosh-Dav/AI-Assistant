package com.example.oneai;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.noties.markwon.Markwon;

public class ChatBot extends AppCompatActivity {

    private EditText chatInputEditText;
    private ImageButton sendChatButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private ProgressBar loadingProgressBar;
    private TextView headerText;

    private Markwon markwon;

    // OpenRouter API Key and URL
    private static final String API_KEY = "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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        // Initialize Markwon
        markwon = Markwon.create(this);

        // Initialize views
        chatInputEditText = findViewById(R.id.chatInputEditText);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        sendChatButton = findViewById(R.id.sendChatButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        headerText = findViewById(R.id.headerText);

        // Setup RecyclerView with adapter
        chatAdapter = new ChatAdapter(markwon); // Pass Markwon to the adapter
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Add a welcome message
        chatAdapter.addMessage("Hello! How can I help you today?", false);

        // Set click listener for send button
        sendChatButton.setOnClickListener(v -> sendChatMessage());
    }

    private void sendChatMessage() {
        String userMessage = chatInputEditText.getText().toString().trim();
        if (userMessage.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user message to chat
        chatAdapter.addMessage(userMessage, true);

        // Scroll to the bottom
        chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);

        // Clear input
        chatInputEditText.setText("");

        // Show loading indicator
        loadingProgressBar.setVisibility(View.VISIBLE);

        // Send message to API
        sendMessageToAPI(userMessage);
    }

    private void sendMessageToAPI(String userMessage) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create JSON body for the API request
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "deepseek/deepseek-chat");

            // Create messages array
            JSONArray messagesArray = new JSONArray();

            // Add system message to get more structured, comprehensive responses
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant that provides comprehensive and structured information. " +
                    "When asked about any topic, provide a detailed response with proper headings and subheadings. " +
                    "Your responses should be well-organized, thorough, and informative. " +
                    "Include relevant facts, explanations, and examples. " +
                    "Format your response with clear sections and a logical flow of information.");
            messagesArray.put(systemMessage);

            // Add user message
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", "user");
            messageObject.put("content", userMessage);
            messagesArray.put(messageObject);

            jsonBody.put("messages", messagesArray);
            jsonBody.put("max_tokens", 800); // Significantly increased to get more content

            // Add additional parameters for better responses
            jsonBody.put("temperature", 0.7);
            jsonBody.put("top_p", 1);

            Log.d("API Request", "Request body: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(ChatBot.this, "Error creating request", Toast.LENGTH_SHORT).show();
            loadingProgressBar.setVisibility(View.GONE);
            return;
        }

        // Create a POST request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, API_URL, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("API Response", "Response: " + response.toString());

                            // Hide loading indicator
                            loadingProgressBar.setVisibility(View.GONE);

                            // Extract the AI's response
                            String aiMessage = response
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            // Add the AI's response to the chat
                            chatAdapter.addMessage(aiMessage, false);

                            // Scroll to the bottom
                            chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("Parse Error", "Error parsing: " + e.getMessage());
                            Log.e("Response Content", response.toString());
                            Toast.makeText(ChatBot.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingProgressBar.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Hide loading indicator
                        loadingProgressBar.setVisibility(View.GONE);

                        if (error.networkResponse != null) {
                            Log.e("API Error", "Status Code: " + error.networkResponse.statusCode);
                            if (error.networkResponse.data != null) {
                                try {
                                    String errorResponse = new String(error.networkResponse.data);
                                    Log.e("API Error", "Error response: " + errorResponse);
                                    Toast.makeText(ChatBot.this, "API Error: " + errorResponse, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Log.e("API Error", "Error: " + error.getMessage());
                            Toast.makeText(ChatBot.this, "Error communicating with AI: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                // Add API key to headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + API_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest);
    }
}