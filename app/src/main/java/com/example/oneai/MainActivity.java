package com.example.oneai;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;

    private static final int REQUEST_CODE_NOTE_CONTENT = 2;

    private static final int REQUEST_STORAGE_PERMISSION = 3;
    private String pendingNoteTimestamp;
    private TextView commandHistory;
    private ArrayList<String> commandList;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize command history and command list
        commandHistory = findViewById(R.id.commandHistory);
        commandList = new ArrayList<>();

        // Initialize media player
        mediaPlayer = new MediaPlayer();

        // Initializing buttons
        Button ttsButton = findViewById(R.id.tts_button);
        Button newsButton = findViewById(R.id.news_button);
        Button mathButton = findViewById(R.id.math_calculation_button);
        Button translationButton = findViewById(R.id.language_translation_button);
        Button smsButton = findViewById(R.id.send_sms_button);
        Button emailButton = findViewById(R.id.send_email_button);
        Button weatherButton = findViewById(R.id.weather_update_button);
        Button speakButton = findViewById(R.id.voice_command_button);
        ImageButton ImageOneAI = findViewById(R.id.imgViewOneAI);// Speak button

        // Setting click listeners
        ttsButton.setOnClickListener(view -> openActivity(TTSActivity.class));
        newsButton.setOnClickListener(view -> openActivity(ChatBot.class));
        mathButton.setOnClickListener(view -> openActivity(MathCalculationActivity.class));
        translationButton.setOnClickListener(view -> openActivity(LanguageTranslationActivity.class));
        smsButton.setOnClickListener(view -> openActivity(SendSMSActivity.class));
        emailButton.setOnClickListener(view -> openActivity(SendEmailActivity.class));
        weatherButton.setOnClickListener(view -> openActivity(WeatherUpdateActivity.class));
        ImageOneAI.setOnClickListener(view -> openActivity(AboutAI.class));

        // Speak button functionality
        speakButton.setOnClickListener(view -> startVoiceRecognition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Method to open new activities
    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }

    // Method to start voice recognition
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your command...");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    // Handling speech input results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String command = results.get(0).toLowerCase();
                String timestamp = getCurrentTimeAndDate();
                handleVoiceCommand(command, timestamp);
            }
        }
        else if (requestCode == REQUEST_CODE_NOTE_CONTENT && resultCode == RESULT_OK && data != null) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String noteContent = results.get(0);
                File savedNote = saveNote(noteContent, pendingNoteTimestamp);

                if (savedNote != null) {
                    // Successfully saved the note
                    String resultMessage = "Note saved successfully: " + savedNote.getName();
                    addCommandToHistory("Note taken -> " + resultMessage + " [" + pendingNoteTimestamp + "]");

                } else {
                    // Failed to save the note
                    addCommandToHistory("Note taken -> Failed to save note [" + pendingNoteTimestamp + "]");
                }
            }
        }
    }



    // Handling voice commands with timestamp
    private void handleVoiceCommand(String command, String timestamp) {
        String response;

        if (command.contains("take photo") || command.contains("camera")) {
            response = capturePhoto(timestamp);
        } else if (command.contains("search")) {
            String query = command.replace("search", "").trim();
            searchWeb(query, timestamp);
            response = "Searched for: " + query;
        } else if (command.contains("what time is it") || command.contains("what date is it")) {
            response = getTimeAndDate();
        } else if (command.contains("play music")) {
            response = playMusic(timestamp);
        } else if (command.contains("stop music")) {
            response = stopMusic(timestamp);
        } else if (command.contains("open youtube")) {
            openAppWithPackage("com.google.android.youtube", "https://www.youtube.com");
            response = "Opening YouTube";
        } else if (command.contains("open chrome")) {
            openAppWithPackage("com.android.chrome", "https://www.google.com");
            response = "Opening Google Chrome";
        } else if (command.contains("open whatsapp")) {
            openAppWithPackage("com.whatsapp", "whatsapp://send?text=Hello");
            response = "Opening WhatsApp";
        } else if (command.contains("open facebook")) {
            openAppWithPackage("com.facebook.katana", "fb://facewebmodal/f?href=https://www.facebook.com");
            response = "Opening Facebook";
        } else if (command.contains("open instagram")) {
            openAppWithPackage("com.instagram.android", "http://instagram.com/");
            response = "Opening Instagram";
        } else if (command.contains("open calendar")) {
            openCalendar();
            response = "Opening Calendar";
        } else if (command.contains("open maps")) {
            openAppWithPackage("com.google.android.apps.maps", "geo:0,0?q=Google");
            response = "Opening Google Maps";
        } else if (command.contains("open play store")) {
            openAppWithPackage("com.android.vending", null);
            response = "Opening Play Store";
        } else if (command.contains("open settings")) {
            openAppWithPackage("com.android.settings", null);
            response = "Opening Settings";
        } else if (command.contains("open gallery")) {
            openGallery();
            response = "Opening Gallery";
        } else if (command.contains("call")) {
            // Extract the phone number from the command
            String phoneNumber = extractPhoneNumber(command);
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                response = makePhoneCall(phoneNumber, timestamp);
            } else {
                response = "Could not identify a valid phone number";
            }
        } else if (command.contains("open files")) {
            openFiles();
            response = "Opening Files";
        } else if (command.contains("open amazon")) {
            openAppWithPackage("in.amazon.mShop.android.shopping", "https://www.amazon.in");
            response = "Opening Amazon";
        } else if (command.contains("take note")) {
            startNoteCapture(timestamp);
            response = "Ready to take your note. Please speak";
        }  else {
            response = "Command not recognized.";
        }

        addCommandToHistory(command + " -> " + response + " [" + timestamp + "]");
    }

    // Method to initiate note capture
    private void startNoteCapture(String timestamp) {

        // Save the timestamp for when we save the note
        pendingNoteTimestamp = timestamp;

        // Start speech recognition for note content
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note content...");

        try {
            startActivityForResult(intent, REQUEST_CODE_NOTE_CONTENT);
        } catch (ActivityNotFoundException e) {
            addCommandToHistory("Error: Speech recognition not available");
        }
    }

    // Play a small beep sound to indicate note taking is starting

    // Method to save note to a file
    private File saveNote(String noteContent, String timestamp) {
        try {
            // Create a filename based on the timestamp
            String filename = "Note_" + timestamp.replace(" ", "_").replace(":", "-") + ".txt";

            // Get the Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            // Create the note file in Downloads
            File noteFile = new File(downloadsDir, filename);

            // Write the content to the file
            FileWriter writer = new FileWriter(noteFile);
            writer.write(noteContent);
            writer.close();

            return noteFile;
        } catch (IOException e) {
            return null;
        }
    }




    // Unified method to open apps with fallback
    private void openAppWithPackage(String packageName, String uriString) {
        try {
            Intent intent;
            if (uriString != null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                intent.setPackage(packageName);
            } else {
                PackageManager pm = getPackageManager();
                intent = pm.getLaunchIntentForPackage(packageName);
            }

            if (intent != null) {
                startActivity(intent);
            } else {
                openInPlayStore(packageName);
            }
        } catch (ActivityNotFoundException e) {
            openInPlayStore(packageName);
        }
    }

    // Method to extract phone number from voice command
    private String extractPhoneNumber(String command) {
        // Remove the "call" word and any leading/trailing spaces
        String processed = command.replace("call", "").trim();

        // Pattern to match digits only, removing any spaces or special characters
        StringBuilder phoneNumber = new StringBuilder();
        for (char c : processed.toCharArray()) {
            if (Character.isDigit(c)) {
                phoneNumber.append(c);
            }
        }

        return phoneNumber.toString();
    }

    // Method to initiate a phone call
    private String makePhoneCall(String phoneNumber, String timestamp) {
        try {
            // Check for call permission at runtime if targeting Android 6.0+
            // This is a simplified version - you should implement proper permission handling
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            // Check if the device can handle the call intent
            if (callIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(callIntent);
                return "Calling " + phoneNumber;
            } else {
                return "Call feature not available on this device";
            }
        } catch (SecurityException e) {
            return "Call permission not granted";
        } catch (Exception e) {
            return "Error making call: " + e.getMessage();
        }
    }
    private void openInPlayStore(String packageName) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void openFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivity(intent);
    }

    private void openCalendar() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
        startActivity(intent);
    }

    private String capturePhoto(String timestamp) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            return "Camera opened";
        } else {
            return "Camera not available";
        }
    }

    private void searchWeb(String query, String timestamp) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra("query", query);
        startActivity(intent);
    }

    private String getTimeAndDate() {
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        return "Time: " + currentTime + ", Date: " + currentDate;
    }

    private String playMusic(String timestamp) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer = MediaPlayer.create(this, R.raw.poc); // Replace with your actual file
                mediaPlayer.start();
                return "Playing music";
            }
            return "Media player not available";
        } catch (Exception e) {
            return "Error playing music: " + e.getMessage();
        }
    }

    private String stopMusic(String timestamp) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                return "Music stopped";
            }
            return "No music playing";
        } catch (Exception e) {
            return "Error stopping music: " + e.getMessage();
        }
    }

    private String getCurrentTimeAndDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void addCommandToHistory(String entry) {
        commandList.add(entry);
        updateCommandHistory();
    }

    private void updateCommandHistory() {
        StringBuilder history = new StringBuilder();
        for (String command : commandList) {
            history.append(command).append("\n\n");
        }
        commandHistory.setText(history.toString());
    }


}