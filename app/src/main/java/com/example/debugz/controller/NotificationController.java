package com.example.debugz.controller;

import com.example.debugz.models.NotificationModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles creation and retrieval of in-app notifications stored in Firestore.
 * Notifications are written by EventDetailActivity on RSVP and by EditEventActivity
 * on event update, then read and displayed by NotificationsActivity.
 * Outstanding issues: notifications are never automatically deleted or expired;
 * they accumulate indefinitely in Firestore.
 *
 * ROLE: Controller Pattern.
 */
public class NotificationController {
    private final FirebaseFirestore db;

    /**
     * Creates a NotificationController and obtains the shared Firestore instance.
     */
    public NotificationController() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback interface for operations that return a list of notifications.
     */
    public interface OnNotificationsFetchedListener {
        /**
         * Called when notifications are successfully retrieved and sorted.
         *
         * @param notifications The list of NotificationModel objects sorted newest-first.
         */
        void onSuccess(List<NotificationModel> notifications);

        /**
         * Called when the Firestore query fails.
         *
         * @param e The exception thrown by Firestore.
         */
        void onFailure(Exception e);
    }

    /**
     * Callback interface for single write operations such as mark-as-read.
     */
    public interface OnOperationListener {
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
     * Callback interface for unread count queries.
     */
    public interface OnCountListener {
        /**
         * Called when the unread count is successfully retrieved.
         *
         * @param count The number of unread notifications for the user.
         */
        void onSuccess(int count);

        /**
         * Called when the count query fails.
         *
         * @param e The exception thrown by Firestore.
         */
        void onFailure(Exception e);
    }

    /**
     * Fetches all notifications addressed to a specific user, sorted newest-first.
     * Sorting is performed in memory to avoid requiring a composite Firestore index.
     * Used by NotificationsActivity to populate the notification list.
     *
     * @param userId   The student ID whose notifications should be fetched.
     * @param listener Callback invoked with the sorted notification list or a failure.
     */
    public void fetchNotificationsForUser(String userId, OnNotificationsFetchedListener listener) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(NotificationModel.class));
                    }
                    list.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    listener.onSuccess(list);
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Marks all unread notifications for a user as read in a single batch write.
     * Called by NotificationsActivity when the user taps "Mark all as read".
     *
     * @param userId   The student ID whose unread notifications should be updated.
     * @param listener Callback invoked on success or failure.
     */
    public void markAllAsRead(String userId, OnOperationListener listener) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "isRead", true);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> listener.onSuccess())
                            .addOnFailureListener(listener::onFailure);
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Writes a single notification document to Firestore.
     * If the notification has no ID, one is generated automatically before writing.
     * Used by EventDetailActivity on RSVP and EditEventActivity on event update.
     * The listener parameter may be null if the caller does not need a callback.
     *
     * @param n        The NotificationModel to persist.
     * @param listener Callback invoked on success or failure; may be null.
     */
    public void createNotification(NotificationModel n, OnOperationListener listener) {
        if (n.getNotificationId() == null) {
            n.setNotificationId(db.collection("notifications").document().getId());
        }
        db.collection("notifications").document(n.getNotificationId()).set(n)
                .addOnSuccessListener(aVoid -> { if (listener != null) listener.onSuccess(); })
                .addOnFailureListener(e -> { if (listener != null) listener.onFailure(e); });
    }

    /**
     * Writes multiple notification documents to Firestore in a single batch.
     * Used by EditEventActivity to notify all attendees of an event update without
     * blocking the UI with sequential writes.
     *
     * @param list The list of NotificationModel objects to persist; each will be
     *             assigned a generated ID. Does nothing if null or empty.
     */
    public void createNotificationsBatch(List<NotificationModel> list) {
        if (list == null || list.isEmpty()) return;
        WriteBatch batch = db.batch();
        for (NotificationModel n : list) {
            String id = db.collection("notifications").document().getId();
            n.setNotificationId(id);
            batch.set(db.collection("notifications").document(id), n);
        }
        batch.commit();
    }

    /**
     * Retrieves the count of unread notifications for a user.
     * Used by MainActivity.updateNotificationBadge() to show or hide the red dot
     * on the notification bell icon.
     *
     * @param userId   The student ID to count unread notifications for.
     * @param listener Callback invoked with the unread count or a failure.
     */
    public void getUnreadCount(String userId, OnCountListener listener) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        listener.onSuccess(queryDocumentSnapshots.size()))
                .addOnFailureListener(listener::onFailure);
    }
}