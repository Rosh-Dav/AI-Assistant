package com.example.oneai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.datatransport.backend.cct.BuildConfig;

public class AboutAI extends AppCompatActivity {

    private static final String GITHUB_URL = "https://github.com/Rosh-Dav/AI-Assistant";
    private static final String DEVELOPER_EMAIL = "yadavisroshan0007@gmail.com";
    private static final String ISSUES_URL = "https://github.com/Rosh-Dav/AI-Assistant";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_ai);

        TextView appVersionTextView = findViewById(R.id.appVersion);
        appVersionTextView.setText("Version " + BuildConfig.VERSION_NAME);

        TextView developerNameTextView = findViewById(R.id.developerName);
        developerNameTextView.setText("Roshan and Team");

        TextView developerInfoTextView = findViewById(R.id.developerInfo);
        developerInfoTextView.setText("Android Developer");

        setLinkClickListeners();
    }

    private void setLinkClickListeners() {
        LinearLayout githubLink = findViewById(R.id.githubLink);
        githubLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(GITHUB_URL);
            }
        });

        LinearLayout emailLink = findViewById(R.id.emailLink);
        emailLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        LinearLayout issuesLink = findViewById(R.id.issuesLink);
        issuesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(ISSUES_URL);
            }
        });
    }


    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + DEVELOPER_EMAIL));
            intent.putExtra(Intent.EXTRA_SUBJECT, "OneAI App Feedback");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}