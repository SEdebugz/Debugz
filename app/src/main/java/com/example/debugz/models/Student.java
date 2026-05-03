package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the student entity used by the browsing and RSVP flows in the app.
 * This model stores the student's profile, interest tags, and registration references so
 * other layers can personalize discovery and track the student's saved events.
 * Outstanding issues: the model does not validate school, year, or preference values, so
 * callers can still persist inconsistent profile data.
 */
public class Student {
    private String studentId;
    private String name;
    private String email;
    private String school;
    private String year;
    private List<String> preferences;
    private List<String> registrationIds;
    private List<String> friendIds;

    /**
     * Creates an empty student instance for Firebase Firestore deserialization.
     */
    public Student() {
        this.preferences = new ArrayList<>();
        this.registrationIds = new ArrayList<>();
        this.friendIds = new ArrayList<>();
    }


    /**
     * Creates a student with the core profile details used by the app.
     *
     * @param studentId  The unique identifier for the student.
     * @param name       The student's full name.
     * @param email      The student's university email address.
     * @param school     The school or department the student belongs to.
     * @param year       The student's current academic year.
     */
    public Student(String studentId, String name, String email, String school, String year) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.school = school;
        this.year = year;
        this.preferences = new ArrayList<>();
        this.registrationIds = new ArrayList<>();
    }

    /**
     * Returns the student's unique identifier.
     *
     * @return the student ID.
     */
    public String getStudentId() { return studentId; }

    /**
     * Sets the student's unique identifier.
     *
     * @param studentId the student ID to store.
     */
    public void setStudentId(String studentId) { this.studentId = studentId; }

    /**
     * Returns the student's full name.
     *
     * @return the student name.
     */
    public String getName() { return name; }

    /**
     * Sets the student's full name.
     *
     * @param name the student name to store.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the student's university email address.
     *
     * @return the student email address.
     */
    public String getEmail() { return email; }

    /**
     * Sets the student's university email address.
     *
     * @param email the student email address to store.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the student's school or department.
     *
     * @return the school or department name.
     */
    public String getSchool() { return school; }

    /**
     * Sets the student's school or department.
     *
     * @param school the school or department name to store.
     */
    public void setSchool(String school) { this.school = school; }

    /**
     * Returns the student's current academic year.
     *
     * @return the academic year label.
     */
    public String getYear() { return year; }

    /**
     * Sets the student's current academic year.
     *
     * @param year the academic year label to store.
     */
    public void setYear(String year) { this.year = year; }

    /**
     * Returns the student's preferred event tags.
     *
     * @return the preference tag list.
     */
    public List<String> getPreferences() { return preferences; }

    /**
     * Replaces the student's preferred event tags.
     *
     * @param preferences the preference tags to store.
     */
    public void setPreferences(List<String> preferences) { this.preferences = preferences; }

    /**
     * Adds a category tag to the student's preferences if not already present.
     *
     * @param tag the category tag to add.
     */
    public void addPreference(String tag) {
        if (!this.preferences.contains(tag)) this.preferences.add(tag);
    }

    /**
     * Returns the registration IDs associated with the student.
     *
     * @return the student's registration ID list.
     */
    public List<String> getRegistrationIds() { return registrationIds; }

    /**
     * Replaces the student's registration IDs.
     *
     * @param registrationIds the registration IDs to store.
     */
    public void setRegistrationIds(List<String> registrationIds) { this.registrationIds = registrationIds; }

    /**
     * Adds a registration ID to the student's list if not already present.
     *
     * @param registrationId the registration ID to add.
     */
    public void addRegistration(String registrationId) {
        if (!this.registrationIds.contains(registrationId)) this.registrationIds.add(registrationId);
    }

    /**
     * Removes a registration ID from the student's list.
     *
     * @param registrationId the registration ID to remove.
     */
    public void removeRegistration(String registrationId) {
        this.registrationIds.remove(registrationId);
    }

    /**
     * Returns friends of a student
     *
     * @return the student's friends ids
     */
    public List<String> getFriendIds() { return friendIds; }

    /**
     * Replaces the friends of a student
     */
    public void setFriendIds(List<String> friendIds) { this.friendIds = friendIds; }
}
