package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.debugz.NotificationHelper;
import com.example.debugz.R;
import com.example.debugz.ReminderWorker;
import com.example.debugz.UserSession;
import com.example.debugz.controller.EventController;
import com.example.debugz.models.Event;
import com.example.debugz.models.Registration;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Shows full event details and handles RSVP (US3, US4, US5), upvote toggle (US6),
 * calendar integration (US8), and Cancel RSVP.
 * 
 * ROLE: Controller / Transaction Pattern.
 */
public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private UserSession session;
    private String eventId, title, date, time, location, description, price;
    private TextView tvTitle, tvDate, tvLocation, tvPrice, tvDescription, tvCapacity, tvSpotsLeft;
    private Button btnRSVP, btnCancelRSVP, btnCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirebaseFirestore.getInstance();
        session = UserSession.getInstance(this);

        eventId = getIntent().getStringExtra("eventId");
        title = getIntent().getStringExtra("title");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        location = getIntent().getStringExtra("location");
        description = getIntent().getStringExtra("description");
        price = getIntent().getStringExtra("price");

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDate = findViewById(R.id.tvDetailDate);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvCapacity = findViewById(R.id.tvDetailCapacity);
        tvSpotsLeft = findViewById(R.id.tvSpotsLeft);
        btnRSVP = findViewById(R.id.btnRSVP);
        btnCancelRSVP = findViewById(R.id.btnCancelRSVP);
        btnCalendar = findViewById(R.id.btnAddToCalendar);

        tvTitle.setText(title);
        tvDate.setText(date + " at " + time);
        tvLocation.setText(location);
        tvDescription.setText(description);
        tvPrice.setText("Price: " + (price != null ? price : "Free"));

        btnRSVP.setOnClickListener(v -> handleRSVP());
        btnCancelRSVP.setOnClickListener(v -> handleCancelRSVP());
        btnCalendar.setOnClickListener(v -> addToCalendar());

        refreshEventData();
    }

    private void refreshEventData() {
        db.collection("events").document(eventId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Event event = snapshot.toObject(Event.class);
                if (event != null) {
                    updateUI(event);
                }
            }
        });
    }

    private void updateUI(Event event) {
        int attendees = event.getAttendeeIds().size();
        tvCapacity.setText("Attending: " + attendees + " / " + event.getMaxCapacity());
        tvSpotsLeft.setText((event.getMaxCapacity() - attendees) + " spots left");

        boolean alreadyRsvpd = event.getAttendeeIds().contains(session.getUserId());
        if (alreadyRsvpd) {
            btnRSVP.setVisibility(View.GONE);
            btnCancelRSVP.setVisibility(View.VISIBLE);
        } else {
            btnRSVP.setVisibility(View.VISIBLE);
            btnCancelRSVP.setVisibility(View.GONE);
            btnRSVP.setEnabled(attendees < event.getMaxCapacity());
            if (attendees >= event.getMaxCapacity()) btnRSVP.setText("Event Full");
        }
    }

    private void handleRSVP() {
        String studentId = session.getUserId();
        String registrationId = eventId + "_" + studentId;
        DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(eventRef);
            Event event = snap.toObject(Event.class);
            if (event.getAttendeeIds().size() >= event.getMaxCapacity()) throw new RuntimeException("Full");
            
            event.addAttendee(studentId);
            transaction.update(eventRef, "attendeeIds", event.getAttendeeIds());
            Registration reg = new Registration(registrationId, studentId, eventId, "Confirmed", System.currentTimeMillis());
            transaction.set(db.collection("registrations").document(registrationId), reg);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "RSVP Successful!", Toast.LENGTH_SHORT).show();
            NotificationHelper.postRsvpConfirmation(this, title);
            refreshEventData();
        });
    }

    private void handleCancelRSVP() {
        String studentId = session.getUserId();
        String registrationId = eventId + "_" + studentId;
        DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(eventRef);
            Event event = snap.toObject(Event.class);
            event.removeAttendee(studentId);
            transaction.update(eventRef, "attendeeIds", event.getAttendeeIds());
            transaction.delete(db.collection("registrations").document(registrationId));
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "RSVP Cancelled", Toast.LENGTH_SHORT).show();
            refreshEventData();
        });
    }

    private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .putExtra(CalendarContract.Events.DESCRIPTION, description);
        startActivity(intent);
    }
}