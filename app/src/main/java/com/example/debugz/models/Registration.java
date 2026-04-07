package com.example.debugz.models;

/**
 * Defines the RSVP record that connects a student to an event in the app.
 * This model is the join object used for registration status tracking, attendance history,
 * and other workflow state such as confirmation or waitlisting.
 * Outstanding issues: status values are stored as raw strings, so callers can still write
 * unsupported workflow states unless higher layers validate them first.
 */
public class Registration {
    private String registrationId;
    private String studentId;
    private String eventId;
    private String status;
    private long timestamp;

    /**
     * Creates an empty registration instance for Firebase Firestore deserialization.
     */
    public Registration() {
    }

    /**
     * Creates a registration linking a student to an event.
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

    /**
     * Returns the unique registration identifier.
     *
     * @return the registration ID.
     */
    public String getRegistrationId() { return registrationId; }

    /**
     * Sets the unique registration identifier.
     *
     * @param registrationId the registration ID to store.
     */
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    /**
     * Returns the ID of the student tied to the registration.
     *
     * @return the student ID.
     */
    public String getStudentId() { return studentId; }

    /**
     * Sets the ID of the student tied to the registration.
     *
     * @param studentId the student ID to store.
     */
    public void setStudentId(String studentId) { this.studentId = studentId; }

    /**
     * Returns the ID of the event tied to the registration.
     *
     * @return the event ID.
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the ID of the event tied to the registration.
     *
     * @param eventId the event ID to store.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Returns the current RSVP status.
     *
     * @return the workflow status string.
     */
    public String getStatus() { return status; }

    /**
     * Sets the current RSVP status.
     *
     * @param status the workflow status string to store.
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns the creation timestamp for the registration.
     *
     * @return the registration timestamp in epoch milliseconds.
     */
    public long getTimestamp() { return timestamp; }

    /**
     * Sets the creation timestamp for the registration.
     *
     * @param timestamp the registration timestamp in epoch milliseconds.
     */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
