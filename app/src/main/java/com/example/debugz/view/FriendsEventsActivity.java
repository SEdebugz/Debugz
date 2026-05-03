package com.example.debugz.view;

import android.annotation.SuppressLint;
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
import com.example.debugz.models.Account;
import com.example.debugz.models.Event;
import com.example.debugz.models.Registration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows events that the current student's friends have RSVP'd to.
 *
 * Flow:
 *   1. Load current user's account → get friendIds list
 *   2. Query registrations where studentId is in friendIds
 *   3. Collect unique eventIds from those registrations
 *   4. Fetch those events and display them
 *
 * All reads are from "accounts" and "registrations" / "events" collections.
 */
public class FriendsEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView      rvFriendsEvents;
    private EventAdapter      adapter;
    private TextView          tvEmpty;

    private final List<Event> friendsEventsList = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_events);

        db            = FirebaseFirestore.getInstance();
        currentUserId = UserSession.getInstance(this).getUserId();

        tvEmpty         = findViewById(R.id.tvFriendsEmpty);
        rvFriendsEvents = findViewById(R.id.rvFriendsEvents);
        rvFriendsEvents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(friendsEventsList, event -> {
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
        rvFriendsEvents.setAdapter(adapter);

        fetchFriendsEvents();
    }

    private void fetchFriendsEvents() {
        // Step 1: load the current user's account to get their friendIds
        db.collection("accounts").document(currentUserId).get()
                .addOnSuccessListener(myDoc -> {
                    if (!myDoc.exists()) {
                        showEmpty("Your account could not be found.");
                        return;
                    }

                    Account me = myDoc.toObject(Account.class);
                    if (me == null || me.getFriendIds() == null || me.getFriendIds().isEmpty()) {
                        showEmpty("You haven't added any friends yet.\nUse Find Friends to get started!");
                        return;
                    }

                    List<String> friendIds = me.getFriendIds();

                    // Firestore whereIn supports up to 30 values
                    if (friendIds.size() > 30) friendIds = friendIds.subList(0, 30);

                    fetchRegistrationsForFriends(friendIds);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load your profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showEmpty("Something went wrong. Please try again.");
                });
    }

    // Step 2: get all registrations where studentId is one of the friend IDs
    private void fetchRegistrationsForFriends(List<String> friendIds) {
        db.collection("registrations")
                .whereIn("studentId", friendIds)
                .get()
                .addOnSuccessListener(regSnap -> {
                    // Step 3: collect unique event IDs
                    List<String> eventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : regSnap) {
                        Registration reg = doc.toObject(Registration.class);
                        if (reg.getEventId() != null
                                && !eventIds.contains(reg.getEventId())) {
                            eventIds.add(reg.getEventId());
                        }
                    }

                    if (eventIds.isEmpty()) {
                        showEmpty("Your friends haven't RSVP'd to any events yet.");
                        return;
                    }

                    // Firestore whereIn supports up to 30 values
                    if (eventIds.size() > 30) eventIds = eventIds.subList(0, 30);

                    fetchEvents(eventIds);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load friends' registrations: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showEmpty("Something went wrong. Please try again.");
                });
    }

    // Step 4: fetch the actual event documents
    @SuppressLint("NotifyDataSetChanged")
    private void fetchEvents(List<String> eventIds) {
        db.collection("events")
                .whereIn("eventId", eventIds)
                .get()
                .addOnSuccessListener(eventSnap -> {
                    friendsEventsList.clear();
                    for (QueryDocumentSnapshot doc : eventSnap) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        friendsEventsList.add(event);
                    }

                    if (friendsEventsList.isEmpty()) {
                        showEmpty("Your friends haven't RSVP'd to any events yet.");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvFriendsEvents.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showEmpty("Something went wrong. Please try again.");
                });
    }

    private void showEmpty(String message) {
        friendsEventsList.clear();
        adapter.notifyDataSetChanged();
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
        rvFriendsEvents.setVisibility(View.GONE);
    }
}