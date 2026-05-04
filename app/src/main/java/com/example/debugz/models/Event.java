package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the event entity used throughout the app.
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

    public Event() {
        this.attendeeIds = new ArrayList<>();
        this.upvotedBy = new ArrayList<>();
        this.status = STATUS_PENDING;
    }

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

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getAttendeeIds() {
        return attendeeIds != null ? attendeeIds : new ArrayList<>();
    }
    public void setAttendeeIds(List<String> attendeeIds) {
        this.attendeeIds = attendeeIds != null ? attendeeIds : new ArrayList<>();
    }
    public int getUpvoteCount() { return upvoteCount; }
    public void setUpvoteCount(int upvoteCount) { this.upvoteCount = upvoteCount; }
    public List<String> getUpvotedBy() {
        return upvotedBy != null ? upvotedBy : new ArrayList<>();
    }
    public void setUpvotedBy(List<String> upvotedBy) {
        this.upvotedBy = upvotedBy != null ? upvotedBy : new ArrayList<>();
    }

    public void addAttendee(String studentId) {
        if (this.attendeeIds == null) this.attendeeIds = new ArrayList<>();
        if (!this.attendeeIds.contains(studentId)) this.attendeeIds.add(studentId);
    }
    public void removeAttendee(String studentId) {
        if (this.attendeeIds != null) this.attendeeIds.remove(studentId);
    }
    public boolean addUpvote(String studentId) {
        if (this.upvotedBy == null) this.upvotedBy = new ArrayList<>();
        if (this.upvotedBy.contains(studentId)) return false;
        this.upvotedBy.add(studentId);
        this.upvoteCount++;
        return true;
    }
    public boolean removeUpvote(String studentId) {
        if (this.upvotedBy == null || !this.upvotedBy.contains(studentId)) return false;
        this.upvotedBy.remove(studentId);
        if (this.upvoteCount > 0) this.upvoteCount--;
        return true;
    }
}
