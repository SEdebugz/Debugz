package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the account entity used for student and organizer login and admin approval.
 * Stores credentials, approval status, and social graph data (friends and friend requests).
 * Outstanding issues: password is stored as plain text; not acceptable for production.
 * No email format validation is enforced at the model level.
 *
 * ROLE: Model.
 */
public class Account {

    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private String accountId;
    private String name;
    private String email;
    private String password;
    private String role;
    private String status;
    private long createdAt;
    private List<String> friendIds;
    private List<String> friendRequests;

    /**
     * Creates an empty Account instance for Firestore deserialization.
     * Initialises friendIds and friendRequests to empty lists.
     */
    public Account() {
        this.friendIds = new ArrayList<>();
        this.friendRequests = new ArrayList<>();
    }

    /**
     * Creates an Account with all required fields.
     *
     * @param accountId  Unique identifier; roll number for students, org ID for organizers.
     * @param name       Display name of the student or organizer.
     * @param email      Contact email address; may be empty if not provided at signup.
     * @param password   Plain-text password; prototype only, not for production use.
     * @param role       One of {@link com.example.debugz.UserSession#ROLE_STUDENT}
     *                   or {@link com.example.debugz.UserSession#ROLE_ORGANIZER}.
     * @param status     Initial approval status; typically {@link #STATUS_PENDING} at signup.
     * @param createdAt  Epoch millisecond timestamp of account creation.
     */
    public Account(String accountId, String name, String email, String password,
                   String role, String status, long createdAt) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.friendIds = new ArrayList<>();
        this.friendRequests = new ArrayList<>();
    }

    /** @return the unique account identifier (roll number or org ID). */
    public String getAccountId() { return accountId; }

    /** @param accountId the account identifier to store. */
    public void setAccountId(String accountId) { this.accountId = accountId; }

    /** @return the display name of the student or organizer. */
    public String getName() { return name; }

    /** @param name the display name to store. */
    public void setName(String name) { this.name = name; }

    /** @return the contact email address; may be null if not provided at signup. */
    public String getEmail() { return email; }

    /** @param email the email address to store. */
    public void setEmail(String email) { this.email = email; }

    /**
     * @return the plain-text password.
     * Note: stored as plain text in this prototype only; not acceptable for production.
     */
    public String getPassword() { return password; }

    /** @param password the plain-text password to store. */
    public void setPassword(String password) { this.password = password; }

    /**
     * @return the role string for this account.
     * One of {@link com.example.debugz.UserSession#ROLE_STUDENT}
     * or {@link com.example.debugz.UserSession#ROLE_ORGANIZER}.
     */
    public String getRole() { return role; }

    /** @param role the role string to store. */
    public void setRole(String role) { this.role = role; }

    /**
     * @return the approval status of this account.
     * One of {@link #STATUS_PENDING}, {@link #STATUS_APPROVED}, or {@link #STATUS_REJECTED}.
     */
    public String getStatus() { return status; }

    /** @param status the approval status to store. */
    public void setStatus(String status) { this.status = status; }

    /** @return the epoch millisecond timestamp when this account was created. */
    public long getCreatedAt() { return createdAt; }

    /** @param createdAt the creation timestamp in epoch milliseconds to store. */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /** @return the list of account IDs this user has accepted as friends. */
    public List<String> getFriendIds() {
        return friendIds != null ? friendIds : new ArrayList<>();
    }

    /** @param friendIds the full friend ID list to store. */
    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds != null ? friendIds : new ArrayList<>();
    }

    /**
     * @return the list of account IDs who have sent this user a pending friend request.
     * Populated by AddFriendActivity via Firestore arrayUnion.
     */
    public List<String> getFriendRequests() {
        return friendRequests != null ? friendRequests : new ArrayList<>();
    }

    /** @param friendRequests the pending friend request ID list to store. */
    public void setFriendRequests(List<String> friendRequests) {
        this.friendRequests = friendRequests != null ? friendRequests : new ArrayList<>();
    }

    /** @return true if this account is awaiting admin approval. */
    public boolean isPending() { return STATUS_PENDING.equals(status); }

    /** @return true if this account has been approved and can log in. */
    public boolean isApproved() { return STATUS_APPROVED.equals(status); }

    /** @return true if this account has been rejected by an admin. */
    public boolean isRejected() { return STATUS_REJECTED.equals(status); }
}