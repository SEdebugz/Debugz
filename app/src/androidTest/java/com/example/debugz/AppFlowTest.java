package com.example.debugz;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.debugz.models.Account;
import com.example.debugz.models.Event;
import com.example.debugz.models.Registration;
import com.example.debugz.models.Organizer;
import com.example.debugz.view.SignupActivity;
import com.example.debugz.view.AdminDashboardActivity;
import com.example.debugz.view.OrganizerDashboardActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ROLE: Instrumentation / Intent Tests.
 * PURPOSE: Verifies critical multi-user flows using ActivityScenario only.
 * No Espresso onView() calls — avoids InputManager crash on API 34/35.
 * No scenario.getState() inside onActivity() — avoids main-thread violation.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppFlowTest {

    @Before
    public void clearSession() {
        UserSession.getInstance(
                ApplicationProvider.getApplicationContext()).logout();
    }

    // ── US15: Admin dashboard ─────────────────────────────────────────────────

    /**
     * Verifies AdminDashboardActivity launches without crashing.
     * US15: Administrator manages event listings.
     */
    @Test
    public void testAdminFlow_DashboardLaunches() {
        UserSession.getInstance(ApplicationProvider.getApplicationContext())
                .login("admin", "Master Admin", UserSession.ROLE_ADMIN);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AdminDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<AdminDashboardActivity> scenario =
                     ActivityScenario.launch(intent)) {
            assertNotNull("AdminDashboardActivity scenario must not be null", scenario);
        }
    }

    /**
     * Verifies the admin session role is correctly set.
     * US15: Only ADMIN role can access moderation features.
     */
    @Test
    public void testAdminFlow_SessionRoleIsAdmin() {
        UserSession session = UserSession.getInstance(
                ApplicationProvider.getApplicationContext());
        session.login("admin", "Master Admin", UserSession.ROLE_ADMIN);

        assertTrue("isAdmin() should return true", session.isAdmin());
        assertEquals("ADMIN", session.getRole());
        assertEquals("admin", session.getUserId());
    }

    // ── US1: Signup screen ────────────────────────────────────────────────────

    /**
     * Verifies SignupActivity launches without crashing.
     * Students and organizers access this to create accounts.
     */
    @Test
    public void testStudentFlow_SignupActivityLaunches() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario =
                     ActivityScenario.launch(intent)) {
            assertNotNull("SignupActivity scenario must not be null", scenario);
        }
    }

    // ── US4 / US5: RSVP model integrity ──────────────────────────────────────

    /**
     * Verifies Registration object is created correctly on RSVP.
     * Mirrors EventDetailActivity.handleRSVP() data flow.
     * US4: Student reserves a place at an event.
     */
    @Test
    public void testStudentFlow_RSVPCreatesRegistration() {
        String studentId = "27100284";
        String eventId   = "event_nasher_2026";
        String regId     = eventId + "_" + studentId;

        Registration reg = new Registration(regId, studentId, eventId,
                "Confirmed", System.currentTimeMillis());

        assertNotNull(reg);
        assertEquals(regId,       reg.getRegistrationId());
        assertEquals(studentId,   reg.getStudentId());
        assertEquals(eventId,     reg.getEventId());
        assertEquals("Confirmed", reg.getStatus());
        assertTrue(reg.getTimestamp() > 0);
    }

    /**
     * Verifies RSVP is blocked when event is at capacity.
     * Mirrors the transaction guard in EventDetailActivity.handleRSVP().
     * US5: Students see remaining spots; RSVP blocked when full.
     */
    @Test
    public void testStudentFlow_RSVPBlockedWhenFull() {
        Event event = new Event("event_001", "Nasher Night", "Music gala",
                "ACF", "May 20, 2026", "7:00 PM", "org_001", 3, "Free", "Performance");

        event.addAttendee("27100001");
        event.addAttendee("27100002");
        event.addAttendee("27100003");

        boolean shouldBlock = event.getAttendeeIds().size() >= event.getMaxCapacity();
        assertTrue("RSVP must be blocked when full", shouldBlock);
        assertEquals(0, event.getMaxCapacity() - event.getAttendeeIds().size());
    }

    // ── US12 / US13: Organizer dashboard ─────────────────────────────────────

    /**
     * Verifies OrganizerDashboardActivity launches when session is ORGANIZER.
     * US12: Organizer accesses event management dashboard.
     */
    @Test
    public void testOrganizerFlow_DashboardLaunches() {
        UserSession.getInstance(ApplicationProvider.getApplicationContext())
                .login("music_society", "Music Society", UserSession.ROLE_ORGANIZER);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                OrganizerDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<OrganizerDashboardActivity> scenario =
                     ActivityScenario.launch(intent)) {
            assertNotNull("OrganizerDashboard scenario must not be null", scenario);
        }
    }

    /**
     * Verifies organizer session role is correctly identified.
     * US12/US13: Only ORGANIZER role can create/edit events.
     */
    @Test
    public void testOrganizerFlow_SessionRoleIsOrganizer() {
        UserSession session = UserSession.getInstance(
                ApplicationProvider.getApplicationContext());
        session.login("music_society", "Music Society", UserSession.ROLE_ORGANIZER);

        assertTrue("isOrganizer() should return true", session.isOrganizer());
        assertEquals("ORGANIZER", session.getRole());
    }

    /**
     * Verifies event is created with correct capacity and linked to organizer.
     * Mirrors EditEventActivity.saveEvent() + EventController.createEvent().
     * US13: Organizer sets event capacity/ticket limits.
     */
    @Test
    public void testOrganizerFlow_EventCreatedWithCapacity() {
        Organizer organizer = new Organizer(
                "music_society", "Music Society", "music@lums.edu.pk");

        Event event = new Event("event_jazz_001", "Jazz Night 2026",
                "An evening of smooth jazz.", "ACF",
                "May 25, 2026", "7:00 PM",
                organizer.getOrganizerId(), 100, "Free", "Performance");

        organizer.addCreatedEvent(event.getEventId());

        assertEquals(100, event.getMaxCapacity());
        assertEquals("music_society", event.getOrganizerId());
        assertTrue(organizer.getCreatedEventIds().contains("event_jazz_001"));
        assertEquals(0, event.getAttendeeIds().size());
    }

    // ── US14: Organizer views attendees ───────────────────────────────────────

    /**
     * Verifies attendee list reflects all RSVPs correctly.
     * Mirrors AttendeesActivity.loadAttendees() query result.
     * US14: Organizer views list of RSVPed attendees.
     */
    @Test
    public void testOrganizerFlow_AttendeeListReflectsRSVPs() {
        Event event = new Event("event_jazz_001", "Jazz Night 2026",
                "An evening of smooth jazz.", "ACF",
                "May 25, 2026", "7:00 PM",
                "music_society", 100, "Free", "Performance");

        String[] students = {"27100284","27100189","27100247","27100195","27100271"};
        for (String id : students) event.addAttendee(id);

        assertEquals(5, event.getAttendeeIds().size());
        for (String id : students)
            assertTrue("Missing: " + id, event.getAttendeeIds().contains(id));
        assertEquals(95, event.getMaxCapacity() - event.getAttendeeIds().size());
    }

    // ── Signup / approval flow ────────────────────────────────────────────────

    /**
     * Verifies new student account starts as PENDING.
     * Mirrors AccountController.submitSignup() behaviour.
     */
    @Test
    public void testSignupFlow_NewAccountIsPending() {
        Account account = new Account(
                "27100284", "Abdul Moeez Khurshid",
                "27100284@lums.edu.pk", "password123",
                UserSession.ROLE_STUDENT, Account.STATUS_PENDING,
                System.currentTimeMillis());

        assertTrue(account.isPending());
        assertEquals(Account.STATUS_PENDING, account.getStatus());
        assertEquals(UserSession.ROLE_STUDENT, account.getRole());
    }

    /**
     * Verifies admin approval changes account status to APPROVED.
     * Mirrors AdminDashboardActivity + AccountController.updateStatus().
     */
    @Test
    public void testSignupFlow_AdminApprovalChangesStatus() {
        Account account = new Account(
                "27100284", "Abdul Moeez Khurshid",
                "27100284@lums.edu.pk", "password123",
                UserSession.ROLE_STUDENT, Account.STATUS_PENDING,
                System.currentTimeMillis());

        account.setStatus(Account.STATUS_APPROVED);

        assertTrue(account.isApproved());
        assertEquals(Account.STATUS_APPROVED, account.getStatus());
    }
}