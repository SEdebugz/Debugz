package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debugz.R;

/**
 * The entry point of the application.
 * Provides a brand introduction and a "Get Started" path.
 */
public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            // Navigate to the main event discovery feed
            Intent intent = new Intent(LandingActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Prevent coming back to landing page with back button
        });
    }
}