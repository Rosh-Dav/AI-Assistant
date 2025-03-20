package com.example.oneai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MathCalculationActivity extends AppCompatActivity {

    private EditText mathExpressionEditText;
    private TextView resultTextView;
    private Button solveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_calculation);

        mathExpressionEditText = findViewById(R.id.inputLayout);
        resultTextView = findViewById(R.id.resultText);
        solveButton = findViewById(R.id.calculateButton);

        TextView textView = findViewById(R.id.documentationText);

        // Load and display formatted HTML text from strings.xml
        String text = getString(R.string.math_operations);
        textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));

        // Solve button click listener
        solveButton.setOnClickListener(v -> {
            String expression = mathExpressionEditText.getText().toString();
            if (!expression.isEmpty()) {
                solveMathExpression(expression);
            } else {
                Toast.makeText(MathCalculationActivity.this, "Please enter a valid expression", Toast.LENGTH_SHORT).show();
            }
        });

        // Help button click listener
        ImageButton helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> openHelpPdf());
    }

    private void solveMathExpression(String expression) {
        // Retrofit setup for Math API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.mathjs.org/v4/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        MathApiService apiService = retrofit.create(MathApiService.class);
        Call<String> call = apiService.solveExpression(expression);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    resultTextView.setText("Result: " + response.body());
                } else {
                    resultTextView.setText("Error: Invalid expression or API issue");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                resultTextView.setText("Error: " + t.getMessage());
            }
        });
    }

    private void openHelpPdf() {
        try {
            // Copy PDF from raw resources to app's files directory
            int resourceId = R.raw.helpmath;
            File outputDir = getFilesDir();
            File outputFile = new File(outputDir, "helpmath.pdf");

            if (!outputFile.exists()) {
                InputStream in = getResources().openRawResource(resourceId);
                FileOutputStream out = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            }

            // Create a content URI for the PDF file
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    "com.example.oneai.fileprovider",
                    outputFile);

            // Launch PDF viewer intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}