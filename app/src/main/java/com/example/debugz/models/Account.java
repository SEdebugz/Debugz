package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

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

    /** Required empty constructor for Firestore. */
    public Account() {
        this.friendIds = new ArrayList<>();
    }

    public Account(String accountId, String name, String email, String password,
                   String role, String status, long createdAt) {
        this.accountId  = accountId;
        this.name       = name;
        this.email      = email;
        this.password   = password;
        this.role       = role;
        this.status     = status;
        this.createdAt  = createdAt;
        this.friendIds  = new ArrayList<>();
    }

    public String getAccountId()              { return accountId; }
    public void   setAccountId(String v)      { this.accountId = v; }

    public String getName()                   { return name; }
    public void   setName(String v)           { this.name = v; }

    public String getEmail()                  { return email; }
    public void   setEmail(String v)          { this.email = v; }

    public String getPassword()               { return password; }
    public void   setPassword(String v)       { this.password = v; }

    public String getRole()                   { return role; }
    public void   setRole(String v)           { this.role = v; }

    public String getStatus()                 { return status; }
    public void   setStatus(String v)         { this.status = v; }

    public long   getCreatedAt()              { return createdAt; }
    public void   setCreatedAt(long v)        { this.createdAt = v; }

    public List<String> getFriendIds() {
        return friendIds != null ? friendIds : new ArrayList<>();
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds != null ? friendIds : new ArrayList<>();
    }

    public void addFriendId(String id) {
        if (this.friendIds == null) this.friendIds = new ArrayList<>();
        if (!this.friendIds.contains(id)) this.friendIds.add(id);
    }

    public void removeFriendId(String id) {
        if (this.friendIds != null) this.friendIds.remove(id);
    }

    public boolean isPending()  { return STATUS_PENDING.equals(status); }
    public boolean isApproved() { return STATUS_APPROVED.equals(status); }
    public boolean isRejected() { return STATUS_REJECTED.equals(status); }
}