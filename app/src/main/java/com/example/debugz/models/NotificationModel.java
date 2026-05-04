package com.example.debugz.models;

/**
 * Defines the notification entity written to Firestore and displayed in NotificationsActivity.
 * Supports five notification types: RSVP_CONFIRMED, EVENT_REMINDER, FRIEND_RSVP,
 * EVENT_UPDATED, and CAPACITY_LOW.
 * Outstanding issues: notifications are never automatically deleted or expired;
 * they accumulate indefinitely in Firestore.
 *
 * ROLE: Model.
 */
public class NotificationModel {
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private String type;
    private String eventId;
    private long timestamp;
    private boolean isRead;

    /**
     * Creates an empty NotificationModel instance for Firestore deserialization.
     */
    public NotificationModel() {
    }

    /**
     * Creates a fully populated notification.
     *
     * @param notificationId Firestore document ID; pass null to have the controller assign one.
     * @param userId         The student this notification is addressed to.
     * @param title          Short heading shown in bold in the notification row.
     * @param message        Body text shown below the title.
     * @param type           One of: RSVP_CONFIRMED, EVENT_REMINDER, FRIEND_RSVP,
     *                       EVENT_UPDATED, CAPACITY_LOW.
     * @param eventId        The related event ID; used for deep-linking if extended.
     * @param timestamp      Epoch milliseconds; used for newest-first sorting.
     * @param isRead         Whether the user has already seen this notification.
     */
    public NotificationModel(String notificationId, String userId, String title, String message, String type, String eventId, long timestamp, boolean isRead) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    /** @return the Firestore document ID for this notification. */
    public String getNotificationId() { return notificationId; }

    /** @param notificationId the document ID to store. */
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    /** @return the ID of the student this notification is addressed to. */
    public String getUserId() { return userId; }

    /** @param userId the student ID to store. */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return the short heading displayed in bold on the notification row. */
    public String getTitle() { return title; }

    /** @param title the heading to store. */
    public void setTitle(String title) { this.title = title; }

    /** @return the body text displayed below the title. */
    public String getMessage() { return message; }

    /** @param message the body text to store. */
    public void setMessage(String message) { this.message = message; }

    /**
     * @return the notification type string.
     * One of: RSVP_CONFIRMED, EVENT_REMINDER, FRIEND_RSVP, EVENT_UPDATED, CAPACITY_LOW.
     */
    public String getType() { return type; }

    /** @param type the notification type string to store. */
    public void setType(String type) { this.type = type; }

    /** @return the Firestore event ID associated with this notification. */
    public String getEventId() { return eventId; }

    /** @param eventId the event ID to store. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return the creation timestamp in epoch milliseconds. */
    public long getTimestamp() { return timestamp; }

    /** @param timestamp the creation timestamp in epoch milliseconds to store. */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /** @return true if the user has already seen this notification. */
    public boolean isRead() { return isRead; }

    /** @param read true to mark this notification as read. */
    public void setRead(boolean read) { isRead = read; }
}