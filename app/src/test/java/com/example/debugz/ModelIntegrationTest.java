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

/**
 * Exercises how the core model classes collaborate.
 * Updated with US2 Category and String Price.
 */
public class ModelIntegrationTest {

    private Organizer organizer;
    private Event careerFair;
    private Student faneez;

    @Before
    public void setUp() {
        organizer = new Organizer("org_cso", "LUMS Career Services Office", "cso@lums.edu.pk");

        careerFair = new Event(
                "event_cf_2026",
                "Engineering Career Fair 2026",
                "Meet top employers.",
                "Main Hall",
                "March 15, 2026",
                "10:00 AM",
                "org_cso",
                3,
                "Free",
                "Academic"
        );

        faneez = new Student("27100247", "Faneez Zulfiqar Ali", "27100247@lums.edu.pk", "SBASSE", "Sophomore");
    }

    @Test
    public void testStudentRSVP_Flow() {
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

        assertTrue(careerFair.getAttendeeIds().contains("27100247"));
        assertEquals(1, careerFair.getAttendeeIds().size());
        assertEquals("Academic", careerFair.getCategory());
    }
}
