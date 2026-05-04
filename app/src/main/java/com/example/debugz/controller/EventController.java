package com.example.debugz.controller;

import com.example.debugz.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates event retrieval, creation, update, and deletion for all roles.
 * ROLE: Controller Pattern.
 */
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

    public interface OnEventOperationListener {
        void onSuccess();
        void onFailure(Exception e);
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

    /**
     * Seeds 6 realistic LUMS demo events.
     * Demo events are automatically marked as APPROVED.
     */
    public void seedDemoData(Runnable onSuccess) {
        WriteBatch batch = db.batch();
        
        Event[] demoEvents = {
            new Event("lums_fest_2026", "LUMS Music Festival", "Annual concert featuring local bands.", "Central Courtyard", "April 25, 2026", "7:00 PM", "lums_music_soc", 1000, "1500 PKR", "Performance"),
            new Event("cs_symposium_2026", "CS Research Symposium", "Undergraduate and graduate students present their research in AI, systems, and software engineering.", "SDSB Auditorium", "May 22, 2026", "10:00 AM", "lums_cs_society", 150, "Free", "Talk"),
            new Event("cricket_iba_2026", "LUMS vs IBA Cricket Match", "Inter-university cricket rivalry returns. Come support LUMS against IBA Karachi.", "Cricket Ground", "May 24, 2026", "9:00 AM", "lums_sports_board", 500, "Free", "Sports"),
            new Event("ent_summit_2026", "Entrepreneurship Summit 2026", "Pakistan's leading startup founders and investors share insights on building companies from scratch.", "SDSB Auditorium", "May 25, 2026", "11:00 AM", "lums_entrepreneurship_society", 300, "500 PKR", "Talk"),
            new Event("sanat_ghar_2026", "Sanat Ghar Cultural Evening", "A fusion evening of classical music, poetry, and visual art celebrating Pakistani heritage.", "Sanat Ghar", "May 28, 2026", "6:00 PM", "lums_arts_council", 120, "Free", "Performance"),
            new Event("ml_workshop_2026", "Machine Learning Workshop", "Hands-on workshop covering neural networks and model training using Python. Laptops required.", "SBASSE Building", "May 30, 2026", "2:00 PM", "lums_cs_society", 60, "Free", "Club")
        };

        for (Event e : demoEvents) {
            e.setStatus(Event.STATUS_APPROVED);
            batch.set(db.collection("events").document(e.getEventId()), e);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void fetchEventsByOrganizer(String organizerId, OnEventsFetchedListener listener) {
        db.collection("events").whereEqualTo("organizerId", organizerId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onDatabaseEmpty();
                        return;
                    }
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        events.add(event);
                    }
                    listener.onSuccess(events);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void createEvent(Event event, OnEventOperationListener listener) {
        String docId = (event.getEventId() != null && !event.getEventId().isEmpty()) ? event.getEventId() : db.collection("events").document().getId();
        event.setEventId(docId);
        event.setStatus(Event.STATUS_PENDING); // New events must be approved by admin
        db.collection("events").document(docId).set(event).addOnSuccessListener(aVoid -> listener.onSuccess()).addOnFailureListener(listener::onFailure);
    }

    public void updateEvent(Event event, OnEventOperationListener listener) {
        db.collection("events").document(event.getEventId()).set(event).addOnSuccessListener(aVoid -> listener.onSuccess()).addOnFailureListener(listener::onFailure);
    }

    public void deleteEvent(String eventId, OnEventOperationListener listener) {
        db.collection("events").document(eventId).delete().addOnSuccessListener(aVoid -> {
            db.collection("registrations").whereEqualTo("eventId", eventId).get().addOnSuccessListener(queryDocumentSnapshots -> {
                WriteBatch batch = db.batch();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) batch.delete(doc.getReference());
                batch.commit().addOnSuccessListener(batchVoid -> listener.onSuccess()).addOnFailureListener(listener::onFailure);
            });
        }).addOnFailureListener(listener::onFailure);
    }

    public void toggleUpvote(String eventId, String studentId, OnUpvoteListener listener) {
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentReference ref = db.collection("events").document(eventId);
            com.google.firebase.firestore.DocumentSnapshot snap = transaction.get(ref);
            Event event = snap.toObject(Event.class);
            if (event == null) return false;
            boolean nowUpvoted;
            if (event.getUpvotedBy().contains(studentId)) {
                event.removeUpvote(studentId);
                nowUpvoted = false;
            } else {
                event.addUpvote(studentId);
                nowUpvoted = true;
            }
            transaction.update(ref, "upvoteCount", event.getUpvoteCount());
            transaction.update(ref, "upvotedBy", event.getUpvotedBy());
            return nowUpvoted;
        }).addOnSuccessListener(nowUpvoted -> { if (listener != null) listener.onUpvoteToggled(nowUpvoted); });
    }

    public interface OnUpvoteListener {
        void onUpvoteToggled(boolean nowUpvoted);
        void onFailure(Exception e);
    }

    public List<Event> searchEvents(String query, List<Event> allEvents) {
        List<Event> filtered = new ArrayList<>();
        if (query == null || query.isEmpty()) { filtered.addAll(allEvents); return filtered; }
        String lowerQuery = query.toLowerCase();
        for (Event event : allEvents) {
            if (event.getTitle().toLowerCase().contains(lowerQuery) || event.getDescription().toLowerCase().contains(lowerQuery)) filtered.add(event);
        }
        return filtered;
    }
}
