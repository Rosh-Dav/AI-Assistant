package com.example.oneai;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Locale;

public class WeatherUpdateActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private EditText cityEditText;
    private TextView weatherDetailsTextView;
    private Button voiceInputButton, fetchWeatherButton;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_update);

        // Initialize views
        cityEditText = findViewById(R.id.cityEditText);
        weatherDetailsTextView = findViewById(R.id.weatherDetailsTextView);
        scrollView = findViewById(R.id.scrollView);
        voiceInputButton = findViewById(R.id.voiceInputButton);
        fetchWeatherButton = findViewById(R.id.fetchWeatherButton);

        // Set click listeners
        voiceInputButton.setOnClickListener(v -> startVoiceRecognition());
        fetchWeatherButton.setOnClickListener(v -> getWeather());
    }

    private void startVoiceRecognition() {
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the city name");

        speechRecognizer.setRecognitionListener(new android.speech.RecognitionListener() {
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
                Log.e("Speech Error", "Error: " + error);
                Toast.makeText(WeatherUpdateActivity.this, "Voice recognition failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    cityEditText.setText(matches.get(0));
                    getWeather();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    private void getWeather() {
        String city = cityEditText.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://wttr.in/" + city + "?format=%C+%t+%w+%h+%P+%m";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    String result = formatWeatherData(city, response);
                    weatherDetailsTextView.setText(result);
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
                },
                error -> {
                    Toast.makeText(WeatherUpdateActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show();
                    Log.e("Weather Error", "Error: " + error.getMessage());
                }
        );

        requestQueue.add(stringRequest);
    }

    private String formatWeatherData(String city, String rawData) {
        String[] weatherDetails = rawData.split(" ");

        // Extract individual weather components
        String condition = weatherDetails[0];
        String temperature = weatherDetails[1];
        String wind = weatherDetails[2];
        String humidity = weatherDetails[3];
        String pressure = weatherDetails[4];
        String moonPhase = weatherDetails[5];

        String conditionSymbol = getConditionSymbol(condition);
        String windSymbol = "🌬";

        StringBuilder weatherData = new StringBuilder();

        weatherData.append("Weather in ").append(city).append(":\n\n");
        weatherData.append(conditionSymbol).append(" Condition:\t").append(condition).append("\n");
        weatherData.append("🌡️ Temperature:\t").append(temperature).append("\n");
        weatherData.append(windSymbol).append(" Wind:\t\t").append(wind).append("\n");
        weatherData.append("💧 Humidity:\t\t").append(humidity).append("\n");
        weatherData.append("📊 Pressure:\t\t").append(pressure).append("\n");
        weatherData.append("🌙 Moon Phase:\t").append(moonPhase);

        return weatherData.toString();
    }

    private String getConditionSymbol(String condition) {
        switch (condition.toLowerCase()) {
            case "clear":
                return "☀";
            case "rain":
                return "🌧";
            case "cloudy":
                return "☁";
            case "snow":
                return "❄";
            default:
                return "🌤";
        }
    }

    private String getMoonPhaseSymbol(String moonPhase) {
        switch (moonPhase.toLowerCase()) {
            case "waxing crescent":
                return "🌒";
            case "full moon":
                return "🌕";
            case "waning crescent":
                return "🌘";
            default:
                return "🌑";
        }
    }
}