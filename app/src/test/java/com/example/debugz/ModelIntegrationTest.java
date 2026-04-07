package com.example.debugz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.debugz.models.Event;
import com.example.debugz.models.Organizer;
import com.example.debugz.models.Registration;
import com.example.debugz.models.Student;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Integration-style unit tests that verify how the model classes work together
 * to support end-to-end flows described in the user stories and storyboards.
 *
 * These tests use realistic campus-event data to simulate:
 *   - Organizer creates event → Student discovers → Student RSVPs (Storyboard 1, 4)
 *   - Capacity tracking and waitlist scenario (Storyboard 10)
 *   - Student monitors RSVP'd events (US10)
 *   - Organizer edits event and views attendees (US12, US14)
 */
public class ModelIntegrationTest {

    private Organizer organizer;
    private Event careerFair;
    private Student faneez;
    private Student ahmed;
    private Student moeez;

    @Before
    public void setUp() {
        // Organizer: LUMS Career Services Office
        organizer = new Organizer("org_cso", "LUMS Career Services Office", "cso@lums.edu.pk");

        // Event: Engineering Career Fair — capacity of 3 (small for testing)
        careerFair = new Event(
                "event_cf_2026",
                "Engineering Career Fair 2026",
                "Meet top employers and find internships at LUMS Main Hall.",
                "Main Hall",
                "March 15, 2026",
                "10:00 AM",
                "org_cso",
                3
        );

        // Students
        faneez = new Student("27100247", "Faneez Zulfiqar Ali", "27100247@lums.edu.pk", "SBASSE", "Sophomore");
        ahmed = new Student("27100195", "Muhammad Ahmed", "27100195@lums.edu.pk", "SBASSE", "Junior");
        moeez = new Student("27100284", "Abdul Moeez Khurshid", "27100284@lums.edu.pk", "SBASSE", "Junior");
    }

    // ──────────────────────────────────────────────
    // Storyboard 4: Organizer Event Creation Flow
    // ──────────────────────────────────────────────

    @Test
    public void testOrganizerCreatesEvent_andTracksList() {
        organizer.addCreatedEvent(careerFair.getEventId());

        assertTrue(organizer.getCreatedEventIds().contains("event_cf_2026"));
        assertEquals(1, organizer.getCreatedEventIds().size());
        assertEquals("org_cso", careerFair.getOrganizerId());
    }

    // ──────────────────────────────────────────────
    // Storyboard 1: Student RSVP flow (US1 → US3 → US4)
    // ──────────────────────────────────────────────

    @Test
    public void testStudentRSVP_createsRegistrationAndUpdatesEventAndStudent() {
        // Student Faneez RSVPs to the Career Fair
        String regId = "reg_cf_faneez";
        Registration rsvp = new Registration(
                regId,
                faneez.getStudentId(),
                careerFair.getEventId(),
                "Confirmed",
                System.currentTimeMillis()
        );

        // Update Event attendee list
        careerFair.addAttendee(faneez.getStudentId());

        // Update Student registration list
        faneez.addRegistration(regId);

        // Assertions
        assertEquals("Confirmed", rsvp.getStatus());
        assertTrue(careerFair.getAttendeeIds().contains("27100247"));
        assertTrue(faneez.getRegistrationIds().contains("reg_cf_faneez"));
        assertEquals(1, careerFair.getAttendeeIds().size());
    }

    // ──────────────────────────────────────────────
    // US5 / US13: Capacity tracking
    // ──────────────────────────────────────────────

    @Test
    public void testSpotsRemaining_decreasesWithEachRSVP() {
        assertEquals(3, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        careerFair.addAttendee(faneez.getStudentId());
        assertEquals(2, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        careerFair.addAttendee(ahmed.getStudentId());
        assertEquals(1, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        careerFair.addAttendee(moeez.getStudentId());
        assertEquals(0, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());
    }

    // ──────────────────────────────────────────────
    // Storyboard 10: Waitlist and Capacity Flow
    // ──────────────────────────────────────────────

    @Test
    public void testWaitlistFlow_eventAtCapacityThenSpotOpens() {
        // Fill event to capacity
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(ahmed.getStudentId());
        careerFair.addAttendee(moeez.getStudentId());
        assertEquals(0, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        // New student wants to attend — event is full → Waitlisted
        Student huzayfah = new Student("27100271", "Huzayfah Abid", "27100271@lums.edu.pk", "SBASSE", "Sophomore");
        Registration waitlistReg = new Registration(
                "reg_cf_huzayfah",
                huzayfah.getStudentId(),
                careerFair.getEventId(),
                "Waitlisted",
                System.currentTimeMillis()
        );
        assertEquals("Waitlisted", waitlistReg.getStatus());

        // Ahmed cancels his RSVP — spot opens
        careerFair.removeAttendee(ahmed.getStudentId());
        ahmed.removeRegistration("reg_cf_ahmed");
        assertEquals(1, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        // Huzayfah is promoted from waitlist
        waitlistReg.setStatus("Confirmed");
        careerFair.addAttendee(huzayfah.getStudentId());
        huzayfah.addRegistration("reg_cf_huzayfah");

        assertEquals("Confirmed", waitlistReg.getStatus());
        assertTrue(careerFair.getAttendeeIds().contains("27100271"));
        assertEquals(0, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());
    }

    // ──────────────────────────────────────────────
    // US10: Student monitors RSVP'd events
    // ──────────────────────────────────────────────

    @Test
    public void testStudentTracksMultipleRSVPs() {
        Event lumun = new Event("event_lumun_2026", "LUMUN 2026", "Model UN",
                "SDSB Auditorium", "March 18, 2026", "09:00 AM", "org_lumun", 650);

        // Faneez RSVPs to both Career Fair and LUMUN
        faneez.addRegistration("reg_cf_faneez");
        faneez.addRegistration("reg_lumun_faneez");

        careerFair.addAttendee(faneez.getStudentId());
        lumun.addAttendee(faneez.getStudentId());

        assertEquals(2, faneez.getRegistrationIds().size());
        assertTrue(careerFair.getAttendeeIds().contains(faneez.getStudentId()));
        assertTrue(lumun.getAttendeeIds().contains(faneez.getStudentId()));
    }

    // ──────────────────────────────────────────────
    // US12: Organizer edits event details
    // ──────────────────────────────────────────────

    @Test
    public void testOrganizerEditsEvent_updatesDetails() {
        careerFair.setTitle("Engineering Career Fair 2026 – Updated");
        careerFair.setLocation("Sports Complex – Hall A");
        careerFair.setDate("March 20, 2026");
        careerFair.setTime("11:00 AM");
        careerFair.setMaxCapacity(500);

        assertEquals("Engineering Career Fair 2026 – Updated", careerFair.getTitle());
        assertEquals("Sports Complex – Hall A", careerFair.getLocation());
        assertEquals("March 20, 2026", careerFair.getDate());
        assertEquals(500, careerFair.getMaxCapacity());
    }

    // ──────────────────────────────────────────────
    // US14: Organizer views list of RSVP'd attendees
    // ──────────────────────────────────────────────

    @Test
    public void testOrganizerViewsAttendeeList() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(ahmed.getStudentId());
        careerFair.addAttendee(moeez.getStudentId());

        List<String> attendees = careerFair.getAttendeeIds();

        assertEquals(3, attendees.size());
        assertTrue(attendees.contains("27100247")); // Faneez
        assertTrue(attendees.contains("27100195")); // Ahmed
        assertTrue(attendees.contains("27100284")); // Moeez
    }

    // ──────────────────────────────────────────────
    // US13: Organizer sets capacity limits
    // ──────────────────────────────────────────────

    @Test
    public void testCapacityLimit_canBeUpdatedByOrganizer() {
        assertEquals(3, careerFair.getMaxCapacity());
        careerFair.setMaxCapacity(500);
        assertEquals(500, careerFair.getMaxCapacity());
    }

    // ──────────────────────────────────────────────
    // Edge Cases
    // ──────────────────────────────────────────────

    @Test
    public void testDuplicateRSVP_doesNotDuplicateAttendee() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(faneez.getStudentId());
        assertEquals(1, careerFair.getAttendeeIds().size());
    }

    @Test
    public void testRemoveNonExistentAttendee_doesNotAffectList() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.removeAttendee("nonexistent_student_id");
        assertEquals(1, careerFair.getAttendeeIds().size());
    }

    @Test
    public void testStudentCancelsRSVP_bothListsUpdated() {
        String regId = "reg_cf_faneez";
        careerFair.addAttendee(faneez.getStudentId());
        faneez.addRegistration(regId);

        // Cancel
        careerFair.removeAttendee(faneez.getStudentId());
        faneez.removeRegistration(regId);

        assertFalse(careerFair.getAttendeeIds().contains(faneez.getStudentId()));
        assertFalse(faneez.getRegistrationIds().contains(regId));
        assertEquals(0, careerFair.getAttendeeIds().size());
        assertEquals(0, faneez.getRegistrationIds().size());
    }
}


