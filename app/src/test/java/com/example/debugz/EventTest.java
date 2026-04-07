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
 * Unit tests for the Event model class.
 * Covers constructor initialization, getters/setters, attendee management,
 * and capacity logic — relevant to US1, US3, US4, US5, US13.
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
                200
        );
    }

    // ──────────────────────────────────────────────
    // Constructor Tests
    // ──────────────────────────────────────────────

    @Test
    public void testParameterizedConstructor_setsAllFields() {
        assertEquals("event_001", event.getEventId());
        assertEquals("Engineering Career Fair", event.getTitle());
        assertEquals("Meet top employers and find internships.", event.getDescription());
        assertEquals("Main Hall", event.getLocation());
        assertEquals("March 15, 2026", event.getDate());
        assertEquals("10:00 AM", event.getTime());
        assertEquals("org_cso", event.getOrganizerId());
        assertEquals(200, event.getMaxCapacity());
    }

    @Test
    public void testParameterizedConstructor_initializesEmptyAttendeeList() {
        assertNotNull("Attendee list should not be null", event.getAttendeeIds());
        assertTrue("Attendee list should start empty", event.getAttendeeIds().isEmpty());
    }

    @Test
    public void testDefaultConstructor_initializesEmptyAttendeeList() {
        Event defaultEvent = new Event();
        assertNotNull("Default attendee list should not be null", defaultEvent.getAttendeeIds());
        assertTrue("Default attendee list should be empty", defaultEvent.getAttendeeIds().isEmpty());
    }

    @Test
    public void testDefaultConstructor_fieldsAreDefaults() {
        Event defaultEvent = new Event();
        assertNull(defaultEvent.getEventId());
        assertNull(defaultEvent.getTitle());
        assertNull(defaultEvent.getDescription());
        assertNull(defaultEvent.getLocation());
        assertNull(defaultEvent.getDate());
        assertNull(defaultEvent.getTime());
        assertNull(defaultEvent.getOrganizerId());
        assertEquals(0, defaultEvent.getMaxCapacity());
    }

    // ──────────────────────────────────────────────
    // Getter / Setter Tests
    // ──────────────────────────────────────────────

    @Test
    public void testSetAndGetEventId() {
        event.setEventId("event_updated");
        assertEquals("event_updated", event.getEventId());
    }

    @Test
    public void testSetAndGetTitle() {
        event.setTitle("LUMUN 2026");
        assertEquals("LUMUN 2026", event.getTitle());
    }

    @Test
    public void testSetAndGetDescription() {
        event.setDescription("Premier Model UN conference at LUMS.");
        assertEquals("Premier Model UN conference at LUMS.", event.getDescription());
    }

    @Test
    public void testSetAndGetLocation() {
        event.setLocation("SDSB Auditorium");
        assertEquals("SDSB Auditorium", event.getLocation());
    }

    @Test
    public void testSetAndGetDate() {
        event.setDate("March 18, 2026");
        assertEquals("March 18, 2026", event.getDate());
    }

    @Test
    public void testSetAndGetTime() {
        event.setTime("09:00 AM");
        assertEquals("09:00 AM", event.getTime());
    }

    @Test
    public void testSetAndGetOrganizerId() {
        event.setOrganizerId("org_lumun");
        assertEquals("org_lumun", event.getOrganizerId());
    }

    @Test
    public void testSetAndGetMaxCapacity() {
        event.setMaxCapacity(650);
        assertEquals(650, event.getMaxCapacity());
    }

    // ──────────────────────────────────────────────
    // Attendee Management Tests (US4 – RSVP, US5 – spots remaining)
    // ──────────────────────────────────────────────

    @Test
    public void testAddAttendee_addsNewStudent() {
        event.addAttendee("stu_001");
        assertTrue("Attendee list should contain stu_001", event.getAttendeeIds().contains("stu_001"));
        assertEquals(1, event.getAttendeeIds().size());
    }

    @Test
    public void testAddAttendee_preventsDuplicates() {
        event.addAttendee("stu_001");
        event.addAttendee("stu_001");
        assertEquals("Should not add duplicate attendee", 1, event.getAttendeeIds().size());
    }

    @Test
    public void testAddAttendee_multipleDistinctStudents() {
        event.addAttendee("stu_001");
        event.addAttendee("stu_002");
        event.addAttendee("stu_003");
        assertEquals(3, event.getAttendeeIds().size());
    }

    @Test
    public void testRemoveAttendee_removesExistingStudent() {
        event.addAttendee("stu_001");
        event.addAttendee("stu_002");
        event.removeAttendee("stu_001");
        assertFalse(event.getAttendeeIds().contains("stu_001"));
        assertEquals(1, event.getAttendeeIds().size());
    }

    @Test
    public void testRemoveAttendee_nonExistentId_doesNothing() {
        event.addAttendee("stu_001");
        event.removeAttendee("stu_999");
        assertEquals("Removing a non-existent ID should not change the list", 1, event.getAttendeeIds().size());
    }

    @Test
    public void testSetAttendeeIds_replacesEntireList() {
        event.addAttendee("old_stu");
        List<String> newIds = Arrays.asList("stu_100", "stu_200");
        event.setAttendeeIds(newIds);
        assertEquals(2, event.getAttendeeIds().size());
        assertFalse(event.getAttendeeIds().contains("old_stu"));
        assertTrue(event.getAttendeeIds().contains("stu_200"));
    }

    // ──────────────────────────────────────────────
    // Capacity / Spots-Remaining Logic (US5, US13)
    // ──────────────────────────────────────────────

    @Test
    public void testSpotsRemaining_whenEmpty() {
        int spotsLeft = event.getMaxCapacity() - event.getAttendeeIds().size();
        assertEquals(200, spotsLeft);
    }

    @Test
    public void testSpotsRemaining_afterSomeRSVPs() {
        for (int i = 0; i < 50; i++) {
            event.addAttendee("stu_" + i);
        }
        int spotsLeft = event.getMaxCapacity() - event.getAttendeeIds().size();
        assertEquals(150, spotsLeft);
    }

    @Test
    public void testSpotsRemaining_atFullCapacity() {
        for (int i = 0; i < 200; i++) {
            event.addAttendee("stu_" + i);
        }
        int spotsLeft = event.getMaxCapacity() - event.getAttendeeIds().size();
        assertEquals(0, spotsLeft);
    }

    @Test
    public void testCapacityPercentage_calculation() {
        for (int i = 0; i < 160; i++) {
            event.addAttendee("stu_" + i);
        }
        int currentAttendees = event.getAttendeeIds().size();
        int max = event.getMaxCapacity();
        int progress = (int) (((float) currentAttendees / max) * 100);
        assertEquals(80, progress);
    }

    // ──────────────────────────────────────────────
    // Realistic Data Scenario Tests
    // ──────────────────────────────────────────────

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
                650
        );
        lumun.addAttendee("27100284");
        lumun.addAttendee("27100189");
        lumun.addAttendee("27100247");

        assertEquals("LUMUN 2026", lumun.getTitle());
        assertEquals(3, lumun.getAttendeeIds().size());
        assertEquals(647, lumun.getMaxCapacity() - lumun.getAttendeeIds().size());
    }

    @Test
    public void testRealisticEvent_smallStudyCircle() {
        Event studyCircle = new Event(
                "event_003",
                "Khokha Study Circle",
                "Group study session for CS360.",
                "Block C-209",
                "Tonight",
                "05:00 PM",
                "org_003",
                20
        );

        // Fill to capacity
        for (int i = 1; i <= 20; i++) {
            studyCircle.addAttendee("stu_" + i);
        }
        assertEquals(20, studyCircle.getAttendeeIds().size());
        assertEquals(0, studyCircle.getMaxCapacity() - studyCircle.getAttendeeIds().size());
    }

    @Test
    public void testFirestoreMapping_defaultConstructorThenSetFields() {
        // Simulates how Firestore deserializes: default constructor + setters
        Event firestoreEvent = new Event();
        firestoreEvent.setEventId("event_fs_001");
        firestoreEvent.setTitle("Firestore Test Event");
        firestoreEvent.setDescription("Testing Firebase mapping");
        firestoreEvent.setLocation("Lab 12");
        firestoreEvent.setDate("April 10, 2026");
        firestoreEvent.setTime("02:00 PM");
        firestoreEvent.setOrganizerId("org_fs");
        firestoreEvent.setMaxCapacity(30);
        firestoreEvent.setAttendeeIds(new ArrayList<>(Arrays.asList("stu_a", "stu_b")));

        assertEquals("event_fs_001", firestoreEvent.getEventId());
        assertEquals("Firestore Test Event", firestoreEvent.getTitle());
        assertEquals(30, firestoreEvent.getMaxCapacity());
        assertEquals(2, firestoreEvent.getAttendeeIds().size());
    }

    @Test
    public void testEditEventDetails_US12() {
        // US12: Organizer wants to edit event details to keep info updated
        event.setTitle("Engineering Career Fair 2026 – Updated");
        event.setLocation("Sports Complex – Hall A");
        event.setDate("March 20, 2026");
        event.setTime("11:00 AM");
        event.setMaxCapacity(300);

        assertEquals("Engineering Career Fair 2026 – Updated", event.getTitle());
        assertEquals("Sports Complex – Hall A", event.getLocation());
        assertEquals("March 20, 2026", event.getDate());
        assertEquals("11:00 AM", event.getTime());
        assertEquals(300, event.getMaxCapacity());
    }
}