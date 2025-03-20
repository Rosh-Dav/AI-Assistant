package com.example.oneai;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class TTSActivity extends AppCompatActivity {

    private EditText transcribeArea;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsactivity);

        initializeViews();
        setupSpeechRecognition();
        setupButtonListeners();
    }

    private void initializeViews() {
        transcribeArea = findViewById(R.id.transcribe_area);
        Button listenButton = findViewById(R.id.listen_button);
        Button stopButton = findViewById(R.id.stop_button);
        Button clearButton = findViewById(R.id.clear_button);

        listenButton.setOnClickListener(v -> startListening());
        stopButton.setOnClickListener(v -> stopListening());
        clearButton.setOnClickListener(v -> transcribeArea.setText(""));
    }

    private void setupSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        speechRecognizer.setRecognitionListener(createRecognitionListener());
    }

    private RecognitionListener createRecognitionListener() {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(TTSActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Toast.makeText(TTSActivity.this, "Finished listening.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error) {
                Toast.makeText(TTSActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                // Process speech recognition results
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String newText = matches.get(0);
                    String existingText = transcribeArea.getText().toString();
                    transcribeArea.setText(existingText.isEmpty() ? newText : existingText + "\n" + newText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        };
    }

    private void setupButtonListeners() {
        // Now handled in initializeViews for better organization
    }

    private void startListening() {
        if (speechRecognizer != null) {
            speechRecognizer.startListening(recognizerIntent);
        }
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}