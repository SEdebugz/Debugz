package com.example.debugz.models;

/**
 * Defines the notification entity for user alerts and updates.
 * ROLE: Model.
 */
public class NotificationModel {
    private String notificationId;
    private String userId; // The student this is for
    private String title;
    private String message;
    private String type; // RSVP_CONFIRMED, EVENT_REMINDER, FRIEND_RSVP, EVENT_UPDATED, CAPACITY_LOW
    private String eventId;
    private long timestamp;
    private boolean isRead;

    public NotificationModel() {
    }

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

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
