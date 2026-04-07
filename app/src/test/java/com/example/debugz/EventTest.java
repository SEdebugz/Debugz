package com.example.debugz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.debugz.models.Event;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the Event model class.
 * Tests core functionality like attendee management and getters/setters.
 */
public class EventTest {

    private Event event;

    @Before
    public void setUp() {
        // Initializing with the new constructor including the price field
        event = new Event("1", "Test Event", "Description", "Location", "2023-10-10", "10:00 AM", "org1", 100, "Free");
    }

    @Test
    public void testAddAttendee() {
        event.addAttendee("student123");
        assertTrue("Attendee list should contain student123", event.getAttendeeIds().contains("student123"));
        assertEquals("Size should be 1", 1, event.getAttendeeIds().size());
    }

    @Test
    public void testAddDuplicateAttendee() {
        event.addAttendee("student123");
        event.addAttendee("student123");
        assertEquals("Size should still be 1 after adding duplicate", 1, event.getAttendeeIds().size());
    }

    @Test
    public void testRemoveAttendee() {
        event.addAttendee("student123");
        event.removeAttendee("student123");
        assertFalse("Attendee list should not contain student123", event.getAttendeeIds().contains("student123"));
        assertEquals("Size should be 0", 0, event.getAttendeeIds().size());
    }

    @Test
    public void testGettersAndSetters() {
        event.setTitle("New Title");
        assertEquals("New Title", event.getTitle());
        event.setMaxCapacity(50);
        assertEquals(50, event.getMaxCapacity());
        event.setPrice("1000 PKR");
        assertEquals("1000 PKR", event.getPrice());
    }
}