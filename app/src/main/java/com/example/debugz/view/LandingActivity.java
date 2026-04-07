package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debugz.R;

/**
 * Provides the app's landing screen and initial navigation into the event feed.
 * The activity acts as the branded entry point before handing control to the discovery
 * experience when the user presses the call-to-action button.
 * Outstanding issues: this screen currently offers only a single navigation path and does
 * not yet branch by role or authentication state.
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
