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
    private Button sendEmailButton, voiceInputButton, attachFileButton;
    private TextView attachedFileText;
    private TextToSpeech textToSpeech;

    private Uri attachedFileUri = null;
    private String recipientEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);

        emailInput = findViewById(R.id.email_input);
        messageInput = findViewById(R.id.email_message_input);
        sendEmailButton = findViewById(R.id.send_email_button);
        voiceInputButton = findViewById(R.id.voice_input_button);
        attachFileButton = findViewById(R.id.attach_file_button);
        attachedFileText = findViewById(R.id.attached_file_text);

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });

        sendEmailButton.setOnClickListener(v -> sendEmail());
        voiceInputButton.setOnClickListener(v -> askForEmail());
        attachFileButton.setOnClickListener(v -> openFilePicker());
    }

    private void sendEmail() {
        String email = emailInput.getText().toString();
        String message = messageInput.getText().toString();

        if (email.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please enter both email and message", Toast.LENGTH_SHORT).show();
            speak("Please enter both email and message.");
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
            Toast.makeText(this, "Email sent successfully!", Toast.LENGTH_SHORT).show();
            speak("Email sent successfully!");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send email: " + e.getMessage(), Toast.LENGTH_LONG).show();
            speak("Failed to send email. Please try again.");
        }
    }

    private void askForEmail() {
        speak("To whom do you want to send the email?");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the email address");

        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_EMAIL);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show();
            speak("Voice recognition is not supported on your device.");
        }
    }

    private void askForMessage() {
        speak("What message do you want to send?");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the message");

        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_MESSAGE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show();
            speak("Voice recognition is not supported on your device.");
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

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == FILE_PICKER_REQUEST_CODE) {
                attachedFileUri = data.getData();
                attachedFileText.setText(attachedFileUri != null ? attachedFileUri.getLastPathSegment() : "File attached");
                speak("File attached successfully.");
            } else {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (results != null && !results.isEmpty()) {
                    if (requestCode == VOICE_RECOGNITION_REQUEST_EMAIL) {
                        recipientEmail = results.get(0);
                        // Remove any unwanted spaces
                        recipientEmail = recipientEmail.replaceAll("\\s+", "").trim();
                        emailInput.setText(recipientEmail);
                        askForMessage();
                    } else if (requestCode == VOICE_RECOGNITION_REQUEST_MESSAGE) {
                        String message = results.get(0);
                        messageInput.setText(message);
                        Toast.makeText(this, "Ready to send the email", Toast.LENGTH_SHORT).show();
                        speak("Your email is ready to send.");
                    }
                }
            }
        }
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
