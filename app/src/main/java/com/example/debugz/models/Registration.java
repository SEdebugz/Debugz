package com.example.debugz.models;

/**
 * Represents a student's RSVP to a specific campus event.
 * Responsible for acting as the bridge between a Student and an Event.
 */
public class Registration {
    private String registrationId;
    private String studentId;
    private String eventId;
    private String status;
    private long timestamp;

    /**
     * Default constructor required for Firebase Firestore data mapping.
     */
    public Registration() {
    }

    /**
     * Constructs a new Registration linking a student to an event.
     *
     * @param registrationId The unique identifier for this registration record.
     * @param studentId      The ID of the student who is RSVPing.
     * @param eventId        The ID of the event they are attending.
     * @param status         The current status (e.g., "Confirmed", "Waitlisted", "Checked-in").
     * @param timestamp      The time the registration was created.
     */
    public Registration(String registrationId, String studentId, String eventId, String status, long timestamp) {
        this.registrationId = registrationId;
        this.studentId = studentId;
        this.eventId = eventId;
        this.status = status;
        this.timestamp = timestamp;
    }

    /** Returns the unique registration identifier. */
    public String getRegistrationId() { return registrationId; }

    /** Sets the unique registration identifier. */
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    /** Returns the ID of the student who registered. */
    public String getStudentId() { return studentId; }

    /** Sets the ID of the student who registered. */
    public void setStudentId(String studentId) { this.studentId = studentId; }

    /** Returns the ID of the event the student registered for. */
    public String getEventId() { return eventId; }

    /** Sets the ID of the event the student registered for. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** Returns the current status of the RSVP. */
    public String getStatus() { return status; }

    /** Sets the current status of the RSVP. */
    public void setStatus(String status) { this.status = status; }

    /** Returns the timestamp of when the registration was created. */
    public long getTimestamp() { return timestamp; }

    /** Sets the timestamp of when the registration was created. */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}