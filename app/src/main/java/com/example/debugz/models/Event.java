package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a campus event created by an Organizer.
 * Responsible for holding event details, managing attendees, and enforcing capacity limits.
 */
public class Event {
    private String eventId;
    private String title;
    private String description;
    private String location;
    private String date;
    private String time;
    private String organizerId;
    private int maxCapacity;
    private List<String> attendeeIds;
    private List<String> waitlistIds;

    /**
     * Default constructor required for Firebase Firestore data mapping.
     */
    public Event() {
        this.attendeeIds = new ArrayList<>();
        this.waitlistIds = new ArrayList<>();
    }

    /**
     * Constructs a new Event with specified core details.
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
    public Event(String eventId, String title, String description, String location, String date, String time, String organizerId, int maxCapacity) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.time = time;
        this.organizerId = organizerId;
        this.maxCapacity = maxCapacity;
        this.attendeeIds = new ArrayList<>();
        this.waitlistIds = new ArrayList<>();
    }

    /**
     * Gets the unique event ID.
     * @return The event ID string.
     */
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the title of the event.
     * @return The event title string.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the description of the event.
     * @return The event description string.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the location of the event.
     * @return The location string.
     */
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the date of the event.
     * @return The date string.
     */
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Gets the time of the event.
     * @return The time string.
     */
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Gets the ID of the organizer who created the event.
     * @return The organizer ID string.
     */
    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Gets the maximum capacity for the event.
     * @return The maximum number of attendees as an integer.
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * Gets the list of student IDs currently attending the event.
     * @return A list of attendee ID strings.
     */
    public List<String> getAttendeeIds() {
        return attendeeIds;
    }

    public void setAttendeeIds(List<String> attendeeIds) {
        this.attendeeIds = attendeeIds;
    }

    /**
     * Gets the list of student IDs on the waitlist.
     * @return A list of waitlist ID strings.
     */
    public List<String> getWaitlistIds() {
        return waitlistIds;
    }

    public void setWaitlistIds(List<String> waitlistIds) {
        this.waitlistIds = waitlistIds;
    }

    /**
     * Adds a student ID to the attendees list if there is capacity.
     * @param studentId The ID of the student to add.
     */
    public void addAttendee(String studentId) {
        if (!this.attendeeIds.contains(studentId)) {
            this.attendeeIds.add(studentId);
        }
    }

    /**
     * Adds a student ID to the waitlist.
     * @param studentId The ID of the student to add.
     */
    public void addWaitlist(String studentId) {
        if (!this.waitlistIds.contains(studentId)) {
            this.waitlistIds.add(studentId);
        }
    }
}