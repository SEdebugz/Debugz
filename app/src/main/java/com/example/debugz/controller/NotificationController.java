package com.example.debugz.controller;

import com.example.debugz.models.NotificationModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles creation and retrieval of notifications.
 * ROLE: Controller.
 */
public class NotificationController {
    private final FirebaseFirestore db;

    public NotificationController() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnNotificationsFetchedListener {
        void onSuccess(List<NotificationModel> notifications);
        void onFailure(Exception e);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Fetches notifications for a user.
     * Sorting is done in Java to bypass Firestore Index requirements.
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
                    // Sort descending (Newest first) in Java memory
                    list.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    listener.onSuccess(list);
                })
                .addOnFailureListener(listener::onFailure);
    }

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

    public void createNotification(NotificationModel n, OnOperationListener listener) {
        if (n.getNotificationId() == null) {
            n.setNotificationId(db.collection("notifications").document().getId());
        }
        db.collection("notifications").document(n.getNotificationId()).set(n)
                .addOnSuccessListener(aVoid -> { if (listener != null) listener.onSuccess(); })
                .addOnFailureListener(e -> { if (listener != null) listener.onFailure(e); });
    }

    /** Batch create multiple notifications to keep UI smooth. */
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

    public void getUnreadCount(String userId, OnCountListener listener) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> listener.onSuccess(queryDocumentSnapshots.size()))
                .addOnFailureListener(listener::onFailure);
    }

    public interface OnCountListener {
        void onSuccess(int count);
        void onFailure(Exception e);
    }
}
