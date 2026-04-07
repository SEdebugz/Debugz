package com.example.debugz.controller;

import com.example.debugz.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventController {
    private FirebaseFirestore db;

    public EventController() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnEventsFetchedListener {
        void onSuccess(List<Event> events);
        void onFailure(Exception e);
        void onDatabaseEmpty();
    }

    public void fetchAllEvents(OnEventsFetchedListener listener) {
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onDatabaseEmpty();
                    } else {
                        List<Event> events = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Event event = doc.toObject(Event.class);
                            event.setEventId(doc.getId());
                            events.add(event);
                        }
                        listener.onSuccess(events);
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void seedDemoData(Runnable onSuccess) {
        Event e1 = new Event("event_001", "Engineering Career Fair", "Meet top employers and find internships.", "Main Hall", "March 15, 2026", "10:00 AM", "org1", 200);
        Event e2 = new Event("event_002", "LUMUN 2026", "Premier Model UN conference.", "SDSB Auditorium", "March 18, 2026", "09:00 AM", "org2", 650);
        Event e3 = new Event("event_003", "Khokha Study Circle", "Group study session for CS360.", "Block C-209", "Tonight", "05:00 PM", "org3", 20);

        db.collection("events").document(e1.getEventId()).set(e1);
        db.collection("events").document(e2.getEventId()).set(e2);
        db.collection("events").document(e3.getEventId()).set(e3)
                .addOnSuccessListener(aVoid -> onSuccess.run());
    }

    public List<Event> searchEvents(String query, List<Event> allEvents) {
        List<Event> filtered = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filtered.addAll(allEvents);
            return filtered;
        }

        String lowerQuery = query.toLowerCase();
        for (Event event : allEvents) {
            if (event.getTitle().toLowerCase().contains(lowerQuery) ||
                    event.getDescription().toLowerCase().contains(lowerQuery)) {
                filtered.add(event);
            }
        }
        return filtered;
    }
}