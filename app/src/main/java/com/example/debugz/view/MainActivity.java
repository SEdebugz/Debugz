package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.controller.EventController;
import com.example.debugz.models.Event;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private EditText etSearch;
    private EventController eventController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventController = new EventController();

        rvEvents = findViewById(R.id.rvEvents);
        etSearch = findViewById(R.id.etSearch);

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
            startActivity(intent);
        });
        rvEvents.setAdapter(adapter);

        findViewById(R.id.btnMyEvents).setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
        });

        setupSearch();
        loadEvents();
    }

    private void loadEvents() {
        eventController.fetchAllEvents(new EventController.OnEventsFetchedListener() {
            @Override
            public void onSuccess(List<Event> events) {
                allEvents.clear();
                allEvents.addAll(events);
                filter("");
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Check internet or google-services.json", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDatabaseEmpty() {
                eventController.seedDemoData(() -> loadEvents());
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        filteredEvents.clear();
        filteredEvents.addAll(eventController.searchEvents(query, allEvents));
        adapter.notifyDataSetChanged();
    }
}