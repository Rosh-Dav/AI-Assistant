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
                    getWeather(); // Automatically fetch weather after voice input
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

        // wttr.in API URL with detailed weather data format
        String url = "https://wttr.in/" + city + "?format=%C+%t+%w+%h+%P+%m";
        // %C = Condition, %t = Temperature, %w = Wind, %h = Humidity, %P = Pressure, %m = Moon phase

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Format the data nicely with symbols
                        String result = formatWeatherData(city, response);
                        weatherDetailsTextView.setText(result);
                        scrollView.fullScroll(ScrollView.FOCUS_UP); // Scroll to the top
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(WeatherUpdateActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show();
                        Log.e("Weather Error", "Error: " + error.getMessage());
                    }
                }
        );

        requestQueue.add(stringRequest);
    }

    private String formatWeatherData(String city, String rawData) {
        // Split the raw data received from API
        String[] weatherDetails = rawData.split(" ");

        // Extract individual weather components
        String condition = weatherDetails[0];  // Weather condition
        String temperature = weatherDetails[1]; // Temperature
        String wind = weatherDetails[2]; // Wind speed and direction
        String humidity = weatherDetails[3]; // Humidity
        String pressure = weatherDetails[4]; // Pressure
        String moonPhase = weatherDetails[5]; // Moon phase

        // Add weather symbols
        String conditionSymbol = getConditionSymbol(condition);
        String windSymbol = "🌬";  // Wind symbol

        // Use StringBuilder for better string concatenation
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
                return "☀";  // Sun for clear weather
            case "rain":
                return "🌧";  // Cloud with rain for rainy weather
            case "cloudy":
                return "☁";  // Cloud for cloudy weather
            case "snow":
                return "❄";  // Snowflake for snowy weather
            default:
                return "🌤";  // Default symbol for other conditions
        }
    }

    private String getMoonPhaseSymbol(String moonPhase) {
        switch (moonPhase.toLowerCase()) {
            case "waxing crescent":
                return "🌒";  // Waxing Crescent moon phase
            case "full moon":
                return "🌕";  // Full moon phase
            case "waning crescent":
                return "🌘";  // Waning Crescent moon phase
            default:
                return "🌑";  // Default symbol for other moon phases
        }
    }
}