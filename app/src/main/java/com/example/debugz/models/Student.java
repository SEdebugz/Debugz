package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student user of the application.
 * Responsible for managing profile details, social connections, event preferences, and registrations.
 */
public class Student {
    private String studentId;
    private String name;
    private String email;
    private String deviceToken;
    private List<String> preferences;
    private List<String> friendIds;
    private List<String> registrationIds;

    /**
     * Default constructor required for Firebase Firestore data mapping.
     */
    public Student() {
        this.preferences = new ArrayList<>();
        this.friendIds = new ArrayList<>();
        this.registrationIds = new ArrayList<>();
    }

    /**
     * Constructs a new Student with core details.
     *
     * @param studentId   The unique identifier for the student.
     * @param name        The student's full name.
     * @param email       The student's university email address.
     * @param deviceToken The token required for sending push notifications.
     */
    public Student(String studentId, String name, String email, String deviceToken) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.deviceToken = deviceToken;
        this.preferences = new ArrayList<>();
        this.friendIds = new ArrayList<>();
        this.registrationIds = new ArrayList<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    public void addPreference(String tag) {
        if (!this.preferences.contains(tag)) {
            this.preferences.add(tag);
        }
    }

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }

    public void addFriend(String friendId) {
        if (!this.friendIds.contains(friendId)) {
            this.friendIds.add(friendId);
        }
    }

    public List<String> getRegistrationIds() {
        return registrationIds;
    }

    public void setRegistrationIds(List<String> registrationIds) {
        this.registrationIds = registrationIds;
    }

    public void addRegistration(String registrationId) {
        if (!this.registrationIds.contains(registrationId)) {
            this.registrationIds.add(registrationId);
        }
    }
}