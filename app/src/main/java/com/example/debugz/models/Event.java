package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the event entity used throughout the app.
 * Stores all event metadata, attendee tracking, and upvote state.
 * Outstanding issues: date and time are stored as freeform strings;
 * informal values like "Tonight" skip calendar and reminder automation.
 *
 * ROLE: Model.
 */
public class Event {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private String eventId;
    private String title;
    private String description;
    private String location;
    private String date;
    private String time;
    private String organizerId;
    private int maxCapacity;
    private String price;
    private String category;
    private String status;
    private List<String> attendeeIds;
    private int upvoteCount;
    private List<String> upvotedBy;

    /**
     * Creates an empty Event instance for Firestore deserialization.
     * Initialises attendeeIds and upvotedBy to empty lists and sets status to PENDING.
     */
    public Event() {
        this.attendeeIds = new ArrayList<>();
        this.upvotedBy = new ArrayList<>();
        this.status = STATUS_PENDING;
    }

    /**
     * Creates an Event with all required fields.
     *
     * @param eventId      Unique identifier; usually equals the Firestore document ID.
     * @param title        Display title of the event.
     * @param description  Full description shown in EventDetailActivity.
     * @param location     Venue name or room number.
     * @param date         Human-readable date string, e.g. "May 20, 2026".
     * @param time         Human-readable time string, e.g. "7:00 PM".
     * @param organizerId  UserSession.getUserId() of the creating organizer.
     * @param maxCapacity  Maximum number of attendees; 0 means unlimited.
     * @param price        Price string, e.g. "Free" or "500 PKR".
     * @param category     Category tag used for ChipGroup filtering in MainActivity.
     */
    public Event(String eventId, String title, String description, String location, String date, String time, String organizerId, int maxCapacity, String price, String category) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.time = time;
        this.organizerId = organizerId;
        this.maxCapacity = maxCapacity;
        this.price = price;
        this.category = category;
        this.status = STATUS_PENDING;
        this.attendeeIds = new ArrayList<>();
        this.upvoteCount = 0;
        this.upvotedBy = new ArrayList<>();
    }

    /** @return the unique event identifier. */
    public String getEventId() { return eventId; }

    /** @param eventId the event identifier to store. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return the display title of the event. */
    public String getTitle() { return title; }

    /** @param title the display title to store. */
    public void setTitle(String title) { this.title = title; }

    /** @return the full event description. */
    public String getDescription() { return description; }

    /** @param description the full description to store. */
    public void setDescription(String description) { this.description = description; }

    /** @return the venue name or room number. */
    public String getLocation() { return location; }

    /** @param location the venue name to store. */
    public void setLocation(String location) { this.location = location; }

    /** @return the human-readable date string. */
    public String getDate() { return date; }

    /** @param date the human-readable date string to store. */
    public void setDate(String date) { this.date = date; }

    /** @return the human-readable time string. */
    public String getTime() { return time; }

    /** @param time the human-readable time string to store. */
    public void setTime(String time) { this.time = time; }

    /** @return the organizer ID that created this event. */
    public String getOrganizerId() { return organizerId; }

    /** @param organizerId the organizer ID to store. */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /** @return the maximum number of attendees; 0 means unlimited. */
    public int getMaxCapacity() { return maxCapacity; }

    /** @param maxCapacity the capacity cap to store. */
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    /** @return the price string, e.g. "Free" or "500 PKR". */
    public String getPrice() { return price; }

    /** @param price the price string to store. */
    public void setPrice(String price) { this.price = price; }

    /** @return the category tag used for feed filtering. */
    public String getCategory() { return category; }

    /** @param category the category tag to store. */
    public void setCategory(String category) { this.category = category; }

    /**
     * @return the approval status of this event.
     * One of {@link #STATUS_PENDING}, {@link #STATUS_APPROVED}, or {@link #STATUS_REJECTED}.
     */
    public String getStatus() { return status; }

    /**
     * @param status the approval status to store.
     * Use the STATUS_* constants defined on this class.
     */
    public void setStatus(String status) { this.status = status; }

    /** @return the list of student IDs who have RSVP'd to this event. */
    public List<String> getAttendeeIds() {
        return attendeeIds != null ? attendeeIds : new ArrayList<>();
    }

    /** @param attendeeIds the full attendee ID list to store. */
    public void setAttendeeIds(List<String> attendeeIds) {
        this.attendeeIds = attendeeIds != null ? attendeeIds : new ArrayList<>();
    }

    /** @return the total number of upvotes this event has received. */
    public int getUpvoteCount() { return upvoteCount; }

    /** @param upvoteCount the upvote count to store. */
    public void setUpvoteCount(int upvoteCount) { this.upvoteCount = upvoteCount; }

    /** @return the list of student IDs who have upvoted this event. */
    public List<String> getUpvotedBy() {
        return upvotedBy != null ? upvotedBy : new ArrayList<>();
    }

    /** @param upvotedBy the full upvoter ID list to store. */
    public void setUpvotedBy(List<String> upvotedBy) {
        this.upvotedBy = upvotedBy != null ? upvotedBy : new ArrayList<>();
    }

    /**
     * Adds a student to the attendee list if not already present.
     * Called inside the Firestore transaction in EventDetailActivity.handleRSVP().
     *
     * @param studentId the student ID to add.
     */
    public void addAttendee(String studentId) {
        if (this.attendeeIds == null) this.attendeeIds = new ArrayList<>();
        if (!this.attendeeIds.contains(studentId)) this.attendeeIds.add(studentId);
    }

    /**
     * Removes a student from the attendee list.
     * Called when an RSVP is cancelled in EventDetailActivity.handleCancelRSVP().
     *
     * @param studentId the student ID to remove.
     */
    public void removeAttendee(String studentId) {
        if (this.attendeeIds != null) this.attendeeIds.remove(studentId);
    }

    /**
     * Records an upvote from a student if they have not already upvoted.
     * Increments upvoteCount and adds studentId to upvotedBy.
     *
     * @param studentId the student ID casting the upvote.
     * @return true if the upvote was added; false if the student had already upvoted.
     */
    public boolean addUpvote(String studentId) {
        if (this.upvotedBy == null) this.upvotedBy = new ArrayList<>();
        if (this.upvotedBy.contains(studentId)) return false;
        this.upvotedBy.add(studentId);
        this.upvoteCount++;
        return true;
    }

    /**
     * Removes an upvote from a student if one exists.
     * Decrements upvoteCount and removes studentId from upvotedBy.
     * upvoteCount will not go below zero.
     *
     * @param studentId the student ID removing their upvote.
     * @return true if the upvote was removed; false if no upvote existed.
     */
    public boolean removeUpvote(String studentId) {
        if (this.upvotedBy == null || !this.upvotedBy.contains(studentId)) return false;
        this.upvotedBy.remove(studentId);
        if (this.upvoteCount > 0) this.upvoteCount--;
        return true;
    }
}