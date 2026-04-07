package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the organizer entity used by the event-management side of the app.
 * This model stores organizer identity data and the event IDs that the organizer is
 * responsible for creating or maintaining.
 * Outstanding issues: organizer-specific permissions are enforced outside this model, and
 * email or event-list validation is not yet built into the class itself.
 */
public class Organizer {
    private String organizerId;
    private String name;
    private String email;
    private List<String> createdEventIds;

    /**
     * Creates an empty organizer instance for Firebase Firestore deserialization.
     */
    public Organizer() {
        this.createdEventIds = new ArrayList<>();
    }

    /**
     * Creates an organizer with the core profile details used by the app.
     *
     * @param organizerId The unique identifier for the organizer.
     * @param name        The organizer's name.
     * @param email       The organizer's contact email.
     */
    public Organizer(String organizerId, String name, String email) {
        this.organizerId = organizerId;
        this.name = name;
        this.email = email;
        this.createdEventIds = new ArrayList<>();
    }

    /**
     * Returns the organizer's unique identifier.
     *
     * @return the organizer ID.
     */
    public String getOrganizerId() { return organizerId; }

    /**
     * Sets the organizer's unique identifier.
     *
     * @param organizerId the organizer ID to store.
     */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /**
     * Returns the organizer's display name.
     *
     * @return the organizer name.
     */
    public String getName() { return name; }

    /**
     * Sets the organizer's display name.
     *
     * @param name the organizer name to store.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the organizer's contact email.
     *
     * @return the organizer email address.
     */
    public String getEmail() { return email; }

    /**
     * Sets the organizer's contact email.
     *
     * @param email the organizer email address to store.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the event IDs tracked for this organizer.
     *
     * @return the created event ID list.
     */
    public List<String> getCreatedEventIds() { return createdEventIds; }

    /**
     * Replaces the created event ID list.
     *
     * @param createdEventIds the event IDs to associate with the organizer.
     */
    public void setCreatedEventIds(List<String> createdEventIds) { this.createdEventIds = createdEventIds; }

    /**
     * Adds an event ID to the organizer's list if not already present.
     *
     * @param eventId the event ID to add.
     */
    public void addCreatedEvent(String eventId) {
        if (!this.createdEventIds.contains(eventId)) this.createdEventIds.add(eventId);
    }

    /**
     * Removes an event ID from the organizer's list.
     *
     * @param eventId the event ID to remove.
     */
    public void removeCreatedEvent(String eventId) {
        this.createdEventIds.remove(eventId);
    }
}
