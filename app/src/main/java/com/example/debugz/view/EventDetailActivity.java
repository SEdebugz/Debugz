package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
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
import com.google.firebase.firestore.FieldValue;
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
 * calendar integration (US8), student attendee count (US9), and
 * notification + reminder scheduling on RSVP (US7, US11).
 *
 * All writes go through Firestore transactions to guarantee atomicity.
 */
public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EventController eventController;
    private UserSession session;

    // Intent extras
    private String eventId, title, date, time, location, description, price;
    private int capacity;

    // Views
    private TextView tvTitle, tvDate, tvLocation, tvPrice, tvDescription;
    private TextView tvCapacity, tvSpotsLeft, tvAttendeesInfo, tvUpvoteCount;
    private Button btnRSVP, btnUpvote, btnCalendar;

    // Live state fetched from Firestore
    private Event liveEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db             = FirebaseFirestore.getInstance();
        eventController = new EventController();
        session        = UserSession.getInstance(this);

        // ── Extract intent extras ─────────────────────────────────────────
        eventId     = getIntent().getStringExtra("eventId");
        title       = getIntent().getStringExtra("title");
        date        = getIntent().getStringExtra("date");
        time        = getIntent().getStringExtra("time");
        location    = getIntent().getStringExtra("location");
        description = getIntent().getStringExtra("description");
        capacity    = getIntent().getIntExtra("capacity", 0);
        price       = getIntent().getStringExtra("price");

        // ── Bind views ────────────────────────────────────────────────────
        tvTitle        = findViewById(R.id.tvDetailTitle);
        tvDate         = findViewById(R.id.tvDetailDate);
        tvLocation     = findViewById(R.id.tvDetailLocation);
        tvPrice        = findViewById(R.id.tvDetailPrice);
        tvDescription  = findViewById(R.id.tvDetailDescription);
        tvCapacity     = findViewById(R.id.tvDetailCapacity);
        tvSpotsLeft    = findViewById(R.id.tvSpotsLeft);
        tvAttendeesInfo = findViewById(R.id.tvAttendeesInfo);
        tvUpvoteCount  = findViewById(R.id.tvUpvoteCount);
        btnRSVP        = findViewById(R.id.btnRSVP);
        btnUpvote      = findViewById(R.id.btnUpvote);
        btnCalendar    = findViewById(R.id.btnAddToCalendar);

        // ── Static data from intent ───────────────────────────────────────
        tvTitle.setText(title);
        tvDate.setText(date + " at " + time);
        tvLocation.setText(location);
        tvDescription.setText(description);
        String priceDisplay = (price == null || price.isEmpty() || "Free".equalsIgnoreCase(price))
                ? "Price: Free" : "Price: " + price;
        tvPrice.setText(priceDisplay);

        // ── Button listeners ──────────────────────────────────────────────
        btnRSVP.setOnClickListener(v     -> handleRSVP());
        btnUpvote.setOnClickListener(v   -> handleUpvote());
        btnCalendar.setOnClickListener(v -> addToCalendar());

        // ── Live Firestore data ───────────────────────────────────────────
        refreshEventData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshEventData();
    }

    // ── Live data fetch ────────────────────────────────────────────────────

    private void refreshEventData() {
        if (eventId == null || eventId.isEmpty()) return;
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Event event = snapshot.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(snapshot.getId());
                            liveEvent = event;
                            updateCapacityUI(event);
                            updateUpvoteUI(event);
                            updateRsvpButton(event);
                        }
                    }
                });
    }

    private void updateCapacityUI(Event event) {
        int attendees = event.getAttendeeIds() != null ? event.getAttendeeIds().size() : 0;
        int max = event.getMaxCapacity();
        int left = max - attendees;

        tvCapacity.setText("Attending: " + attendees + " / " + max);
        tvSpotsLeft.setText(left > 0 ? left + " spots left" : "FULL");
        tvAttendeesInfo.setText(attendees + " student" + (attendees != 1 ? "s" : "") + " attending");
    }

    private void updateUpvoteUI(Event event) {
        int count = event.getUpvoteCount();
        tvUpvoteCount.setText("♥  " + count + (count == 1 ? " upvote" : " upvotes"));

        String studentId = session.getUserId();
        boolean alreadyUpvoted = event.getUpvotedBy() != null
                && event.getUpvotedBy().contains(studentId);
        btnUpvote.setText(alreadyUpvoted ? "♥ Upvoted" : "Upvote");
        btnUpvote.setAlpha(alreadyUpvoted ? 1.0f : 0.7f);
    }

    private void updateRsvpButton(Event event) {
        String studentId = session.getUserId();
        boolean alreadyRsvpd = event.getAttendeeIds() != null
                && event.getAttendeeIds().contains(studentId);

        if (alreadyRsvpd) {
            btnRSVP.setText("✓ Already RSVP'd");
            btnRSVP.setEnabled(false);
            btnRSVP.setAlpha(0.6f);
        } else {
            int attendees = event.getAttendeeIds() != null ? event.getAttendeeIds().size() : 0;
            boolean isFull = event.getMaxCapacity() > 0
                    && attendees >= event.getMaxCapacity();
            btnRSVP.setText(isFull ? "Event Full" : "RSVP to Event");
            btnRSVP.setEnabled(!isFull);
            btnRSVP.setAlpha(isFull ? 0.5f : 1.0f);
        }
    }

    // ── US4: RSVP (transactional) ──────────────────────────────────────────

    private void handleRSVP() {
        String studentId = session.getUserId();
        if (studentId.isEmpty()) {
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRSVP.setEnabled(false);

        DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(eventRef);

            @SuppressWarnings("unchecked")
            List<String> attendeeIds = (List<String>) snap.get("attendeeIds");
            if (attendeeIds == null) attendeeIds = new ArrayList<>();

            Long maxCap = snap.getLong("maxCapacity");
            long max = maxCap != null ? maxCap : 0;

            if (attendeeIds.contains(studentId)) {
                throw new FirebaseFirestoreException("Already registered",
                        FirebaseFirestoreException.Code.ALREADY_EXISTS);
            }
            if (max > 0 && attendeeIds.size() >= max) {
                throw new FirebaseFirestoreException("Event is at capacity",
                        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED);
            }

            // Add student to attendee list
            attendeeIds.add(studentId);
            transaction.update(eventRef, "attendeeIds", attendeeIds);

            // Write Registration document
            String regId = UUID.randomUUID().toString();
            Registration reg = new Registration(
                    regId, studentId, eventId, "Confirmed", System.currentTimeMillis());
            transaction.set(db.collection("registrations").document(regId), reg);

            return null;
        }).addOnSuccessListener(aVoid -> onRsvpSuccess())
          .addOnFailureListener(e -> {
              btnRSVP.setEnabled(true);
              String msg = "RSVP failed";
              if (e instanceof FirebaseFirestoreException) {
                  FirebaseFirestoreException ffe = (FirebaseFirestoreException) e;
                  if (ffe.getCode() == FirebaseFirestoreException.Code.ALREADY_EXISTS) {
                      msg = "You've already RSVP'd to this event.";
                  } else if (ffe.getCode() == FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED) {
                      msg = "Sorry, this event is full.";
                  }
              }
              Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
              refreshEventData();
          });
    }

    private void onRsvpSuccess() {
        // US7: post an immediate confirmation notification
        NotificationHelper.postRsvpConfirmation(this, title);

        // US11: schedule a reminder notification 24 h before the event
        scheduleReminder();

        // Refresh UI
        refreshEventData();

        // US8: offer to add to calendar via dialog
        new AlertDialog.Builder(this)
                .setTitle("RSVP Confirmed! 🎉")
                .setMessage("You're registered for " + title + ".\nWould you like to add it to your calendar?")
                .setPositiveButton("Add to Calendar", (d, w) -> addToCalendar())
                .setNegativeButton("Not Now", null)
                .show();
    }

    // ── US6: Upvote ────────────────────────────────────────────────────────

    private void handleUpvote() {
        String studentId = session.getUserId();
        if (studentId.isEmpty()) {
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpvote.setEnabled(false);
        eventController.toggleUpvote(eventId, studentId, new EventController.OnUpvoteListener() {
            @Override
            public void onUpvoteToggled(boolean nowUpvoted) {
                btnUpvote.setEnabled(true);
                Toast.makeText(EventDetailActivity.this,
                        nowUpvoted ? "Upvoted!" : "Upvote removed", Toast.LENGTH_SHORT).show();
                refreshEventData();
            }

            @Override
            public void onFailure(Exception e) {
                btnUpvote.setEnabled(true);
                Toast.makeText(EventDetailActivity.this,
                        "Upvote failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── US8: Calendar integration ──────────────────────────────────────────

    private void addToCalendar() {
        long startMs = parseEventDateTimeMs(date, time);
        long endMs   = startMs > 0 ? startMs + (2 * 60 * 60 * 1000L) : 0; // +2 h estimate

        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setData(CalendarContract.Events.CONTENT_URI);
        calIntent.putExtra(CalendarContract.Events.TITLE,          title);
        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION,    description != null ? description : "");
        if (startMs > 0) {
            calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs);
            calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,   endMs);
        }

        if (calIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(calIntent);
        } else {
            Toast.makeText(this, "No calendar app found on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    // ── US11: WorkManager reminder ─────────────────────────────────────────

    private void scheduleReminder() {
        long eventDateMs = parseEventDateTimeMs(date, time);
        if (eventDateMs < 0) return; // unrecognised date format — skip

        long reminderMs = eventDateMs - TimeUnit.HOURS.toMillis(24);
        long delayMs    = reminderMs - System.currentTimeMillis();
        if (delayMs <= 0) return; // event is within 24 h or already past

        Data inputData = new Data.Builder()
                .putString(ReminderWorker.KEY_EVENT_TITLE, title)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(request);
    }

    // ── Date/time parsing ──────────────────────────────────────────────────

    /**
     * Parses the stored date + time strings into epoch milliseconds.
     *
     * @return milliseconds, or -1 if the strings cannot be parsed.
     */
    private long parseEventDateTimeMs(String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) return -1;
        try {
            String combined = dateStr.trim() + " " + timeStr.trim();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US);
            Date parsed = sdf.parse(combined);
            if (parsed != null) {
                // Align to the start of the day to guarantee correct scheduling
                Calendar cal = Calendar.getInstance();
                cal.setTime(parsed);
                return cal.getTimeInMillis();
            }
        } catch (ParseException ignored) {
            // Informal dates like "Tonight" will fall through here — scheduling is skipped
        }
        return -1;
    }
}
