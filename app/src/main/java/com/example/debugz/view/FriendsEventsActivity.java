package com.example.debugz.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.models.Event;
import com.example.debugz.models.Registration;
import com.example.debugz.models.Student;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsEventsActivity extends AppCompatActivity {

    private RecyclerView rvFriendsEvents;
    private EventAdapter adapter;
    private List<Event> friendsEventsList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentStudentId = "demo_student_123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_events);

        db = FirebaseFirestore.getInstance();
        rvFriendsEvents = findViewById(R.id.rvFriendsEvents);
        rvFriendsEvents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(friendsEventsList, event -> {
            // Optional: navigate to details if you want
        });
        rvFriendsEvents.setAdapter(adapter);

        fetchFriendsEvents();
    }

    private void fetchFriendsEvents() {
        db.collection("students").document(currentStudentId).get().addOnSuccessListener(documentSnapshot -> {
            Student me = documentSnapshot.toObject(Student.class);

            if (me == null || me.getFriendIds() == null || me.getFriendIds().isEmpty()) {
                Toast.makeText(this, "You aren't following anyone yet.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> friends = me.getFriendIds();

            // Firestore 'whereIn' only supports arrays up to 10 elements. Slicing for safety.
            if (friends.size() > 10) {
                friends = friends.subList(0, 10);
            }

            db.collection("registrations").whereIn("studentId", friends).get().addOnSuccessListener(regSnapshots -> {
                List<String> eventIds = new ArrayList<>();
                for (QueryDocumentSnapshot regDoc : regSnapshots) {
                    Registration reg = regDoc.toObject(Registration.class);
                    if (!eventIds.contains(reg.getEventId())) {
                        eventIds.add(reg.getEventId());
                    }
                }

                if (eventIds.isEmpty()) {
                    Toast.makeText(this, "Your friends haven't RSVP'd to anything.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (eventIds.size() > 10) {
                    eventIds = eventIds.subList(0, 10);
                }

                db.collection("events").whereIn("eventId", eventIds).get().addOnSuccessListener(eventSnapshots -> {
                    friendsEventsList.clear();
                    for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                        Event event = eventDoc.toObject(Event.class);
                        friendsEventsList.add(event);
                    }
                    adapter.notifyDataSetChanged();
                });
            });
        });
    }
}