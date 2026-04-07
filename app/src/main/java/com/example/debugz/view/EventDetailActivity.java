package com.example.debugz.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debugz.R;
import com.example.debugz.models.Registration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.UUID;

/**
 * Shows full event details and handles RSVP creation for a selected event.
 * This screen is the detail view in the browse-to-RSVP flow and writes the registration
 * record that links the demo student to the chosen event.
 * Outstanding issues: the RSVP flow still uses a hardcoded student ID and does not block
 * duplicate registrations or over-capacity submissions.
 */
public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId, title, date, time, location, description;
    private int capacity;
    private double ticketPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirebaseFirestore.getInstance();

        // Get data from Intent
        eventId = getIntent().getStringExtra("eventId");
        title = getIntent().getStringExtra("title");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        location = getIntent().getStringExtra("location");
        description = getIntent().getStringExtra("description");
        capacity = getIntent().getIntExtra("capacity", 0);
        ticketPrice = getIntent().getDoubleExtra("ticketPrice", 0.0); // Add this pull

        // Initialize Views
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvPrice = findViewById(R.id.tvDetailPrice); // Link the new view
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvCapacity = findViewById(R.id.tvDetailCapacity);
        Button btnRSVP = findViewById(R.id.btnRSVP);

        // Set Data
        tvTitle.setText(title);
        tvDate.setText(date + " at " + time);
        tvLocation.setText(location);

        // Format the price nicely
        String priceString = ticketPrice == 0.0 ? "Price: Free" : "Price: Rs. " + ticketPrice;
        tvPrice.setText(priceString); // Set the price text

        tvDescription.setText(description);
        tvCapacity.setText("Max Capacity: " + capacity);

        btnRSVP.setOnClickListener(v -> handleRSVP());
    }

    /**
     * Handles the RSVP logic by writing a new Registration to Firestore.
     */
    private void handleRSVP() {
        // For prototype demo, we use a hardcoded student ID
        String studentId = "demo_student_123";
        String registrationId = UUID.randomUUID().toString();

        Registration registration = new Registration(
                registrationId,
                studentId,
                eventId,
                "Confirmed",
                System.currentTimeMillis()
        );

        db.collection("registrations")
                .document(registrationId)
                .set(registration)
                .addOnSuccessListener(aVoid -> {
                    // Add student to the event's attendee array so capacity updates
                    db.collection("events").document(eventId)
                            .update("attendeeIds", FieldValue.arrayUnion(studentId))
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "RSVP Successful!", Toast.LENGTH_SHORT).show();
                                finish(); // Go back to feed
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Capacity update failed.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "RSVP Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
