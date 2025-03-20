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
    private boolean isEnglishToHindi = true;
    private Translator translator;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_translation);

        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        translateButton = findViewById(R.id.translateButton);
        voiceButton = findViewById(R.id.voiceButton);
        clearButton = findViewById(R.id.clear_l);
        swapLanguagesButton = findViewById(R.id.swapLanguagesButton);

        // Set up the default translator for English to Hindi
        setupTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.HINDI);

        translateButton.setOnClickListener(v -> {
            String text = inputText.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter text to translate.", Toast.LENGTH_SHORT).show();
            } else {
                translateText(text);
            }
        });

        clearButton.setOnClickListener(v -> {
            inputText.setText("");
            outputText.setText("");
        });

        swapLanguagesButton.setOnClickListener(v -> {
            isEnglishToHindi = !isEnglishToHindi; // Toggle translation direction
            String sourceLanguage = isEnglishToHindi ? TranslateLanguage.ENGLISH : TranslateLanguage.HINDI;
            String targetLanguage = isEnglishToHindi ? TranslateLanguage.HINDI : TranslateLanguage.ENGLISH;

            setupTranslator(sourceLanguage, targetLanguage);

            inputText.setHint(isEnglishToHindi ? "Enter text in English" : "Enter text in Hindi");
            outputText.setText("");
        });

        voiceButton.setOnClickListener(v -> {
            Toast.makeText(this, "Speak Now", Toast.LENGTH_SHORT).show();
            startVoiceRecognition();
        });
    }

    private void setupTranslator(String sourceLanguage, String targetLanguage) {
        if (translator != null) {
            translator.close();
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build();

        translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> Toast.makeText(this, "Language models downloaded.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to download language models.", Toast.LENGTH_SHORT).show());
    }

    private void translateText(String text) {
        translator.translate(text)
                .addOnSuccessListener(translatedText -> outputText.setText(translatedText))
                .addOnFailureListener(e -> Toast.makeText(this, "Translation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

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

        String languageCode = isEnglishToHindi ? "en-US" : "hi-IN";
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak now");
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (translator != null) {
            translator.close();
        }
    }
}