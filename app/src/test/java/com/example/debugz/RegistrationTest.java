package com.example.debugz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.debugz.models.Registration;

import org.junit.Before;
import org.junit.Test;

/**
 * Verifies the Registration model's public API, including status transitions and timestamp
 * storage for the RSVP workflow.
 * Outstanding issues: the suite does not enforce a restricted set of status values because
 * the current model stores statuses as unchecked strings.
 */
public class RegistrationTest {

    private Registration registration;
    private final long testTimestamp = 1712500000000L; // Realistic epoch ms (approx Apr 7 2025)

    @Before
    public void setUp() {
        registration = new Registration(
                "reg_001",
                "stu_001",
                "event_001",
                "Confirmed",
                testTimestamp
        );
    }

    // ──────────────────────────────────────────────
    // Constructor Tests
    // ──────────────────────────────────────────────

    @Test
    public void testParameterizedConstructor_setsAllFields() {
        assertEquals("reg_001", registration.getRegistrationId());
        assertEquals("stu_001", registration.getStudentId());
        assertEquals("event_001", registration.getEventId());
        assertEquals("Confirmed", registration.getStatus());
        assertEquals(testTimestamp, registration.getTimestamp());
    }

    @Test
    public void testDefaultConstructor_fieldsAreDefaults() {
        Registration defaultReg = new Registration();
        assertNull(defaultReg.getRegistrationId());
        assertNull(defaultReg.getStudentId());
        assertNull(defaultReg.getEventId());
        assertNull(defaultReg.getStatus());
        assertEquals(0L, defaultReg.getTimestamp());
    }

    // ──────────────────────────────────────────────
    // Getter / Setter Tests
    // ──────────────────────────────────────────────

    @Test
    public void testSetAndGetRegistrationId() {
        registration.setRegistrationId("reg_updated");
        assertEquals("reg_updated", registration.getRegistrationId());
    }

    @Test
    public void testSetAndGetStudentId() {
        registration.setStudentId("stu_999");
        assertEquals("stu_999", registration.getStudentId());
    }

    @Test
    public void testSetAndGetEventId() {
        registration.setEventId("event_999");
        assertEquals("event_999", registration.getEventId());
    }

    @Test
    public void testSetAndGetStatus_confirmed() {
        registration.setStatus("Confirmed");
        assertEquals("Confirmed", registration.getStatus());
    }

    @Test
    public void testSetAndGetStatus_waitlisted() {
        registration.setStatus("Waitlisted");
        assertEquals("Waitlisted", registration.getStatus());
    }

    @Test
    public void testSetAndGetStatus_checkedIn() {
        registration.setStatus("Checked-in");
        assertEquals("Checked-in", registration.getStatus());
    }

    @Test
    public void testSetAndGetStatus_cancelled() {
        registration.setStatus("Cancelled");
        assertEquals("Cancelled", registration.getStatus());
    }

    @Test
    public void testSetAndGetTimestamp() {
        long newTimestamp = 1712600000000L;
        registration.setTimestamp(newTimestamp);
        assertEquals(newTimestamp, registration.getTimestamp());
    }

    // ──────────────────────────────────────────────
    // Realistic Data Scenario Tests
    // ──────────────────────────────────────────────

    @Test
    public void testRealisticRSVP_careerFair() {
        Registration rsvp = new Registration(
                "reg_cf_001",
                "27100284",
                "event_career_fair_spring_2026",
                "Confirmed",
                System.currentTimeMillis()
        );
        assertEquals("27100284", rsvp.getStudentId());
        assertEquals("event_career_fair_spring_2026", rsvp.getEventId());
        assertEquals("Confirmed", rsvp.getStatus());
        assertTrue("Timestamp should be positive", rsvp.getTimestamp() > 0);
    }

    @Test
    public void testRealisticRSVP_waitlisted() {
        Registration rsvp = new Registration(
                "reg_lumun_042",
                "27100189",
                "event_lumun_2026",
                "Waitlisted",
                1743000000000L
        );
        assertEquals("Waitlisted", rsvp.getStatus());
        assertEquals("event_lumun_2026", rsvp.getEventId());
    }

    @Test
    public void testStatusTransition_waitlistedToConfirmed() {
        Registration rsvp = new Registration(
                "reg_trans_001",
                "27100247",
                "event_003",
                "Waitlisted",
                testTimestamp
        );
        assertEquals("Waitlisted", rsvp.getStatus());

        // Spot opens up – organizer confirms
        rsvp.setStatus("Confirmed");
        assertEquals("Confirmed", rsvp.getStatus());
    }

    @Test
    public void testStatusTransition_confirmedToCheckedIn() {
        registration.setStatus("Checked-in");
        assertEquals("Checked-in", registration.getStatus());
    }

    @Test
    public void testFirestoreMapping_defaultConstructorThenSetFields() {
        // Simulates how Firestore deserializes: default constructor + setters
        Registration firestoreReg = new Registration();
        firestoreReg.setRegistrationId("reg_fs_001");
        firestoreReg.setStudentId("stu_fs_001");
        firestoreReg.setEventId("event_fs_001");
        firestoreReg.setStatus("Confirmed");
        firestoreReg.setTimestamp(1712500000000L);

        assertEquals("reg_fs_001", firestoreReg.getRegistrationId());
        assertEquals("stu_fs_001", firestoreReg.getStudentId());
        assertEquals("event_fs_001", firestoreReg.getEventId());
        assertEquals("Confirmed", firestoreReg.getStatus());
        assertEquals(1712500000000L, firestoreReg.getTimestamp());
    }
}

