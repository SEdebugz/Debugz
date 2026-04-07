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
import com.example.debugz.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * ROLE: Controller / Observer Pattern.
 * PURPOSE: Acts as the main entry point for the "Discover Events" feature (US1). 
 * It observes the Firestore "events" collection and updates the UI in real-time.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private FirebaseFirestore db;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        
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
            intent.putExtra("price", event.getPrice());
            startActivity(intent);
        });
        rvEvents.setAdapter(adapter);

        findViewById(R.id.btnMyEvents).setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
        });

        setupSearch();
        fetchEvents();
    }

    private void fetchEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        seedDemoData();
                    } else {
                        allEvents.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Event event = doc.toObject(Event.class);
                            event.setEventId(doc.getId());
                            allEvents.add(event);
                        }
                        filter("");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Check internet or google-services.json", Toast.LENGTH_LONG).show();
                });
    }

    private void seedDemoData() {
        WriteBatch batch = db.batch();
        Event e1 = new Event("event_001", "Engineering Career Fair", "Meet top employers and find internships.", "Main Hall", "March 15, 2026", "10:00 AM", "org1", 200, "Free");
        Event e2 = new Event("event_002", "LUMUN 2026", "Premier Model UN conference.", "SDSB Auditorium", "March 18, 2026", "09:00 AM", "org2", 650, "5000 PKR");
        Event e3 = new Event("event_003", "Khokha Study Circle", "Group study session for CS360.", "Block C-209", "Tonight", "05:00 PM", "org3", 20, "Free");

        batch.set(db.collection("events").document(e1.getEventId()), e1);
        batch.set(db.collection("events").document(e2.getEventId()), e2);
        batch.set(db.collection("events").document(e3.getEventId()), e3);

        batch.commit().addOnSuccessListener(aVoid -> {
            fetchEvents(); 
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
        if (query.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Event event : allEvents) {
                if (event.getTitle().toLowerCase().contains(lowerQuery) || 
                    event.getDescription().toLowerCase().contains(lowerQuery)) {
                    filteredEvents.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}