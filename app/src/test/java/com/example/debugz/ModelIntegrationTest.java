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
 * Integration tests for the model layer collaboration.
 */
public class ModelIntegrationTest {

    private Organizer organizer;
    private Event careerFair;
    private Student faneez;
    private Student ahmed;
    private Student moeez;

    @Before
    public void setUp() {
        organizer = new Organizer("org_cso", "LUMS Career Services Office", "cso@lums.edu.pk");

        careerFair = new Event("event_cf_2026", "Engineering Career Fair 2026",
                "Meet top employers and find internships at LUMS Main Hall.",
                "Main Hall", "March 15, 2026", "10:00 AM", "org_cso", 3, "Free", "Talk");

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
        long timestamp = System.currentTimeMillis();
        String regId = "reg_cf_faneez";
        Registration rsvp = new Registration(regId, faneez.getStudentId(), careerFair.getEventId(), "Confirmed", timestamp);

        careerFair.addAttendee(faneez.getStudentId());
        faneez.addRegistration(regId);

        assertEquals("Confirmed", rsvp.getStatus());
        assertTrue(careerFair.getAttendeeIds().contains(faneez.getStudentId()));
        assertTrue(faneez.getRegistrationIds().contains(regId));
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
        Registration waitlistReg = new Registration("reg_wait", huzayfah.getStudentId(), careerFair.getEventId(), "Waitlisted", System.currentTimeMillis());
        assertEquals("Waitlisted", waitlistReg.getStatus());

        careerFair.removeAttendee(ahmed.getStudentId());
        assertEquals(1, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        waitlistReg.setStatus("Confirmed");
        careerFair.addAttendee(huzayfah.getStudentId());
        huzayfah.addRegistration("reg_wait");

        assertTrue(careerFair.getAttendeeIds().contains(huzayfah.getStudentId()));
        assertEquals("Confirmed", waitlistReg.getStatus());
        assertEquals(0, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());
    }

    @Test
    public void testStudentTracksMultipleRSVPs() {
        Event lumun = new Event("event_lumun", "LUMUN 2026", "Model UN", "SDSB", "Date", "Time", "org_lumun", 650, "1500", "Talk");
        
        careerFair.addAttendee(faneez.getStudentId());
        faneez.addRegistration("reg_cf");
        
        lumun.addAttendee(faneez.getStudentId());
        faneez.addRegistration("reg_lumun");

        assertEquals(2, faneez.getRegistrationIds().size());
        assertTrue(careerFair.getAttendeeIds().contains(faneez.getStudentId()));
        assertTrue(lumun.getAttendeeIds().contains(faneez.getStudentId()));
    }

    @Test
    public void testOrganizerEditsEvent_updatesDetails() {
        careerFair.setTitle("Updated Title");
        careerFair.setLocation("Updated Hall");
        careerFair.setDate("Updated Date");
        careerFair.setTime("Updated Time");
        careerFair.setMaxCapacity(500);

        assertEquals("Updated Title", careerFair.getTitle());
        assertEquals("Updated Hall", careerFair.getLocation());
        assertEquals("Updated Date", careerFair.getDate());
        assertEquals("Updated Time", careerFair.getTime());
        assertEquals(500, careerFair.getMaxCapacity());
    }

    @Test
    public void testOrganizerViewsAttendeeList() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(ahmed.getStudentId());
        careerFair.addAttendee(moeez.getStudentId());

        List<String> attendees = careerFair.getAttendeeIds();
        assertEquals(3, attendees.size());
        assertTrue(attendees.contains(faneez.getStudentId()));
        assertTrue(attendees.contains(ahmed.getStudentId()));
        assertTrue(attendees.contains(moeez.getStudentId()));
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
        careerFair.removeAttendee("invalid_id");
        assertEquals(1, careerFair.getAttendeeIds().size());
    }

    @Test
    public void testStudentCancelsRSVP_bothListsUpdated() {
        String regId = "reg_01";
        careerFair.addAttendee(faneez.getStudentId());
        faneez.addRegistration(regId);

        careerFair.removeAttendee(faneez.getStudentId());
        faneez.removeRegistration(regId);

        assertFalse(careerFair.getAttendeeIds().contains(faneez.getStudentId()));
        assertFalse(faneez.getRegistrationIds().contains(regId));
        assertEquals(0, careerFair.getAttendeeIds().size());
        assertEquals(0, faneez.getRegistrationIds().size());
    }

    @Test
    public void testAdminRemovesEvent_removedFromOrganizerList() {
        organizer.addCreatedEvent(careerFair.getEventId());
        Administrator admin = new Administrator("demo_admin_001", "Platform Admin", "admin@lums.edu.pk");
        
        organizer.removeCreatedEvent(careerFair.getEventId());
        
        assertFalse(organizer.getCreatedEventIds().contains(careerFair.getEventId()));
        assertEquals(0, organizer.getCreatedEventIds().size());
        assertEquals("demo_admin_001", admin.getAdminId());
    }

    @Test
    public void testAdminRemovesEvent_attendeeListCleared() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(ahmed.getStudentId());

        careerFair.getAttendeeIds().clear();
        assertEquals(0, careerFair.getAttendeeIds().size());
    }

    @Test
    public void testOrganizerUpdatesCapacity_spotsRecalculated() {
        careerFair.addAttendee(faneez.getStudentId());
        careerFair.addAttendee(ahmed.getStudentId());
        assertEquals(1, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());

        careerFair.setMaxCapacity(10);
        assertEquals(8, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());
    }

    @Test
    public void testOrganizerViewsAttendees_matchesRSVPs() {
        careerFair.addAttendee(faneez.getStudentId());
        faneez.addRegistration("reg1");
        careerFair.addAttendee(ahmed.getStudentId());
        ahmed.addRegistration("reg2");

        List<String> attendees = careerFair.getAttendeeIds();
        assertTrue(attendees.contains(faneez.getStudentId()));
        assertTrue(attendees.contains(ahmed.getStudentId()));
        assertTrue(faneez.getRegistrationIds().contains("reg1"));
        assertTrue(ahmed.getRegistrationIds().contains("reg2"));
    }
}
