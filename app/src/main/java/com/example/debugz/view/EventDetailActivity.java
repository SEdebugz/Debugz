package com.example.debugz.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
 * Shows full event details and handles RSVP, upvote toggle, and reminders.
 * ROLE: View (Activity).
 */
public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private UserSession session;
    private EventController eventController;
    private NotificationController notificationController;

    private String eventId, title, date, time, location, description, price, category;
    private TextView tvTitle, tvDescription, tvCapacity, tvSpotsLeft, tvUpvoteCount;
    private Chip chipDate, chipTime, chipLocation, chipPrice;
    private LinearProgressIndicator pbCapacity;
    private Button btnRSVP, btnCancelRSVP, btnCalendar;
    private View btnUpvote;
    private ImageView ivUpvoteHeart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirebaseFirestore.getInstance();
        session = UserSession.getInstance(this);
        eventController = new EventController();
        notificationController = new NotificationController();

        eventId = getIntent().getStringExtra("eventId");
        title = getIntent().getStringExtra("title");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        location = getIntent().getStringExtra("location");
        description = getIntent().getStringExtra("description");
        price = getIntent().getStringExtra("price");
        category = getIntent().getStringExtra("category");

        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        
        // FIX: Disable the default CollapsingToolbar title to prevent overlap with the event title
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitleEnabled(false);
            collapsingToolbar.setTitle("");
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Definitely hide Activity title
            getSupportActionBar().setTitle(""); 
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvCapacity = findViewById(R.id.tvCapacityInfo);
        tvSpotsLeft = findViewById(R.id.tvSpotsLeft);
        tvUpvoteCount = findViewById(R.id.tvUpvoteCount);
        pbCapacity = findViewById(R.id.pbCapacity);
        
        chipDate = findViewById(R.id.chipDetailDate);
        chipTime = findViewById(R.id.chipDetailTime);
        chipLocation = findViewById(R.id.chipDetailLocation);
        chipPrice = findViewById(R.id.chipDetailPrice);

        btnRSVP = findViewById(R.id.btnRSVP);
        btnCancelRSVP = findViewById(R.id.btnCancelRSVP);
        btnCalendar = findViewById(R.id.btnAddToCalendar);
        btnUpvote = findViewById(R.id.btnUpvote);
        ivUpvoteHeart = findViewById(R.id.ivUpvoteHeart);

        tvTitle.setText(title);
        tvDescription.setText(description);
        chipDate.setText(date);
        chipTime.setText(time);
        chipLocation.setText(location);
        chipPrice.setText(price != null ? price : "Free");

        btnRSVP.setOnClickListener(v -> handleRSVP());
        btnCancelRSVP.setOnClickListener(v -> handleCancelRSVP());
        btnCalendar.setOnClickListener(v -> addToCalendar());
        btnUpvote.setOnClickListener(v -> toggleUpvote());

        refreshEventData();
    }

    private void refreshEventData() {
        db.collection("events").document(eventId).get().addOnSuccessListener(snapshot -> {
            Event event = snapshot.toObject(Event.class);
            if (event != null) updateUI(event);
        });
    }

    private void updateUI(Event event) {
        int current = event.getAttendeeIds().size();
        int max = event.getMaxCapacity();
        tvCapacity.setText(current + " / " + max);
        
        int left = max - current;
        tvSpotsLeft.setText(left + " spots left");
        
        int percent = (max > 0) ? (int) (((float) current / max) * 100) : 0;
        pbCapacity.setProgress(percent);

        int color = Color.parseColor("#27AE60"); // Green
        if (left <= (max * 0.1)) color = Color.parseColor("#E74C3C"); // Red
        else if (left <= (max * 0.2)) color = Color.parseColor("#F39C12"); // Amber
        
        tvSpotsLeft.setTextColor(color);
        pbCapacity.setIndicatorColor(color);

        tvUpvoteCount.setText(String.valueOf(event.getUpvoteCount()));
        boolean isUpvoted = event.getUpvotedBy().contains(session.getUserId());
        ivUpvoteHeart.setColorFilter(isUpvoted ? Color.parseColor("#E74C3C") : Color.GRAY);

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
        String studentName = session.getUserName();
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
            
            return event;
        }).addOnSuccessListener(event -> {
            Toast.makeText(this, "RSVP Successful!", Toast.LENGTH_SHORT).show();
            triggerRsvpNotificationsBatch(event, studentId, studentName);
            scheduleReminder(event);
            refreshEventData();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Optimized: send all notifications in a single batch to stop UI lag. */
    private void triggerRsvpNotificationsBatch(Event event, String studentId, String studentName) {
        List<NotificationModel> batch = new ArrayList<>();

        // 1. My RSVP
        batch.add(new NotificationModel(null, studentId, "RSVP Confirmed 🎉", 
                "You're going to " + event.getTitle() + "!", "RSVP_CONFIRMED", eventId, System.currentTimeMillis(), false));
        NotificationHelper.postRsvpConfirmation(this, event.getTitle());

        // 2. Notify friends (only those who are actual mutual friends)
        db.collection("accounts").whereArrayContains("friendIds", studentId).get().addOnSuccessListener(snapshots -> {
            for (QueryDocumentSnapshot doc : snapshots) {
                batch.add(new NotificationModel(null, doc.getId(), "Friend Activity", 
                        studentName + " is going to " + event.getTitle() + "!", "FRIEND_RSVP", eventId, System.currentTimeMillis(), false));
            }

            if (event.getAttendeeIds().size() >= (event.getMaxCapacity() * 0.8)) {
                for (String upvoterId : event.getUpvotedBy()) {
                    batch.add(new NotificationModel(null, upvoterId, "Almost Full! ⚠️", 
                            event.getTitle() + " is nearly at capacity. RSVP now!", "CAPACITY_LOW", eventId, System.currentTimeMillis(), false));
                }
            }
            
            notificationController.createNotificationsBatch(batch);
        });
    }

    private void scheduleReminder(Event event) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US);
            Date eventDate = sdf.parse(event.getDate() + " " + event.getTime());
            if (eventDate == null) return;

            long delay = eventDate.getTime() - System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);
            if (delay > 0) {
                Data data = new Data.Builder().putString(ReminderWorker.KEY_EVENT_TITLE, event.getTitle()).build();
                OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .build();
                WorkManager.getInstance(this).enqueue(request);
            }
        } catch (Exception ignored) {}
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
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.ALL_DAY, false);
        startActivity(intent);
    }
}
