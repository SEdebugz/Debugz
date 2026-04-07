package com.example.debugz.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debugz.R;
import com.example.debugz.models.Event;
import com.example.debugz.models.Registration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.UUID;

/**
 * ROLE: Controller / Transaction Pattern.
 * PURPOSE: Manages the display of detailed event information (US3) and 
 * coordinates the RSVP process (US4). 
 */
public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId, title, date, time, location, description, price;
    private int capacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirebaseFirestore.getInstance();

        // Initial data from Intent
        eventId = getIntent().getStringExtra("eventId");
        title = getIntent().getStringExtra("title");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        location = getIntent().getStringExtra("location");
        description = getIntent().getStringExtra("description");
        capacity = getIntent().getIntExtra("capacity", 0);
        price = getIntent().getStringExtra("price");

        setupViews();
        refreshEventData(); 
    }

    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        Button btnRSVP = findViewById(R.id.btnRSVP);

        tvTitle.setText(title);
        tvDate.setText(date + " at " + time);
        tvLocation.setText(location);
        tvDescription.setText(description);
        tvPrice.setText("Price: " + (price != null ? price : "Free"));

        btnRSVP.setOnClickListener(v -> handleRSVPWithTransaction());
    }

    private void refreshEventData() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event liveEvent = documentSnapshot.toObject(Event.class);
                        if (liveEvent != null) {
                            TextView tvCapacity = findViewById(R.id.tvDetailCapacity);
                            int current = liveEvent.getAttendeeIds().size();
                            tvCapacity.setText("Capacity: " + current + " / " + liveEvent.getMaxCapacity());
                        }
                    }
                });
    }

    private void handleRSVPWithTransaction() {
        String studentId = "demo_student_123";
        String registrationId = eventId + "_" + studentId; 

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            com.google.firebase.firestore.DocumentReference eventRef = db.collection("events").document(eventId);
            com.google.firebase.firestore.DocumentSnapshot eventSnapshot = transaction.get(eventRef);
            
            Event event = eventSnapshot.toObject(Event.class);
            if (event == null) throw new RuntimeException("Event not found");

            if (event.getAttendeeIds().contains(studentId)) {
                throw new RuntimeException("Already RSVP'd");
            }

            if (event.getAttendeeIds().size() >= event.getMaxCapacity()) {
                throw new RuntimeException("Event is full");
            }

            event.addAttendee(studentId);
            transaction.update(eventRef, "attendeeIds", event.getAttendeeIds());

            Registration reg = new Registration(registrationId, studentId, eventId, "Confirmed", System.currentTimeMillis());
            transaction.set(db.collection("registrations").document(registrationId), reg);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "RSVP Successful!", Toast.LENGTH_SHORT).show();
            refreshEventData();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}