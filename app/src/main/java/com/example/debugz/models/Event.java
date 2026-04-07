package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a campus event created by an Organizer.
 * Responsible for holding core event details and tracking attendees.
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
     * Default constructor required for Firebase Firestore data mapping.
     */
    public Event() {
        this.attendeeIds = new ArrayList<>();
    }

    /**
     * Constructs a new Event with core details.
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

    /** Returns the unique event identifier. */
    public String getEventId() { return eventId; }

    /** Sets the unique event identifier. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** Returns the title of the event. */
    public String getTitle() { return title; }

    /** Sets the title of the event. */
    public void setTitle(String title) { this.title = title; }

    /** Returns the description of the event. */
    public String getDescription() { return description; }

    /** Sets the description of the event. */
    public void setDescription(String description) { this.description = description; }

    /** Returns the location of the event. */
    public String getLocation() { return location; }

    /** Sets the location of the event. */
    public void setLocation(String location) { this.location = location; }

    /** Returns the date of the event. */
    public String getDate() { return date; }

    /** Sets the date of the event. */
    public void setDate(String date) { this.date = date; }

    /** Returns the time of the event. */
    public String getTime() { return time; }

    /** Sets the time of the event. */
    public void setTime(String time) { this.time = time; }

    /** Returns the ID of the organizer who created the event. */
    public String getOrganizerId() { return organizerId; }

    /** Sets the ID of the organizer who created the event. */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /** Returns the maximum capacity for the event. */
    public int getMaxCapacity() { return maxCapacity; }

    /** Sets the maximum capacity for the event. */
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    /** Setter and getter for ticketPrice **/
    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }

    /** Returns the list of student IDs attending the event. */
    public List<String> getAttendeeIds() { return attendeeIds; }

    /** Replaces the attendee list with the provided list. */
    public void setAttendeeIds(List<String> attendeeIds) { this.attendeeIds = attendeeIds; }

    /**
     * Adds a student ID to the attendees list if not already present.
     *
     * @param studentId The student ID to add.
     */
    public void addAttendee(String studentId) {
        if (!this.attendeeIds.contains(studentId)) this.attendeeIds.add(studentId);
    }

    /**
     * Removes a student ID from the attendees list.
     *
     * @param studentId The student ID to remove.
     */
    public void removeAttendee(String studentId) {
        this.attendeeIds.remove(studentId);
    }
}