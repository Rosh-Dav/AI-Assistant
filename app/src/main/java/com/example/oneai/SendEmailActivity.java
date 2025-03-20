package com.example.oneai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class SendEmailActivity extends AppCompatActivity {

    private static final int VOICE_RECOGNITION_REQUEST_EMAIL = 101;
    private static final int VOICE_RECOGNITION_REQUEST_MESSAGE = 102;
    private static final int FILE_PICKER_REQUEST_CODE = 103;

    private EditText emailInput, messageInput;
    private TextView attachedFileText;
    private TextToSpeech textToSpeech;
    private Uri attachedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);

        initializeViews();
        setupTextToSpeech();
        setupClickListeners();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.email_input);
        messageInput = findViewById(R.id.email_message_input);
        attachedFileText = findViewById(R.id.attached_file_text);
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.send_email_button).setOnClickListener(v -> sendEmail());
        findViewById(R.id.voice_input_button).setOnClickListener(v -> askForEmail());
        findViewById(R.id.attach_file_button).setOnClickListener(v -> openFilePicker());
    }

    private void sendEmail() {
        String email = emailInput.getText().toString();
        String message = messageInput.getText().toString();

        if (email.isEmpty() || message.isEmpty()) {
            showToastAndSpeak("Please enter both email and message.");
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("*/*");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        if (attachedFileUri != null) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, attachedFileUri);
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
            showToastAndSpeak("Email sent successfully!");
        } catch (Exception e) {
            showToastAndSpeak("Failed to send email: " + e.getMessage());
        }
    }

    private void askForEmail() {
        speak("To whom do you want to send the email?");
        startVoiceRecognition("Speak the email address", VOICE_RECOGNITION_REQUEST_EMAIL);
    }

    private void askForMessage() {
        speak("What message do you want to send?");
        startVoiceRecognition("Speak the message", VOICE_RECOGNITION_REQUEST_MESSAGE);
    }

    private void startVoiceRecognition(String prompt, int requestCode) {
        // Set up voice recognition with appropriate parameters
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);

        try {
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            showToastAndSpeak("Voice recognition is not supported on your device.");
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_PICKER_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "No file manager found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == FILE_PICKER_REQUEST_CODE) {
            handleFilePickerResult(data);
        } else {
            handleVoiceRecognitionResult(requestCode, data);
        }
    }

    private void handleFilePickerResult(Intent data) {
        attachedFileUri = data.getData();
        attachedFileText.setText(attachedFileUri != null ? attachedFileUri.getLastPathSegment() : "File attached");
        speak("File attached successfully.");
    }

    private void handleVoiceRecognitionResult(int requestCode, Intent data) {
        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        if (results == null || results.isEmpty()) return;

        if (requestCode == VOICE_RECOGNITION_REQUEST_EMAIL) {
            // Clean up email by removing unwanted spaces
            String email = results.get(0).replaceAll("\\s+", "").trim();
            emailInput.setText(email);
            askForMessage();
        } else if (requestCode == VOICE_RECOGNITION_REQUEST_MESSAGE) {
            messageInput.setText(results.get(0));
            showToastAndSpeak("Your email is ready to send.");
        }
    }

    private void showToastAndSpeak(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        speak(message);
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}