package com.example.oneai;

import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.util.ArrayList;

public class LanguageTranslationActivity extends AppCompatActivity {

    private EditText inputText;
    private TextView outputText;
    private Button translateButton, voiceButton, clearButton;
    private ImageButton swapLanguagesButton;
    private boolean isEnglishToHindi = true; // Default direction: English to Hindi
    private Translator translator;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_translation);

        // Initialize views
        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        translateButton = findViewById(R.id.translateButton);
        voiceButton = findViewById(R.id.voiceButton);
        clearButton = findViewById(R.id.clear_l);
        swapLanguagesButton = findViewById(R.id.swapLanguagesButton);

        // Set up the default translator
        setupTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.HINDI);

        // Translate button click listener
        translateButton.setOnClickListener(v -> {
            String text = inputText.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter text to translate.", Toast.LENGTH_SHORT).show();
            } else {
                translateText(text);
            }
        });

        clearButton.setOnClickListener(v -> {
            inputText.setText(""); // Clear EditText
            outputText.setText(""); // Clear Output Text (optional)
        });

        // Swap languages button click listener
        swapLanguagesButton.setOnClickListener(v -> {
            isEnglishToHindi = !isEnglishToHindi; // Toggle translation direction
            String sourceLanguage = isEnglishToHindi ? TranslateLanguage.ENGLISH : TranslateLanguage.HINDI;
            String targetLanguage = isEnglishToHindi ? TranslateLanguage.HINDI : TranslateLanguage.ENGLISH;

            setupTranslator(sourceLanguage, targetLanguage);

            // Update hint and clear output
            inputText.setHint(isEnglishToHindi ? "Enter text in English" : "Enter text in Hindi");
            outputText.setText("");
        });

        // Voice input button to trigger Hindi voice recognition
        voiceButton.setOnClickListener(v -> {
            Toast.makeText(this, "Speak Now", Toast.LENGTH_SHORT).show();
            startVoiceRecognition(); // Start listening for Hindi speech
        });
    }

    // Set up the translator with the given source and target languages
    private void setupTranslator(String sourceLanguage, String targetLanguage) {
        if (translator != null) {
            translator.close(); // Close the previous translator
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build();

        translator = Translation.getClient(options);

        // Download the language model if needed
        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> Toast.makeText(this, "Language models downloaded.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to download language models.", Toast.LENGTH_SHORT).show());
    }

    // Translate the input text
    private void translateText(String text) {
        translator.translate(text)
                .addOnSuccessListener(translatedText -> outputText.setText(translatedText))
                .addOnFailureListener(e -> Toast.makeText(this, "Translation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Start speech recognition for Hindi voice input
// Start speech recognition based on selected language
    private void startVoiceRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                Toast.makeText(LanguageTranslationActivity.this, "Error in speech recognition", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> recognizedWords = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (recognizedWords != null && !recognizedWords.isEmpty()) {
                    inputText.setText(recognizedWords.get(0)); // Show recognized text in input field
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Set recognition language dynamically
        String languageCode = isEnglishToHindi ? "en-US" : "hi-IN"; // English to Hindi -> English input, Hindi to English -> Hindi input
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak now");
        speechRecognizer.startListening(recognizerIntent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy(); // Release resources
        }
        if (translator != null) {
            translator.close(); // Release resources
        }
    }
}
