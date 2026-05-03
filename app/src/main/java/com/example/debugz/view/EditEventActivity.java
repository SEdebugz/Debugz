package com.example.debugz.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
 * Updated to include Category selection (US2).
 */
public class EditEventActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etLocation, etDate, etTime, etCapacity, etPrice;
    private AutoCompleteTextView actvCategory;
    private Button btnSave;
    private TextView tvHeader;
    private EventController eventController;

    private String existingEventId;

    private static final String[] CATEGORIES = {"Sports", "Talk", "Performance", "Club", "Academic", "Other"};

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
        actvCategory = findViewById(R.id.actvEventCategory);
        btnSave = findViewById(R.id.btnSaveEvent);

        // Setup Category Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        actvCategory.setAdapter(adapter);

        existingEventId = getIntent().getStringExtra("eventId");
        boolean isEditMode = existingEventId != null && !existingEventId.isEmpty();

        if (isEditMode) {
            tvHeader.setText("Edit Event");
            btnSave.setText("Save Changes");
            etTitle.setText(getIntent().getStringExtra("title"));
            etDescription.setText(getIntent().getStringExtra("description"));
            etLocation.setText(getIntent().getStringExtra("location"));
            etDate.setText(getIntent().getStringExtra("date"));
            etTime.setText(getIntent().getStringExtra("time"));
            etCapacity.setText(String.valueOf(getIntent().getIntExtra("maxCapacity", 0)));
            etPrice.setText(getIntent().getStringExtra("price"));
            actvCategory.setText(getIntent().getStringExtra("category"), false);
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
        String category = actvCategory.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(category)) {
            actvCategory.setError("Category is required");
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
                UserSession.getInstance(this).getUserId(),
                maxCapacity,
                price.isEmpty() ? "Free" : price,
                category
        );

        btnSave.setEnabled(false);

        EventController.OnEventOperationListener listener = new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                btnSave.setEnabled(true);
                Toast.makeText(EditEventActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (isEditMode) {
            eventController.updateEvent(event, listener);
        } else {
            eventController.createEvent(event, listener);
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
