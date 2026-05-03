package com.example.debugz.controller;

import com.example.debugz.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates event retrieval, creation, update, and deletion for all roles.
 * Covers US12 (edit), US13 (capacity), US14 (attendees via registrations), US15 (admin delete).
 * ROLE: Controller Pattern.
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
     */
    public interface OnEventsFetchedListener {
        void onSuccess(List<Event> events);
        void onFailure(Exception e);
        void onDatabaseEmpty();
    }

    /**
     * Callback contract for single-event write/delete operations.
     */
    public interface OnEventOperationListener {
        void onSuccess();
        void onFailure(Exception e);
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
     * UPDATED: Now uses String for price to match the Event model.
     *
     * @param onSuccess code to run after the final seed write succeeds.
     * @deprecated No longer called by the app; events are created by real organizers.
     *             Kept only so existing references compile during migration.
     */
    @Deprecated
    public void seedDemoData(Runnable onSuccess) {
        // Seeding removed — organizers create events through the Organizer Dashboard.
        if (onSuccess != null) onSuccess.run();
    }

    /**
     * Fetches events belonging to a specific organizer from Firestore.
     * Used by OrganizerDashboardActivity to show only the organizer's own events.
     *
     * @param organizerId the organizer whose events to fetch.
     * @param listener    the callback that receives the fetch result.
     */
    public void fetchEventsByOrganizer(String organizerId, OnEventsFetchedListener listener) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
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
     * Writes a new event document to Firestore.
     * If the event has no eventId, one is auto-generated.
     * The created event will immediately appear in the student discovery feed.
     *
     * @param event    the event to persist.
     * @param listener callback for success or failure.
     */
    public void createEvent(Event event, OnEventOperationListener listener) {
        String docId = (event.getEventId() != null && !event.getEventId().isEmpty())
                ? event.getEventId()
                : db.collection("events").document().getId();
        event.setEventId(docId);
        db.collection("events").document(docId).set(event)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Overwrites an existing event document in Firestore with the updated state.
     * Covers US12 (edit details) and US13 (update capacity/ticket limit).
     *
     * @param event    the event with updated fields (must have a valid eventId).
     * @param listener callback for success or failure.
     */
    public void updateEvent(Event event, OnEventOperationListener listener) {
        db.collection("events").document(event.getEventId()).set(event)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Deletes an event document and all associated registration documents from Firestore.
     * Covers US15 (admin removes inappropriate or duplicate events).
     * After deletion the event no longer appears in any user's feed or My Events screen.
     *
     * @param eventId  the Firestore document ID of the event to remove.
     * @param listener callback for success or failure.
     */
    public void deleteEvent(String eventId, OnEventOperationListener listener) {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    // Also remove registrations for this event so students' My-Events stays clean
                    db.collection("registrations")
                            .whereEqualTo("eventId", eventId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                WriteBatch batch = db.batch();
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    batch.delete(doc.getReference());
                                }
                                batch.commit()
                                        .addOnSuccessListener(batchVoid -> listener.onSuccess())
                                        .addOnFailureListener(listener::onFailure);
                            })
                            .addOnFailureListener(listener::onFailure);
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * In-memory filter: returns the subset of events whose organizerId matches.
     * Pure function — no Firestore access — suitable for unit testing.
     *
     * @param organizerId the organizer ID to match.
     * @param allEvents   the full list of events to filter.
     * @return a new list containing only events belonging to the given organizer.
     */
    public List<Event> filterEventsByOrganizer(String organizerId, List<Event> allEvents) {
        List<Event> result = new ArrayList<>();
        if (organizerId == null || organizerId.isEmpty()) return result;
        for (Event event : allEvents) {
            if (organizerId.equals(event.getOrganizerId())) {
                result.add(event);
            }
        }
        return result;
    }

    /**
     * Toggles an upvote on an event using a Firestore transaction (US6).
     * If the student has not yet upvoted: increments upvoteCount and adds studentId.
     * If they already have: decrements upvoteCount and removes studentId.
     *
     * @param eventId   Firestore document ID of the event.
     * @param studentId the student performing the upvote.
     * @param listener  callback reporting whether the final state is upvoted or not.
     */
    public void toggleUpvote(String eventId, String studentId, OnUpvoteListener listener) {
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentReference ref =
                    db.collection("events").document(eventId);
            com.google.firebase.firestore.DocumentSnapshot snap = transaction.get(ref);

            @SuppressWarnings("unchecked")
            java.util.List<String> upvotedBy =
                    (java.util.List<String>) snap.get("upvotedBy");
            if (upvotedBy == null) upvotedBy = new ArrayList<>();

            long currentCount = snap.getLong("upvoteCount") != null
                    ? snap.getLong("upvoteCount") : 0;

            boolean nowUpvoted;
            if (upvotedBy.contains(studentId)) {
                // Toggle OFF — remove upvote
                upvotedBy.remove(studentId);
                currentCount = Math.max(0, currentCount - 1);
                nowUpvoted = false;
            } else {
                // Toggle ON — add upvote
                upvotedBy.add(studentId);
                currentCount++;
                nowUpvoted = true;
            }

            transaction.update(ref, "upvoteCount", currentCount);
            transaction.update(ref, "upvotedBy", upvotedBy);

            return nowUpvoted;
        }).addOnSuccessListener(nowUpvoted -> {
            if (listener != null) listener.onUpvoteToggled(nowUpvoted);
        }).addOnFailureListener(e -> {
            if (listener != null) listener.onFailure(e);
        });
    }

    /** Callback for the toggleUpvote transaction result. */
    public interface OnUpvoteListener {
        void onUpvoteToggled(boolean nowUpvoted);
        void onFailure(Exception e);
    }

    /**
     * Filters a list of events by checking whether the query appears in the title or description.
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