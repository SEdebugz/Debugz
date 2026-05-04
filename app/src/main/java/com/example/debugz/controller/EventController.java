package com.example.debugz.controller;

import com.example.debugz.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates event retrieval, creation, update, and deletion for all roles.
 * Wraps all Firestore event operations and exposes pure helper methods for
 * in-memory filtering and search used by MainActivity and OrganizerDashboardActivity.
 * Outstanding issues: toggleUpvote does not call the onFailure callback on transaction
 * failure; callers cannot distinguish a network error from a successful no-op.
 *
 * ROLE: Controller Pattern.
 */
public class EventController {
    private FirebaseFirestore db;

    /**
     * Creates an EventController and obtains the shared Firestore instance.
     */
    public EventController() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback interface for operations that return a list of events.
     */
    public interface OnEventsFetchedListener {
        /**
         * Called when events are successfully retrieved from Firestore.
         *
         * @param events The list of Event objects returned by the query.
         */
        void onSuccess(List<Event> events);

        /**
         * Called when the Firestore query fails.
         *
         * @param e The exception thrown by Firestore.
         */
        void onFailure(Exception e);

        /**
         * Called when the query succeeds but the result set is empty.
         */
        void onDatabaseEmpty();
    }

    /**
     * Callback interface for single event write operations.
     */
    public interface OnEventOperationListener {
        /**
         * Called when the operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when the operation fails.
         *
         * @param e The exception thrown by Firestore.
         */
        void onFailure(Exception e);
    }

    /**
     * Callback interface for upvote toggle operations.
     */
    public interface OnUpvoteListener {
        /**
         * Called when the upvote transaction completes successfully.
         *
         * @param nowUpvoted true if the student has just upvoted; false if the upvote
         *                   was removed.
         */
        void onUpvoteToggled(boolean nowUpvoted);

        /**
         * Called when the upvote transaction fails.
         *
         * @param e The exception thrown by Firestore.
         */
        void onFailure(Exception e);
    }

    /**
     * Fetches all events from Firestore regardless of status.
     * Callers such as MainActivity filter the result to APPROVED only.
     *
     * @param listener Callback invoked with the full event list, an empty signal,
     *                 or a failure.
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
     * Writes 6 pre-built LUMS demo events to Firestore in a single batch.
     * All seeded events are set to status APPROVED so they appear immediately
     * in the student feed. Safe to call only when the events collection is empty;
     * AdminDashboardActivity checks this before invoking.
     *
     * @param onSuccess Runnable executed after the batch commit succeeds; may be null.
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

    /**
     * Fetches all events created by a specific organizer.
     * Used by OrganizerDashboardActivity to populate the organizer's event list.
     *
     * @param organizerId The organizer ID to filter by; matches the organizerId field
     *                    in each event document.
     * @param listener    Callback invoked with the matching event list, an empty signal,
     *                    or a failure.
     */
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

    /**
     * Writes a new event document to Firestore with status PENDING.
     * If the event has no eventId, a UUID is generated automatically.
     * The event will not appear in the student feed until an admin approves it.
     *
     * @param event    The Event object to persist; organizerId must be set to the
     *                 current session user's ID before calling.
     * @param listener Callback invoked on success or failure.
     */
    public void createEvent(Event event, OnEventOperationListener listener) {
        String docId = (event.getEventId() != null && !event.getEventId().isEmpty())
                ? event.getEventId()
                : db.collection("events").document().getId();
        event.setEventId(docId);
        event.setStatus(Event.STATUS_PENDING);
        db.collection("events").document(docId).set(event)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Overwrites an existing event document in Firestore.
     * Used by EditEventActivity when saving changes to an existing event.
     * Does not reset the event's approval status.
     *
     * @param event    The Event object with updated fields; eventId must match an
     *                 existing Firestore document.
     * @param listener Callback invoked on success or failure.
     */
    public void updateEvent(Event event, OnEventOperationListener listener) {
        db.collection("events").document(event.getEventId()).set(event)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Deletes an event and all associated registration documents in a cascade.
     * Step 1: deletes the event document from the events collection.
     * Step 2: queries registrations where eventId matches and batch-deletes them.
     * Used by both AdminDashboardActivity and OrganizerDashboardActivity.
     *
     * @param eventId  The Firestore document ID of the event to delete.
     * @param listener Callback invoked after both the event and its registrations
     *                 are deleted, or on failure at either step.
     */
    public void deleteEvent(String eventId, OnEventOperationListener listener) {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("registrations").whereEqualTo("eventId", eventId).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                WriteBatch batch = db.batch();
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots)
                                    batch.delete(doc.getReference());
                                batch.commit()
                                        .addOnSuccessListener(batchVoid -> listener.onSuccess())
                                        .addOnFailureListener(listener::onFailure);
                            });
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Toggles the upvote state for a student on a specific event using a Firestore transaction.
     * If the student has already upvoted, the upvote is removed; otherwise it is added.
     * Both upvoteCount and upvotedBy are updated atomically.
     *
     * @param eventId   The Firestore document ID of the event to upvote or un-upvote.
     * @param studentId The ID of the student casting or removing the upvote.
     * @param listener  Callback invoked with the new upvote state on success; the
     *                  onFailure callback is not currently wired to transaction failures.
     */
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
        }).addOnSuccessListener(nowUpvoted -> {
            if (listener != null) listener.onUpvoteToggled(nowUpvoted);
        });
    }

    /**
     * Filters an in-memory event list by a search query against title and description.
     * Case-insensitive. Returns all events if the query is null or empty.
     * Used by MainActivity.applyFilters() to avoid repeated Firestore reads.
     *
     * @param query     The search string typed by the user; null or empty returns all events.
     * @param allEvents The full list of events to filter.
     * @return A new list containing only events whose title or description contains
     *         the query string.
     */
    public List<Event> searchEvents(String query, List<Event> allEvents) {
        List<Event> filtered = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filtered.addAll(allEvents);
            return filtered;
        }
        String lowerQuery = query.toLowerCase();
        for (Event event : allEvents) {
            if (event.getTitle().toLowerCase().contains(lowerQuery)
                    || event.getDescription().toLowerCase().contains(lowerQuery))
                filtered.add(event);
        }
        return filtered;
    }
}