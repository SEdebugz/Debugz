package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.controller.EventController;
import com.example.debugz.controller.NotificationController;
import com.example.debugz.models.Event;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Hosts the main discovery feed where students browse and search events.
 * Only shows APPROVED events (US-Admin Approval).
 *
 * ROLE: View (Activity).
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();
    private EditText etSearch;
    private ChipGroup cgCategories;
    private TextView tvEmpty;
    private View notificationBadge;
    private EventController eventController;
    private NotificationController notificationController;

    private String currentCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventController = new EventController();
        notificationController = new NotificationController();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvEvents = findViewById(R.id.rvEvents);
        etSearch = findViewById(R.id.etSearch);
        cgCategories = findViewById(R.id.cgCategories);
        tvEmpty = findViewById(R.id.tvMainEmpty);
        notificationBadge = findViewById(R.id.notificationBadge);

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(filteredEvents, event -> {
            Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("title", event.getTitle());
            intent.putExtra("date", event.getDate());
            intent.putExtra("time", event.getTime());
            intent.putExtra("location", event.getLocation());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("capacity", event.getMaxCapacity());
            intent.putExtra("price", event.getPrice());
            intent.putExtra("category", event.getCategory());
            startActivity(intent);
        });
        rvEvents.setAdapter(adapter);

        findViewById(R.id.btnNotifications).setOnClickListener(v -> 
            startActivity(new Intent(this, NotificationsActivity.class)));

        findViewById(R.id.btnMyEvents).setOnClickListener(v ->
                startActivity(new Intent(this, MyEventsActivity.class)));

        findViewById(R.id.btnAddFriends).setOnClickListener(v ->
                startActivity(new Intent(this, AddFriendActivity.class)));

        findViewById(R.id.btnFriendsEvents).setOnClickListener(v ->
                startActivity(new Intent(this, FriendsEventsActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        setupSearchAndFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        notificationController.getUnreadCount(UserSession.getInstance(this).getUserId(), new NotificationController.OnCountListener() {
            @Override
            public void onSuccess(int count) {
                notificationBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Exception e) {}
        });
    }

    private void loadEvents() {
        eventController.fetchAllEvents(new EventController.OnEventsFetchedListener() {
            @Override
            public void onSuccess(List<Event> events) {
                // Admin Approval Logic: Only show APPROVED events to students
                List<Event> approvedEvents = new ArrayList<>();
                for (Event e : events) {
                    if (Event.STATUS_APPROVED.equals(e.getStatus())) {
                        approvedEvents.add(e);
                    }
                }
                
                approvedEvents.sort((a, b) -> b.getUpvoteCount() - a.getUpvoteCount());
                allEvents.clear();
                allEvents.addAll(approvedEvents);
                applyFilters();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDatabaseEmpty() {
                allEvents.clear();
                applyFilters();
            }
        });
    }

    private void setupSearchAndFilters() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        cgCategories.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            currentCategory = (chip != null) ? chip.getText().toString() : "All";
            applyFilters();
        });
    }

    private void applyFilters() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        filteredEvents.clear();

        for (Event event : allEvents) {
            boolean matchesSearch = query.isEmpty()
                    || event.getTitle().toLowerCase().contains(query)
                    || event.getDescription().toLowerCase().contains(query);

            boolean matchesCategory = currentCategory.equals("All")
                    || currentCategory.equalsIgnoreCase(event.getCategory());

            if (matchesSearch && matchesCategory) {
                filteredEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredEvents.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void logout() {
        UserSession.getInstance(this).logout();
        Intent intent = new Intent(this, LandingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
