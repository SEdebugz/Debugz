package com.example.debugz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.debugz.models.Organizer;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Verifies the Organizer model's public API, including construction, field mutation, and
 * created-event tracking used by organizer-facing workflows.
 * Outstanding issues: the suite does not exercise authorization or Firestore-backed
 * organizer scenarios because those concerns live outside the model class.
 */
public class OrganizerTest {

    private Organizer organizer;

    @Before
    public void setUp() {
        organizer = new Organizer("org_001", "LUMS Drama Club", "drama@lums.edu.pk");
    }

    // ──────────────────────────────────────────────
    // Constructor Tests
    // ──────────────────────────────────────────────

    @Test
    public void testParameterizedConstructor_setsAllFields() {
        assertEquals("org_001", organizer.getOrganizerId());
        assertEquals("LUMS Drama Club", organizer.getName());
        assertEquals("drama@lums.edu.pk", organizer.getEmail());
    }

    @Test
    public void testParameterizedConstructor_initializesEmptyEventList() {
        assertNotNull("Created event IDs list should not be null", organizer.getCreatedEventIds());
        assertTrue("Created event IDs list should start empty", organizer.getCreatedEventIds().isEmpty());
    }

    @Test
    public void testDefaultConstructor_initializesEmptyEventList() {
        Organizer defaultOrg = new Organizer();
        assertNotNull(defaultOrg.getCreatedEventIds());
        assertTrue(defaultOrg.getCreatedEventIds().isEmpty());
    }

    @Test
    public void testDefaultConstructor_fieldsAreNull() {
        Organizer defaultOrg = new Organizer();
        assertNull(defaultOrg.getOrganizerId());
        assertNull(defaultOrg.getName());
        assertNull(defaultOrg.getEmail());
    }

    // ──────────────────────────────────────────────
    // Getter / Setter Tests
    // ──────────────────────────────────────────────

    @Test
    public void testSetAndGetOrganizerId() {
        organizer.setOrganizerId("org_999");
        assertEquals("org_999", organizer.getOrganizerId());
    }

    @Test
    public void testSetAndGetName() {
        organizer.setName("LUMS Adventure Society");
        assertEquals("LUMS Adventure Society", organizer.getName());
    }

    @Test
    public void testSetAndGetEmail() {
        organizer.setEmail("adventure@lums.edu.pk");
        assertEquals("adventure@lums.edu.pk", organizer.getEmail());
    }

    // ──────────────────────────────────────────────
    // Created Event Management Tests
    // ──────────────────────────────────────────────

    @Test
    public void testAddCreatedEvent_addsNewEventId() {
        organizer.addCreatedEvent("event_001");
        assertTrue(organizer.getCreatedEventIds().contains("event_001"));
        assertEquals(1, organizer.getCreatedEventIds().size());
    }

    @Test
    public void testAddCreatedEvent_preventsDuplicates() {
        organizer.addCreatedEvent("event_001");
        organizer.addCreatedEvent("event_001");
        assertEquals("Should not add duplicate event ID", 1, organizer.getCreatedEventIds().size());
    }

    @Test
    public void testAddCreatedEvent_multipleDistinctEvents() {
        organizer.addCreatedEvent("event_001");
        organizer.addCreatedEvent("event_002");
        organizer.addCreatedEvent("event_003");
        assertEquals(3, organizer.getCreatedEventIds().size());
        assertTrue(organizer.getCreatedEventIds().contains("event_002"));
    }

    @Test
    public void testRemoveCreatedEvent_removesExistingId() {
        organizer.addCreatedEvent("event_001");
        organizer.addCreatedEvent("event_002");
        organizer.removeCreatedEvent("event_001");
        assertFalse(organizer.getCreatedEventIds().contains("event_001"));
        assertEquals(1, organizer.getCreatedEventIds().size());
    }

    @Test
    public void testRemoveCreatedEvent_nonExistentId_doesNothing() {
        organizer.addCreatedEvent("event_001");
        organizer.removeCreatedEvent("event_999");
        assertEquals("Removing a non-existent ID should not change the list", 1, organizer.getCreatedEventIds().size());
    }

    @Test
    public void testSetCreatedEventIds_replacesEntireList() {
        organizer.addCreatedEvent("old_event");
        List<String> newIds = Arrays.asList("event_100", "event_200", "event_300");
        organizer.setCreatedEventIds(newIds);
        assertEquals(3, organizer.getCreatedEventIds().size());
        assertFalse(organizer.getCreatedEventIds().contains("old_event"));
        assertTrue(organizer.getCreatedEventIds().contains("event_300"));
    }

    // ──────────────────────────────────────────────
    // Realistic Data Scenario Tests
    // ──────────────────────────────────────────────

    @Test
    public void testRealisticOrganizer_LUMSCareerServicesOffice() {
        Organizer cso = new Organizer(
                "org_cso",
                "LUMS Career Services Office",
                "cso@lums.edu.pk"
        );
        cso.addCreatedEvent("event_career_fair_spring_2026");
        cso.addCreatedEvent("event_resume_workshop");
        cso.addCreatedEvent("event_mock_interviews");

        assertEquals("LUMS Career Services Office", cso.getName());
        assertEquals(3, cso.getCreatedEventIds().size());
        assertTrue(cso.getCreatedEventIds().contains("event_career_fair_spring_2026"));
    }

    @Test
    public void testRealisticOrganizer_studentSociety() {
        Organizer lumun = new Organizer(
                "org_lumun",
                "LUMUN Society",
                "lumun@lums.edu.pk"
        );
        lumun.addCreatedEvent("event_lumun_2026");

        assertEquals("org_lumun", lumun.getOrganizerId());
        assertEquals(1, lumun.getCreatedEventIds().size());
    }
}
