package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an event organizer in the application.
 * Responsible for managing their profile and tracking the events they create.
 */
public class Organizer {
    private String organizerId;
    private String name;
    private String email;
    private List<String> createdEventIds;

    /**
     * Default constructor required for Firebase Firestore data mapping.
     */
    public Organizer() {
        this.createdEventIds = new ArrayList<>();
    }

    /**
     * Constructs a new Organizer with core details.
     *
     * @param organizerId The unique identifier for the organizer.
     * @param name        The organizer's name
     * @param email       The organizer's contact email.
     */
    public Organizer(String organizerId, String name, String email) {
        this.organizerId = organizerId;
        this.name = name;
        this.email = email;
        this.createdEventIds = new ArrayList<>();
    }

    /** Returns the organizer's unique identifier. */
    public String getOrganizerId() { return organizerId; }

    /** Sets the organizer's unique identifier. */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /** Returns the organizer's name. */
    public String getName() { return name; }

    /** Sets the organizer's name. */
    public void setName(String name) { this.name = name; }

    /** Returns the organizer's contact email. */
    public String getEmail() { return email; }

    /** Sets the organizer's contact email. */
    public void setEmail(String email) { this.email = email; }

    /** Returns the list of event IDs created by this organizer. */
    public List<String> getCreatedEventIds() { return createdEventIds; }

    /** Replaces the created events list with the provided list. */
    public void setCreatedEventIds(List<String> createdEventIds) { this.createdEventIds = createdEventIds; }

    /**
     * Adds an event ID to the organizer's list if not already present.
     *
     * @param eventId The event ID to add.
     */
    public void addCreatedEvent(String eventId) {
        if (!this.createdEventIds.contains(eventId)) this.createdEventIds.add(eventId);
    }

    /**
     * Removes an event ID from the organizer's list.
     *
     * @param eventId The event ID to remove.
     */
    public void removeCreatedEvent(String eventId) {
        this.createdEventIds.remove(eventId);
    }
}