package com.example.debugz.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.models.Event;
import com.example.debugz.models.Registration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display events the student has RSVP'd to.
 * Satisfies US10 (Monitor RSVP'd events).
 */
public class MyEventsActivity extends AppCompatActivity {

    private RecyclerView rvMyEvents;
    private EventAdapter adapter;
    private List<Event> myEventsList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        db = FirebaseFirestore.getInstance();
        rvMyEvents = findViewById(R.id.rvMyEvents);
        rvMyEvents.setLayoutManager(new LinearLayoutManager(this));
        
        // We reuse EventAdapter but can omit click listener or navigate back to details
        adapter = new EventAdapter(myEventsList, event -> {
            // Optional: View details again
        });
        rvMyEvents.setAdapter(adapter);

        fetchMyRegistrations();
    }

    private void fetchMyRegistrations() {
        String currentStudentId = "demo_student_123"; // Must match EventDetailActivity
        
        db.collection("registrations")
                .whereEqualTo("studentId", currentStudentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> eventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Registration reg = doc.toObject(Registration.class);
                        eventIds.add(reg.getEventId());
                    }
                    if (!eventIds.isEmpty()) {
                        fetchEventDetails(eventIds);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching RSVPs", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchEventDetails(List<String> eventIds) {
        // Simple way: Fetch all events and filter locally for the prototype
        // Better way: Firestore 'in' query
        db.collection("events")
                .whereIn("eventId", eventIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myEventsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        myEventsList.add(event);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}