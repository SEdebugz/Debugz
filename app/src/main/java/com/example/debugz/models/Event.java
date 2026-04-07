package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the event entity used throughout the app's event-discovery workflow.
 * This model stores the details shown in listings and event-detail screens, along with
 * attendee tracking data used for RSVP and remaining-capacity calculations.
 * Outstanding issues: validation for dates, prices, and capacity is still handled outside
 * the model, so invalid values can still be assigned by callers.
 */
public class Event {
    private String eventId;
    private String title;
    private String description;
    private String location;
    private String date;
    private String time;
    private double ticketPrice;
    private String organizerId;
    private int maxCapacity;
    private List<String> attendeeIds;

    /**
     * Creates an empty event instance for Firebase Firestore deserialization.
     */
    public Event() {
        this.attendeeIds = new ArrayList<>();
    }

    /**
     * Creates an event with the core data needed by the UI and Firestore.
     *
     * @param eventId     The unique identifier for the event.
     * @param title       The name of the event.
     * @param description Details about what the event is.
     * @param location    Where the event is taking place.
     * @param date        The date of the event.
     * @param time        The time of the event.
     * @param organizerId The ID of the organizer who created it.
     * @param maxCapacity The maximum number of attendees allowed.
     */
    public Event(String eventId, String title, String description, String location, String date, String time, String organizerId, int maxCapacity, double ticketPrice) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.time = time;
        this.organizerId = organizerId;
        this.maxCapacity = maxCapacity;
        this.attendeeIds = new ArrayList<>();
        this.ticketPrice = ticketPrice;
    }

    /**
     * Returns the unique event identifier.
     *
     * @return the event ID used by the app and Firestore.
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the unique event identifier.
     *
     * @param eventId the event ID to store.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Returns the title shown for the event.
     *
     * @return the event title.
     */
    public String getTitle() { return title; }

    /**
     * Sets the title shown for the event.
     *
     * @param title the event title.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Returns the event description.
     *
     * @return the descriptive text for the event.
     */
    public String getDescription() { return description; }

    /**
     * Sets the event description.
     *
     * @param description the descriptive text for the event.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Returns the event location.
     *
     * @return the venue or place for the event.
     */
    public String getLocation() { return location; }

    /**
     * Sets the event location.
     *
     * @param location the venue or place for the event.
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * Returns the event date label.
     *
     * @return the date string currently stored for the event.
     */
    public String getDate() { return date; }

    /**
     * Sets the event date label.
     *
     * @param date the date string to store.
     */
    public void setDate(String date) { this.date = date; }

    /**
     * Returns the event time label.
     *
     * @return the time string currently stored for the event.
     */
    public String getTime() { return time; }

    /**
     * Sets the event time label.
     *
     * @param time the time string to store.
     */
    public void setTime(String time) { this.time = time; }

    /**
     * Returns the organizer ID associated with the event.
     *
     * @return the organizer's identifier.
     */
    public String getOrganizerId() { return organizerId; }

    /**
     * Sets the organizer ID associated with the event.
     *
     * @param organizerId the organizer's identifier.
     */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /**
     * Returns the configured attendee limit.
     *
     * @return the maximum number of attendees allowed.
     */
    public int getMaxCapacity() { return maxCapacity; }

    /**
     * Sets the configured attendee limit.
     *
     * @param maxCapacity the maximum number of attendees allowed.
     */
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    /**
     * Returns the ticket price shown in the UI.
     *
     * @return the stored ticket price, where {@code 0.0} means free.
     */
    public double getTicketPrice() { return ticketPrice; }

    /**
     * Sets the ticket price shown in the UI.
     *
     * @param ticketPrice the ticket price to store.
     */
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }

    /**
     * Returns the current attendee ID list.
     *
     * @return the list of student IDs attached to this event.
     */
    public List<String> getAttendeeIds() { return attendeeIds; }

    /**
     * Replaces the attendee ID list.
     *
     * @param attendeeIds the attendee IDs that should now belong to the event.
     */
    public void setAttendeeIds(List<String> attendeeIds) { this.attendeeIds = attendeeIds; }

    /**
     * Adds a student ID to the attendee list if it is not already present.
     *
     * @param studentId the student ID to add.
     */
    public void addAttendee(String studentId) {
        if (!this.attendeeIds.contains(studentId)) this.attendeeIds.add(studentId);
    }

    /**
     * Removes a student ID from the attendee list.
     *
     * @param studentId the student ID to remove.
     */
    public void removeAttendee(String studentId) {
        this.attendeeIds.remove(studentId);
    }
}
