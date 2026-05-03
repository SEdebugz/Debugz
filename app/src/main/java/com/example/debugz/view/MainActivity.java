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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.controller.EventController;
import com.example.debugz.models.Event;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Hosts the main discovery feed where students browse and search events.
 * Updated to support Category filtering (US2).
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private EditText etSearch;
    private ChipGroup cgCategories;
    private TextView tvEmpty;
    private EventController eventController;

    private String currentCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventController = new EventController();

        rvEvents = findViewById(R.id.rvEvents);
        etSearch = findViewById(R.id.etSearch);
        cgCategories = findViewById(R.id.cgCategories);
        tvEmpty = findViewById(R.id.tvMainEmpty);

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

        findViewById(R.id.btnMyEvents).setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            UserSession.getInstance(this).logout();
            startActivity(new Intent(this, LandingActivity.class));
            finish();
        });

        setupSearchAndFilters();
        loadEvents();
    }

    private void loadEvents() {
        eventController.fetchAllEvents(new EventController.OnEventsFetchedListener() {
            @Override
            public void onSuccess(List<Event> events) {
                allEvents.clear();
                allEvents.addAll(events);
                applyFilters();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_LONG).show();
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        cgCategories.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                currentCategory = chip.getText().toString();
            } else {
                currentCategory = "All";
            }
            applyFilters();
        });
    }

    private void applyFilters() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        filteredEvents.clear();

        for (Event event : allEvents) {
            boolean matchesSearch = query.isEmpty() || 
                                    event.getTitle().toLowerCase().contains(query) || 
                                    event.getDescription().toLowerCase().contains(query);
            
            boolean matchesCategory = currentCategory.equals("All") || 
                                      currentCategory.equalsIgnoreCase(event.getCategory());

            if (matchesSearch && matchesCategory) {
                filteredEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredEvents.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
