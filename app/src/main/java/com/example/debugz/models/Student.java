package com.example.debugz.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student user of the application.
 * Responsible for managing core profile details, event preferences, and registrations.
 */
public class Student {
    private String studentId;
    private String name;
    private String email;
    private String school;
    private String year;
    private List<String> preferences;
    private List<String> registrationIds;

    /**
     * Default constructor required for Firebase Firestore data mapping.
     */
    public Student() {
        this.preferences = new ArrayList<>();
        this.registrationIds = new ArrayList<>();
    }

    /**
     * Constructs a new Student with core details.
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

    /** Returns the student's unique identifier. */
    public String getStudentId() { return studentId; }

    /** Sets the student's unique identifier. */
    public void setStudentId(String studentId) { this.studentId = studentId; }

    /** Returns the student's full name. */
    public String getName() { return name; }

    /** Sets the student's full name. */
    public void setName(String name) { this.name = name; }

    /** Returns the student's university email address. */
    public String getEmail() { return email; }

    /** Sets the student's university email address. */
    public void setEmail(String email) { this.email = email; }

    /** Returns the school or department the student belongs to. */
    public String getSchool() { return school; }

    /** Sets the school or department the student belongs to. */
    public void setSchool(String school) { this.school = school; }

    /** Returns the student's current academic year. */
    public String getYear() { return year; }

    /** Sets the student's current academic year. */
    public void setYear(String year) { this.year = year; }

    /** Returns the list of event category tags the student prefers. */
    public List<String> getPreferences() { return preferences; }

    /** Replaces the student's preference list with the provided list. */
    public void setPreferences(List<String> preferences) { this.preferences = preferences; }

    /**
     * Adds a category tag to the student's preferences if not already present.
     *
     * @param tag The category tag to add.
     */
    public void addPreference(String tag) {
        if (!this.preferences.contains(tag)) this.preferences.add(tag);
    }

    /** Returns the list of registration IDs associated with the student. */
    public List<String> getRegistrationIds() { return registrationIds; }

    /** Replaces the student's registration list with the provided list. */
    public void setRegistrationIds(List<String> registrationIds) { this.registrationIds = registrationIds; }

    /**
     * Adds a registration ID to the student's list if not already present.
     *
     * @param registrationId The registration ID to add.
     */
    public void addRegistration(String registrationId) {
        if (!this.registrationIds.contains(registrationId)) this.registrationIds.add(registrationId);
    }

    /**
     * Removes a registration ID from the student's list.
     *
     * @param registrationId The registration ID to remove.
     */
    public void removeRegistration(String registrationId) {
        this.registrationIds.remove(registrationId);
    }
}