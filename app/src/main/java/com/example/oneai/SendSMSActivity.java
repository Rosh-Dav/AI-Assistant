package com.example.oneai;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class SendSMSActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST = 101;
    private static final int VOICE_RECOGNITION_REQUEST_PHONE = 102;
    private static final int VOICE_RECOGNITION_REQUEST_MESSAGE = 103;

    private EditText phoneNumberInput, messageInput;
    private Button sendButton, voiceInputButton;
    private TextToSpeech textToSpeech;

    private String phoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_smsactivity);

        phoneNumberInput = findViewById(R.id.phone_number_input);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        voiceInputButton = findViewById(R.id.voice_input_button);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });

        // Check and request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        }

        sendButton.setOnClickListener(v -> sendSms());

        voiceInputButton.setOnClickListener(v -> askForPhoneNumber());
    }

    private void sendSms() {
        String phoneNumber = phoneNumberInput.getText().toString();
        String message = messageInput.getText().toString();

        if (phoneNumber.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please enter both phone number and message", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent successfully!", Toast.LENGTH_SHORT).show();
            speak("SMS sent successfully!");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            speak("Failed to send SMS. Please try again.");
        }
    }

    private void askForPhoneNumber() {
        speak("Which number do you want to send the message to?");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Which number do you want to send the message to?");

        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_PHONE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show();
            speak("Voice recognition is not supported on your device.");
        }
    }

    private void askForMessage() {
        speak("Speak the message you want to send.");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the message you want to send.");

        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_MESSAGE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show();
            speak("Voice recognition is not supported on your device.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                if (requestCode == VOICE_RECOGNITION_REQUEST_PHONE) {
                    phoneNumber = results.get(0);
                    phoneNumberInput.setText(phoneNumber);
                    askForMessage();
                } else if (requestCode == VOICE_RECOGNITION_REQUEST_MESSAGE) {
                    String message = results.get(0);
                    messageInput.setText(message);
                    Toast.makeText(this, "Ready to send the SMS", Toast.LENGTH_SHORT).show();
                    speak("Your message is ready to send.");
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                speak("SMS permission granted.");
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                speak("SMS permission denied. The app cannot send messages without it.");
            }
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
