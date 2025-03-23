package com.example.oneai;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private static final int PERMISSION_REQUEST_CODE = 100;

    private String pendingNoteTimestamp;
    private TextView commandHistory;
    private ArrayList<String> commandList;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        commandHistory = findViewById(R.id.commandHistory);
        commandList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        initializeButtons();
        requestPermissions();
    }

    private void requestPermissions() {
        String[] permissions = new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.RECORD_AUDIO,
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void initializeButtons() {
        findViewById(R.id.tts_button).setOnClickListener(view -> openActivity(TTSActivity.class));
        findViewById(R.id.news_button).setOnClickListener(view -> openActivity(ChatBot.class));
        findViewById(R.id.math_calculation_button).setOnClickListener(view -> openActivity(MathCalculationActivity.class));
        findViewById(R.id.language_translation_button).setOnClickListener(view -> openActivity(LanguageTranslationActivity.class));
        findViewById(R.id.send_sms_button).setOnClickListener(view -> openActivity(SendSMSActivity.class));
        findViewById(R.id.send_email_button).setOnClickListener(view -> openActivity(SendEmailActivity.class));
        findViewById(R.id.weather_update_button).setOnClickListener(view -> openActivity(WeatherUpdateActivity.class));
        findViewById(R.id.imgViewOneAI).setOnClickListener(view -> openActivity(AboutAI.class));
        findViewById(R.id.voice_command_button).setOnClickListener(view -> startVoiceRecognition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(MainActivity.this, activityClass));
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (results == null || results.isEmpty()) return;

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            String command = results.get(0).toLowerCase();
            handleVoiceCommand(command, getCurrentTimeAndDate());
        } else if (requestCode == REQUEST_CODE_NOTE_CONTENT) {
            handleNoteContent(results.get(0));
        }
    }

    private void handleNoteContent(String noteContent) {
        File savedNote = saveNote(noteContent, pendingNoteTimestamp);
        String resultMessage = savedNote != null
                ? "Note saved successfully: " + savedNote.getName()
                : "Failed to save note";

        addCommandToHistory("Note taken -> " + resultMessage + " [" + pendingNoteTimestamp + "]");
    }

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
            String phoneNumber = extractPhoneNumber(command);
            response = (phoneNumber != null && !phoneNumber.isEmpty())
                    ? makePhoneCall(phoneNumber, timestamp)
                    : "Could not identify a valid phone number";
        } else if (command.contains("open files")) {
            openFiles();
            response = "Opening Files";
        } else if (command.contains("open amazon")) {
            openAppWithPackage("in.amazon.mShop.android.shopping", "https://www.amazon.in");
            response = "Opening Amazon";
        } else if (command.contains("take note")) {
            startNoteCapture(timestamp);
            response = "Ready to take your note. Please speak";
        } else {
            response = "Command not recognized.";
        }

        addCommandToHistory(command + " -> " + response + " [" + timestamp + "]");
    }

    private void startNoteCapture(String timestamp) {
        pendingNoteTimestamp = timestamp;

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

    private File saveNote(String noteContent, String timestamp) {
        try {
            String filename = "Note_" + timestamp.replace(" ", "_").replace(":", "-") + ".txt";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File noteFile = new File(downloadsDir, filename);
            FileWriter writer = new FileWriter(noteFile);
            writer.write(noteContent);
            writer.close();

            return noteFile;
        } catch (IOException e) {
            return null;
        }
    }

    private void openAppWithPackage(String packageName, String uriString) {
        try {
            Intent intent;
            if (uriString != null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                intent.setPackage(packageName);
            } else {
                intent = getPackageManager().getLaunchIntentForPackage(packageName);
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

    // Extract numbers from command for phone calls
    private String extractPhoneNumber(String command) {
        String processed = command.replace("call", "").trim();
        StringBuilder phoneNumber = new StringBuilder();

        for (char c : processed.toCharArray()) {
            if (Character.isDigit(c)) {
                phoneNumber.append(c);
            }
        }

        return phoneNumber.toString();
    }

    private String makePhoneCall(String phoneNumber, String timestamp) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

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
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
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
                mediaPlayer = MediaPlayer.create(this, R.raw.poc);
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
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
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