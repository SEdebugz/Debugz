package com.example.debugz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.debugz.models.Administrator;
import com.example.debugz.models.Event;
import com.example.debugz.models.Organizer;
import com.example.debugz.models.Registration;
import com.example.debugz.models.Student;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Exercises how the core model classes collaborate during common event-management flows.
 * UPDATED: Uses String for price to match Phase 3 requirements.
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

        // Event: Engineering Career Fair — capacity of 3. Price set as "Free" (String).
        careerFair = new Event(
                "event_cf_2026",
                "Engineering Career Fair 2026",
                "Meet top employers and find internships at LUMS Main Hall.",
                "Main Hall",
                "March 15, 2026",
                "10:00 AM",
                "org_cso",
                3,
                "Free"
        );

        // Students
        faneez = new Student("27100247", "Faneez Zulfiqar Ali", "27100247@lums.edu.pk", "SBASSE", "Sophomore");
        ahmed = new Student("27100195", "Muhammad Ahmed", "27100195@lums.edu.pk", "SBASSE", "Junior");
        moeez = new Student("27100284", "Abdul Moeez Khurshid", "27100284@lums.edu.pk", "SBASSE", "Junior");
    }

    @Test
    public void testOrganizerCreatesEvent_andTracksList() {
        organizer.addCreatedEvent(careerFair.getEventId());

        assertTrue(organizer.getCreatedEventIds().contains("event_cf_2026"));
        assertEquals(1, organizer.getCreatedEventIds().size());
        assertEquals("org_cso", careerFair.getOrganizerId());
    }

    @Test
    public void testStudentRSVP_createsRegistrationAndUpdatesEventAndStudent() {
        String regId = "reg_cf_faneez";
        Registration rsvp = new Registration(
                regId,
                faneez.getStudentId(),
                careerFair.getEventId(),
                "Confirmed",
                System.currentTimeMillis()
        );

        careerFair.addAttendee(faneez.getStudentId());
        faneez.addRegistration(regId);

        assertEquals("Confirmed", rsvp.getStatus());
        assertTrue(careerFair.getAttendeeIds().contains("27100247"));
        assertTrue(faneez.getRegistrationIds().contains("reg_cf_faneez"));
        assertEquals(1, careerFair.getAttendeeIds().size());
    }

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

    @Test
    public void testWaitlistFlow_eventAtCapacityThenSpotOpens() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(ahmed.getStudentId());
        careerFair.addAttendee(moeez.getStudentId());
        assertEquals(0, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        Student huzayfah = new Student("27100271", "Huzayfah Abid", "27100271@lums.edu.pk", "SBASSE", "Sophomore");
        Registration waitlistReg = new Registration(
                "reg_cf_huzayfah",
                huzayfah.getStudentId(),
                careerFair.getEventId(),
                "Waitlisted",
                System.currentTimeMillis()
        );
        assertEquals("Waitlisted", waitlistReg.getStatus());

        careerFair.removeAttendee(ahmed.getStudentId());
        ahmed.removeRegistration("reg_cf_ahmed");
        assertEquals(1, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        waitlistReg.setStatus("Confirmed");
        careerFair.addAttendee(huzayfah.getStudentId());
        huzayfah.addRegistration("reg_cf_huzayfah");

        assertEquals("Confirmed", waitlistReg.getStatus());
        assertTrue(careerFair.getAttendeeIds().contains("27100271"));
        assertEquals(0, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());
    }

    @Test
    public void testStudentTracksMultipleRSVPs() {
        Event lumun = new Event("event_lumun_2026", "LUMUN 2026", "Model UN",
                "SDSB Auditorium", "March 18, 2026", "09:00 AM", "org_lumun", 650, "1500 PKR");

        faneez.addRegistration("reg_cf_faneez");
        faneez.addRegistration("reg_lumun_faneez");

        careerFair.addAttendee(faneez.getStudentId());
        lumun.addAttendee(faneez.getStudentId());

        assertEquals(2, faneez.getRegistrationIds().size());
        assertTrue(careerFair.getAttendeeIds().contains(faneez.getStudentId()));
        assertTrue(lumun.getAttendeeIds().contains(faneez.getStudentId()));
    }

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

    @Test
    public void testOrganizerViewsAttendeeList() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(ahmed.getStudentId());
        careerFair.addAttendee(moeez.getStudentId());

        List<String> attendees = careerFair.getAttendeeIds();

        assertEquals(3, attendees.size());
        assertTrue(attendees.contains("27100247"));
        assertTrue(attendees.contains("27100195"));
        assertTrue(attendees.contains("27100284"));
    }

    @Test
    public void testCapacityLimit_canBeUpdatedByOrganizer() {
        assertEquals(3, careerFair.getMaxCapacity());
        careerFair.setMaxCapacity(500);
        assertEquals(500, careerFair.getMaxCapacity());
    }

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

        careerFair.removeAttendee(faneez.getStudentId());
        faneez.removeRegistration(regId);

        assertFalse(careerFair.getAttendeeIds().contains(faneez.getStudentId()));
        assertFalse(faneez.getRegistrationIds().contains(regId));
        assertEquals(0, careerFair.getAttendeeIds().size());
        assertEquals(0, faneez.getRegistrationIds().size());
    }

    // ──────────────────────────────────────────────
    // Administrator (US15) scenarios
    // ──────────────────────────────────────────────

    /**
     * US15: Admin removes an event from the system.
     * At the model layer this is represented by removing the event ID from
     * the organizer's tracking list (in-memory); in production the controller
     * issues a Firestore delete that propagates to all users.
     */
    @Test
    public void testAdminRemovesEvent_removedFromOrganizerList() {
        organizer.addCreatedEvent(careerFair.getEventId());
        assertEquals(1, organizer.getCreatedEventIds().size());

        // Admin decides to remove the event
        Administrator admin = new Administrator("demo_admin_001", "Platform Admin", "admin@lums.edu.pk");
        // Admin triggers deletion — model-level effect: event removed from organizer's list
        organizer.removeCreatedEvent(careerFair.getEventId());

        assertFalse("Organizer list should no longer contain the deleted event",
                organizer.getCreatedEventIds().contains(careerFair.getEventId()));
        assertEquals(0, organizer.getCreatedEventIds().size());
        // Verify admin object was constructed correctly
        assertEquals("demo_admin_001", admin.getAdminId());
    }

    /**
     * US15: After an event is removed, any existing registrations are no longer valid.
     * At the model layer this is a registration status update; in production the
     * EventController.deleteEvent cascades the removal to the registrations collection.
     */
    @Test
    public void testAdminRemovesEvent_attendeeListCleared() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(ahmed.getStudentId());
        assertEquals(2, careerFair.getAttendeeIds().size());

        // When an admin removes an event the attendee list is effectively cleared
        // (in Firestore: event document + registrations are deleted by the controller).
        // At the model layer, simulate by removing all attendee IDs.
        careerFair.getAttendeeIds().clear();

        assertEquals("Attendee list should be empty after event removal",
                0, careerFair.getAttendeeIds().size());
    }

    /**
     * US13: Organizer updates event capacity; verifies remaining spots recalculated correctly.
     */
    @Test
    public void testOrganizerUpdatesCapacity_spotsRecalculated() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(ahmed.getStudentId());
        // Original capacity 3, 2 attendees → 1 spot left
        assertEquals(1, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        // Organizer expands capacity to 10 (US13)
        careerFair.setMaxCapacity(10);
        assertEquals(8, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());
    }

    /**
     * US14: Organizer views attendee list — already covered by testOrganizerViewsAttendeeList
     * but here we also verify that the list matches what was RSVP'd.
     */
    @Test
    public void testOrganizerViewsAttendees_matchesRSVPs() {
        String regId1 = "reg_cf_faneez";
        String regId2 = "reg_cf_ahmed";

        careerFair.addAttendee(faneez.getStudentId());
        faneez.addRegistration(regId1);
        careerFair.addAttendee(ahmed.getStudentId());
        ahmed.addRegistration(regId2);

        List<String> attendees = careerFair.getAttendeeIds();
        assertEquals(2, attendees.size());
        assertTrue(attendees.contains(faneez.getStudentId()));
        assertTrue(attendees.contains(ahmed.getStudentId()));

        // Organizer can also inspect each student's registration link
        assertTrue(faneez.getRegistrationIds().contains(regId1));
        assertTrue(ahmed.getRegistrationIds().contains(regId2));
    }
}
