package com.example.debugz.controller;

import com.example.debugz.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates event retrieval and prototype event data for the discovery screens.
 * This controller acts as a thin mediator between Firestore and the view layer, and also
 * centralizes the simple keyword-based filtering logic used by the main event feed.
 * Outstanding issues: demo seeding still lives in production code and searching currently
 * checks only titles and descriptions with no normalization beyond lowercasing.
 */
public class EventController {
    private FirebaseFirestore db;

    /**
     * Creates a controller backed by the shared Firestore instance.
     */
    public EventController() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback contract for asynchronous event-loading operations.
     * Outstanding issues: the interface exposes multiple mutually exclusive callbacks instead
     * of a single result type, so callers must coordinate state transitions manually.
     */
    public interface OnEventsFetchedListener {
        /**
         * Called when events are fetched successfully.
         *
         * @param events the events returned from Firestore.
         */
        void onSuccess(List<Event> events);

        /**
         * Called when event loading fails.
         *
         * @param e the failure reported by Firestore.
         */
        void onFailure(Exception e);

        /**
         * Called when the events collection exists but contains no documents.
         */
        void onDatabaseEmpty();
    }

    /**
     * Fetches all stored events from Firestore.
     *
     * @param listener the callback that receives the fetch result.
     */
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

    /**
     * Writes a fixed set of demo events to Firestore for prototype use.
     *
     * @param onSuccess code to run after the final seed write succeeds.
     */
    public void seedDemoData(Runnable onSuccess) {
        Event e1 = new Event("event_001", "Engineering Career Fair", "Meet top employers and find internships.", "Main Hall", "March 15, 2026", "10:00 AM", "org1", 200, 0.0);
        Event e2 = new Event("event_002", "LUMUN 2026", "Premier Model UN conference.", "SDSB Auditorium", "March 18, 2026", "09:00 AM", "org2", 650, 1500.0);
        Event e3 = new Event("event_003", "Khokha Study Circle", "Group study session for CS360.", "Block C-209", "Tonight", "05:00 PM", "org3", 20, 0.0);

        db.collection("events").document(e1.getEventId()).set(e1);
        db.collection("events").document(e2.getEventId()).set(e2);
        db.collection("events").document(e3.getEventId()).set(e3)
                .addOnSuccessListener(aVoid -> onSuccess.run());
    }

    /**
     * Filters a list of events by checking whether the query appears in the title or description.
     *
     * @param query the text entered by the user.
     * @param allEvents the complete event list to search.
     * @return the filtered list of matching events.
     */
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
