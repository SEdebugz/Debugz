package com.example.debugz.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.example.debugz.controller.NotificationController;
import com.example.debugz.models.Event;
import com.example.debugz.models.NotificationModel;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * Form screen used by the organizer to create a new event or edit an existing one.
 * Uses Date and Time pickers for structured input (Task 4).
 * Triggers EVENT_UPDATED notifications (Task 6).
 */
public class EditEventActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etLocation, etDate, etTime, etCapacity, etPrice;
    private AutoCompleteTextView actvCategory;
    private Button btnSave;
    private TextView tvHeader;
    private EventController eventController;
    private NotificationController notificationController;

    private String existingEventId;
    private Calendar calendar = Calendar.getInstance();

    private static final String[] CATEGORIES = {"Sports", "Talk", "Performance", "Club", "Academic", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        eventController = new EventController();
        notificationController = new NotificationController();

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        actvCategory.setAdapter(adapter);

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

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

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
            etDate.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
            etTime.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
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

        if (TextUtils.isEmpty(title)) { etTitle.setError("Title required"); return; }
        if (TextUtils.isEmpty(category)) { actvCategory.setError("Category required"); return; }
        if (TextUtils.isEmpty(date)) { etDate.setError("Date required"); return; }

        int maxCapacity = 0;
        try { maxCapacity = Integer.parseInt(capacityStr); } catch (Exception ignored) {}

        String eventId = isEditMode ? existingEventId : UUID.randomUUID().toString();
        Event event = new Event(eventId, title, description, location, date, time, 
                UserSession.getInstance(this).getUserId(), maxCapacity, price, category);

        btnSave.setEnabled(false);

        if (isEditMode) {
            eventController.updateEvent(event, new EventController.OnEventOperationListener() {
                @Override
                public void onSuccess() {
                    triggerUpdateNotifications(event);
                    finish();
                }
                @Override
                public void onFailure(Exception e) {
                    btnSave.setEnabled(true);
                    Toast.makeText(EditEventActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            eventController.createEvent(event, new EventController.OnEventOperationListener() {
                @Override
                public void onSuccess() { finish(); }
                @Override
                public void onFailure(Exception e) {
                    btnSave.setEnabled(true);
                    Toast.makeText(EditEventActivity.this, "Creation failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void triggerUpdateNotifications(Event event) {
        if (event.getAttendeeIds() == null) return;
        for (String studentId : event.getAttendeeIds()) {
            NotificationModel n = new NotificationModel(null, studentId, "Event Updated 📝",
                    event.getTitle() + " details have been updated. Check the app!", 
                    "EVENT_UPDATED", event.getEventId(), System.currentTimeMillis(), false);
            notificationController.createNotification(n, null);
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
