package com.example.debugz.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.debugz.NotificationHelper;
import com.example.debugz.R;
import com.example.debugz.ReminderWorker;
import com.example.debugz.UserSession;
import com.example.debugz.controller.EventController;
import com.example.debugz.controller.NotificationController;
import com.example.debugz.models.Event;
import com.example.debugz.models.NotificationModel;
import com.example.debugz.models.Registration;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * ROLE: View (Activity).
 * PURPOSE: Shows full event details and handles RSVP, upvote toggle, and reminders.
 */
public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private UserSession session;
    private EventController eventController;
    private NotificationController notificationController;

    private String eventId, title, date, time, location, description, price;
    private TextView tvTitle, tvDescription, tvCapacity, tvSpotsLeft, tvUpvoteSummary;
    private Chip chipDate, chipTime, chipLocation, chipPrice;
    private LinearProgressIndicator pbCapacity;
    private Button btnRSVP, btnCancelRSVP, btnCalendar, btnUpvote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirebaseFirestore.getInstance();
        session = UserSession.getInstance(this);
        eventController = new EventController();
        notificationController = new NotificationController();

        // Extract Intent extras
        eventId = getIntent().getStringExtra("eventId");
        title = getIntent().getStringExtra("title");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        location = getIntent().getStringExtra("location");
        description = getIntent().getStringExtra("description");
        price = getIntent().getStringExtra("price");

        setupToolbar();
        initializeViews();
        
        // Set initial static data
        tvTitle.setText(title);
        tvDescription.setText(description);
        chipDate.setText(date != null ? date : "Date TBD");
        chipTime.setText(time != null ? time : "Time TBD");
        chipLocation.setText(location != null ? location : "Location TBD");
        chipPrice.setText(price != null ? price : "Free");

        // Set listeners
        btnRSVP.setOnClickListener(v -> handleRSVP());
        btnCancelRSVP.setOnClickListener(v -> handleCancelRSVP());
        btnCalendar.setOnClickListener(v -> addToCalendar());
        btnUpvote.setOnClickListener(v -> toggleUpvote());

        refreshEventData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        if (collapsingToolbar != null) collapsingToolbar.setTitleEnabled(false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvCapacity = findViewById(R.id.tvCapacityInfo);
        tvSpotsLeft = findViewById(R.id.tvSpotsLeft);
        tvUpvoteSummary = findViewById(R.id.tvUpvoteCountDisplay);
        pbCapacity = findViewById(R.id.pbCapacity);
        
        chipDate = findViewById(R.id.chipDetailDate);
        chipTime = findViewById(R.id.chipDetailTime);
        chipLocation = findViewById(R.id.chipDetailLocation);
        chipPrice = findViewById(R.id.chipDetailPrice);

        btnRSVP = findViewById(R.id.btnRSVP);
        btnCancelRSVP = findViewById(R.id.btnCancelRSVP);
        btnCalendar = findViewById(R.id.btnAddToCalendar);
        btnUpvote = findViewById(R.id.btnUpvote);
    }

    private void refreshEventData() {
        if (eventId == null) return;
        db.collection("events").document(eventId).get().addOnSuccessListener(snapshot -> {
            Event event = snapshot.toObject(Event.class);
            if (event != null) updateUI(event);
        });
    }

    private void updateUI(Event event) {
        int current = event.getAttendeeIds().size();
        int max = event.getMaxCapacity();
        tvCapacity.setText(current + " / " + max);
        
        int left = Math.max(0, max - current);
        tvSpotsLeft.setText(left + " spots left");
        
        int percent = (max > 0) ? (int) (((float) current / max) * 100) : 0;
        pbCapacity.setProgress(percent);

        // Update Upvote state
        tvUpvoteSummary.setText(event.getUpvoteCount() + " people have upvoted this event");
        boolean isUpvoted = event.getUpvotedBy().contains(session.getUserId());
        btnUpvote.setText(isUpvoted ? "UPVOTED" : "UPVOTE");
        btnUpvote.setAlpha(isUpvoted ? 1.0f : 0.7f);

        // Update RSVP state
        boolean alreadyRsvpd = event.getAttendeeIds().contains(session.getUserId());
        btnRSVP.setVisibility(alreadyRsvpd ? View.GONE : View.VISIBLE);
        btnCancelRSVP.setVisibility(alreadyRsvpd ? View.VISIBLE : View.GONE);
        
        if (!alreadyRsvpd) {
            btnRSVP.setEnabled(current < max);
            btnRSVP.setText(current < max ? "RSVP NOW" : "EVENT FULL");
        }
    }

    private void handleRSVP() {
        String studentId = session.getUserId();
        DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(eventRef);
            Event event = snap.toObject(Event.class);
            if (event == null || event.getAttendeeIds().size() >= event.getMaxCapacity()) throw new RuntimeException("Full");
            
            event.addAttendee(studentId);
            transaction.update(eventRef, "attendeeIds", event.getAttendeeIds());
            
            String regId = eventId + "_" + studentId;
            Registration reg = new Registration(regId, studentId, eventId, "Confirmed", System.currentTimeMillis());
            transaction.set(db.collection("registrations").document(regId), reg);
            
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "RSVP Successful!", Toast.LENGTH_SHORT).show();
            NotificationHelper.postRsvpConfirmation(this, title);
            refreshEventData();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleCancelRSVP() {
        String studentId = session.getUserId();
        DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(eventRef);
            Event event = snap.toObject(Event.class);
            if (event != null) {
                event.removeAttendee(studentId);
                transaction.update(eventRef, "attendeeIds", event.getAttendeeIds());
                transaction.delete(db.collection("registrations").document(eventId + "_" + studentId));
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "RSVP Cancelled", Toast.LENGTH_SHORT).show();
            refreshEventData();
        });
    }

    private void toggleUpvote() {
        eventController.toggleUpvote(eventId, session.getUserId(), new EventController.OnUpvoteListener() {
            @Override public void onUpvoteToggled(boolean nowUpvoted) { refreshEventData(); }
            @Override public void onFailure(Exception e) { Toast.makeText(EventDetailActivity.this, "Action failed", Toast.LENGTH_SHORT).show(); }
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