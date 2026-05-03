package com.example.debugz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.debugz.models.Event;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Verifies the Event model's public contract.
 * Updated for US2 (Category) and String prices.
 */
public class EventTest {

    private Event event;

    @Before
    public void setUp() {
        event = new Event(
                "event_001",
                "Engineering Career Fair",
                "Meet top employers and find internships.",
                "Main Hall",
                "March 15, 2026",
                "10:00 AM",
                "org_cso",
                200,
                "Free",
                "Academic"
        );
    }

    @Test
    public void testParameterizedConstructor_setsAllFields() {
        assertEquals("event_001", event.getEventId());
        assertEquals("Engineering Career Fair", event.getTitle());
        assertEquals("Academic", event.getCategory());
        assertEquals("Free", event.getPrice());
    }

    @Test
    public void testSetAndGetCategory() {
        event.setCategory("Sports");
        assertEquals("Sports", event.getCategory());
    }

    @Test
    public void testAddAttendee_addsNewStudent() {
        event.addAttendee("stu_001");
        assertTrue(event.getAttendeeIds().contains("stu_001"));
        assertEquals(1, event.getAttendeeIds().size());
    }

    @Test
    public void testRemoveAttendee() {
        event.addAttendee("stu_001");
        event.removeAttendee("stu_001");
        assertFalse(event.getAttendeeIds().contains("stu_001"));
    }

    @Test
    public void testRealisticEvent_LUMUN() {
        Event lumun = new Event(
                "event_002",
                "LUMUN 2026",
                "Premier Model UN conference.",
                "SDSB Auditorium",
                "March 18, 2026",
                "09:00 AM",
                "org_lumun",
                650,
                "1500 PKR",
                "Talk"
        );
        assertEquals("Talk", lumun.getCategory());
        assertEquals("1500 PKR", lumun.getPrice());
    }
}
