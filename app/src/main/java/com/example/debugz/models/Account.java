package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * ROLE: Model.
 * Updated to support Friend Requests (incoming request IDs).
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
    private List<String> friendRequests; // Added: List of student IDs who sent a request to this user

    public Account() {
        this.friendIds = new ArrayList<>();
        this.friendRequests = new ArrayList<>();
    }

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

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<String> getFriendIds() {
        return friendIds != null ? friendIds : new ArrayList<>();
    }
    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds != null ? friendIds : new ArrayList<>();
    }

    public List<String> getFriendRequests() {
        return friendRequests != null ? friendRequests : new ArrayList<>();
    }
    public void setFriendRequests(List<String> friendRequests) {
        this.friendRequests = friendRequests != null ? friendRequests : new ArrayList<>();
    }

    public boolean isPending() { return STATUS_PENDING.equals(status); }
    public boolean isApproved() { return STATUS_APPROVED.equals(status); }
    public boolean isRejected() { return STATUS_REJECTED.equals(status); }
}
