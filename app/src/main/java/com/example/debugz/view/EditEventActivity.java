package com.example.debugz.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.controller.EventController;
import com.example.debugz.models.Event;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;

/**
 * Form screen used by the organizer to create a new event or edit an existing one.
 * When launched with an "eventId" extra, the form pre-fills with existing data (edit mode).
 * When launched without it, all fields are blank (create mode).
 *
 * Covers US12 (edit event details) and US13 (set capacity / ticket limits).
 * Events are saved to the Firestore "events" collection so they become visible to all users.
 */
public class EditEventActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etLocation, etDate, etTime, etCapacity, etPrice;
    private Button btnSave;
    private TextView tvHeader;
    private EventController eventController;

    private String existingEventId;  // non-null means edit mode; null means create mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        eventController = new EventController();

        tvHeader = findViewById(R.id.tvEditEventHeader);
        etTitle = findViewById(R.id.etEventTitle);
        etDescription = findViewById(R.id.etEventDescription);
        etLocation = findViewById(R.id.etEventLocation);
        etDate = findViewById(R.id.etEventDate);
        etTime = findViewById(R.id.etEventTime);
        etCapacity = findViewById(R.id.etEventCapacity);
        etPrice = findViewById(R.id.etEventPrice);
        btnSave = findViewById(R.id.btnSaveEvent);

        // Determine mode from Intent extras
        existingEventId = getIntent().getStringExtra("eventId");
        boolean isEditMode = existingEventId != null && !existingEventId.isEmpty();

        if (isEditMode) {
            tvHeader.setText("Edit Event");
            btnSave.setText("Save Changes");
            // Pre-fill form fields
            etTitle.setText(getIntent().getStringExtra("title"));
            etDescription.setText(getIntent().getStringExtra("description"));
            etLocation.setText(getIntent().getStringExtra("location"));
            etDate.setText(getIntent().getStringExtra("date"));
            etTime.setText(getIntent().getStringExtra("time"));
            etCapacity.setText(String.valueOf(getIntent().getIntExtra("maxCapacity", 0)));
            etPrice.setText(getIntent().getStringExtra("price"));
        }

        btnSave.setOnClickListener(v -> saveEvent(isEditMode));
    }

    private void saveEvent(boolean isEditMode) {
        String title = getText(etTitle);
        String description = getText(etDescription);
        String location = getText(etLocation);
        String date = getText(etDate);
        String time = getText(etTime);
        String capacityStr = getText(etCapacity);
        String price = getText(etPrice);

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(date)) {
            etDate.setError("Date is required");
            return;
        }

        int maxCapacity = 0;
        if (!TextUtils.isEmpty(capacityStr)) {
            try {
                maxCapacity = Integer.parseInt(capacityStr);
            } catch (NumberFormatException e) {
                etCapacity.setError("Enter a valid number");
                return;
            }
        }

        String eventId = isEditMode ? existingEventId : UUID.randomUUID().toString();
        Event event = new Event(
                eventId,
                title,
                description.isEmpty() ? "" : description,
                location.isEmpty() ? "" : location,
                date,
                time.isEmpty() ? "" : time,
                UserSession.getInstance(this).getUserId(),  // real organizer ID from session
                maxCapacity,
                price.isEmpty() ? "Free" : price
        );

        btnSave.setEnabled(false);

        EventController.OnEventOperationListener listener = new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                String msg = isEditMode ? "Event updated!" : "Event created!";
                Toast.makeText(EditEventActivity.this, msg, Toast.LENGTH_SHORT).show();
                finish(); // Return to organizer dashboard
            }

            @Override
            public void onFailure(Exception e) {
                btnSave.setEnabled(true);
                Toast.makeText(EditEventActivity.this,
                        "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (isEditMode) {
            eventController.updateEvent(event, listener);
        } else {
            eventController.createEvent(event, listener);
        }
    }

    /** Safely reads non-null trimmed text from a TextInputEditText. */
    private String getText(TextInputEditText et) {
        CharSequence text = et.getText();
        return text != null ? text.toString().trim() : "";
    }
}

