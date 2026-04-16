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
}
