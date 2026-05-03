package com.example.debugz.models;

/**
 * Defines the administrator entity used by the event-moderation side of the app.
 * Administrators have full rights to delete any event listing from the platform.
 * This model is intentionally minimal: permission enforcement lives in the view/controller layer.
 *
 * ROLE: Model.
 */
public class Administrator {
    private String adminId;
    private String name;
    private String email;

    /**
     * Creates an empty administrator instance for Firebase Firestore deserialization.
     */
    public Administrator() {
    }

    /**
     * Creates an administrator with the core identity details.
     *
     * @param adminId The unique identifier for the administrator.
     * @param name    The administrator's display name.
     * @param email   The administrator's contact email.
     */
    public Administrator(String adminId, String name, String email) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
    }

    /**
     * Returns the administrator's unique identifier.
     *
     * @return the admin ID.
     */
    public String getAdminId() { return adminId; }

    /**
     * Sets the administrator's unique identifier.
     *
     * @param adminId the admin ID to store.
     */
    public void setAdminId(String adminId) { this.adminId = adminId; }

    /**
     * Returns the administrator's display name.
     *
     * @return the admin name.
     */
    public String getName() { return name; }

    /**
     * Sets the administrator's display name.
     *
     * @param name the admin name to store.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the administrator's contact email.
     *
     * @return the admin email address.
     */
    public String getEmail() { return email; }

    /**
     * Sets the administrator's contact email.
     *
     * @param email the admin email address to store.
     */
    public void setEmail(String email) { this.email = email; }
}

