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
 */
public class EventTest {

    private Event event;

    @Before
    public void setUp() {
        event = new Event("event_001", "Engineering Career Fair",
                "Meet top employers and find internships.", "Main Hall",
                "March 15, 2026", "10:00 AM", "org_cso", 200, "Free", "Talk");
    }

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
        assertEquals("Free", event.getPrice());
        assertEquals("Talk", event.getCategory());
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
        assertNull(defaultEvent.getPrice());
        assertNull(defaultEvent.getCategory());
        assertEquals(0, defaultEvent.getUpvoteCount());
        assertTrue(defaultEvent.getUpvotedBy().isEmpty());
    }

    @Test
    public void testSetAndGetPrice() {
        event.setPrice("500 PKR");
        assertEquals("500 PKR", event.getPrice());
    }

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
        event.setDescription("Updated description");
        assertEquals("Updated description", event.getDescription());
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
    public void testAddAttendee_preventsDuplicates() {
        event.addAttendee("stu_001");
        event.addAttendee("stu_001");
        assertEquals(1, event.getAttendeeIds().size());
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
        assertEquals(1, event.getAttendeeIds().size());
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

    @Test
    public void testSpotsRemaining_whenEmpty() {
        assertEquals(200, event.getMaxCapacity() - event.getAttendeeIds().size());
    }

    @Test
    public void testSpotsRemaining_afterSomeRSVPs() {
        for (int i = 0; i < 50; i++) {
            event.addAttendee("stu_" + i);
        }
        assertEquals(150, event.getMaxCapacity() - event.getAttendeeIds().size());
    }

    @Test
    public void testSpotsRemaining_atFullCapacity() {
        for (int i = 0; i < 200; i++) {
            event.addAttendee("stu_" + i);
        }
        assertEquals(0, event.getMaxCapacity() - event.getAttendeeIds().size());
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

    @Test
    public void testRealisticEvent_LUMUN() {
        Event lumun = new Event("event_002", "LUMUN 2026",
                "Premier Model UN conference.", "SDSB Auditorium",
                "March 18, 2026", "09:00 AM", "org_lumun", 650,
                "1500 PKR", "Talk");
        lumun.addAttendee("27100284");
        lumun.addAttendee("27100189");
        lumun.addAttendee("27100247");

        assertEquals("LUMUN 2026", lumun.getTitle());
        assertEquals(3, lumun.getAttendeeIds().size());
        assertEquals(647, lumun.getMaxCapacity() - lumun.getAttendeeIds().size());
    }

    @Test
    public void testRealisticEvent_smallStudyCircle() {
        Event studyCircle = new Event("id", "title", "desc", "loc", "date", "time", "org", 20, "Free", "Academic");
        for (int i = 0; i < 20; i++) {
            studyCircle.addAttendee("stu_" + i);
        }
        assertEquals(20, studyCircle.getAttendeeIds().size());
        assertEquals(0, studyCircle.getMaxCapacity() - studyCircle.getAttendeeIds().size());
    }

    @Test
    public void testFirestoreMapping_defaultConstructorThenSetFields() {
        Event fsEvent = new Event();
        fsEvent.setEventId("event_fs_001");
        fsEvent.setTitle("Firestore Test Event");
        fsEvent.setDescription("Testing Firebase mapping");
        fsEvent.setLocation("Lab 12");
        fsEvent.setDate("April 10, 2026");
        fsEvent.setTime("02:00 PM");
        fsEvent.setOrganizerId("org_fs");
        fsEvent.setMaxCapacity(30);
        fsEvent.setPrice("200 PKR");
        fsEvent.setCategory("Talk");

        assertEquals("event_fs_001", fsEvent.getEventId());
        assertEquals("Firestore Test Event", fsEvent.getTitle());
        assertEquals("Testing Firebase mapping", fsEvent.getDescription());
        assertEquals("Lab 12", fsEvent.getLocation());
        assertEquals("April 10, 2026", fsEvent.getDate());
        assertEquals("02:00 PM", fsEvent.getTime());
        assertEquals("org_fs", fsEvent.getOrganizerId());
        assertEquals(30, fsEvent.getMaxCapacity());
        assertEquals("200 PKR", fsEvent.getPrice());
        assertEquals("Talk", fsEvent.getCategory());
    }

    @Test
    public void testEditEventDetails_US12() {
        event.setTitle("Updated Title");
        event.setLocation("Updated Location");
        event.setDate("Updated Date");
        event.setTime("Updated Time");
        event.setMaxCapacity(300);

        assertEquals("Updated Title", event.getTitle());
        assertEquals("Updated Location", event.getLocation());
        assertEquals("Updated Date", event.getDate());
        assertEquals("Updated Time", event.getTime());
        assertEquals(300, event.getMaxCapacity());
    }

    @Test
    public void testUpvote_initialCountIsZero() {
        assertEquals(0, event.getUpvoteCount());
        assertTrue(event.getUpvotedBy().isEmpty());
    }

    @Test
    public void testAddUpvote_incrementsCount() {
        boolean added = event.addUpvote("stu_001");
        assertTrue(added);
        assertEquals(1, event.getUpvoteCount());
        assertTrue(event.getUpvotedBy().contains("stu_001"));
    }

    @Test
    public void testAddUpvote_preventsDuplicate() {
        event.addUpvote("stu_001");
        boolean addedAgain = event.addUpvote("stu_001");
        assertFalse(addedAgain);
        assertEquals(1, event.getUpvoteCount());
    }

    @Test
    public void testAddUpvote_multipleStudents() {
        event.addUpvote("stu_001");
        event.addUpvote("stu_002");
        event.addUpvote("stu_003");
        assertEquals(3, event.getUpvoteCount());
    }

    @Test
    public void testRemoveUpvote_decrementsCount() {
        event.addUpvote("stu_001");
        boolean removed = event.removeUpvote("stu_001");
        assertTrue(removed);
        assertEquals(0, event.getUpvoteCount());
        assertFalse(event.getUpvotedBy().contains("stu_001"));
    }

    @Test
    public void testRemoveUpvote_nonExistentStudent_isNoOp() {
        event.addUpvote("stu_001");
        boolean removed = event.removeUpvote("stu_999");
        assertFalse(removed);
        assertEquals(1, event.getUpvoteCount());
    }

    @Test
    public void testUpvoteCountFloor_doesNotGoBelowZero() {
        event.removeUpvote("stu_001");
        assertEquals(0, event.getUpvoteCount());
    }
}
