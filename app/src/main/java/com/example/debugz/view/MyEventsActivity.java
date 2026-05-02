package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.models.Event;
import com.example.debugz.models.Registration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the events the current student has RSVP'd to (US10).
 * Reads all Registration documents for the logged-in student, then fetches the
 * corresponding Event documents to display with the shared EventAdapter.
 */
public class MyEventsActivity extends AppCompatActivity {

    private RecyclerView rvMyEvents;
    private EventAdapter adapter;
    private final List<Event> myEventsList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView tvEmpty;
    private String currentStudentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        db = FirebaseFirestore.getInstance();
        currentStudentId = UserSession.getInstance(this).getUserId();

        tvEmpty = findViewById(R.id.tvMyEventsEmpty);

        rvMyEvents = findViewById(R.id.rvMyEvents);
        rvMyEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(myEventsList, event -> {
            // Tap to view details
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("eventId",     event.getEventId());
            intent.putExtra("title",       event.getTitle());
            intent.putExtra("date",        event.getDate());
            intent.putExtra("time",        event.getTime());
            intent.putExtra("location",    event.getLocation());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("capacity",    event.getMaxCapacity());
            intent.putExtra("price",       event.getPrice());
            startActivity(intent);
        });
        rvMyEvents.setAdapter(adapter);

        fetchMyRegistrations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMyRegistrations();
    }

    private void fetchMyRegistrations() {
        if (currentStudentId.isEmpty()) {
            showEmpty();
            return;
        }

        db.collection("registrations")
                .whereEqualTo("studentId", currentStudentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> eventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Registration reg = doc.toObject(Registration.class);
                        if (reg.getEventId() != null) {
                            eventIds.add(reg.getEventId());
                        }
                    }
                    if (!eventIds.isEmpty()) {
                        fetchEventDetails(eventIds);
                    } else {
                        showEmpty();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching RSVPs", Toast.LENGTH_SHORT).show());
    }

    private void fetchEventDetails(List<String> eventIds) {
        // Firestore 'whereIn' supports up to 30 values; events per student are typically far fewer
        db.collection("events")
                .whereIn("eventId", eventIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myEventsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        myEventsList.add(event);
                    }
                    adapter.notifyDataSetChanged();

                    if (myEventsList.isEmpty()) {
                        showEmpty();
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvMyEvents.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show());
    }

    private void showEmpty() {
        myEventsList.clear();
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(View.VISIBLE);
        rvMyEvents.setVisibility(View.GONE);
    }
}
