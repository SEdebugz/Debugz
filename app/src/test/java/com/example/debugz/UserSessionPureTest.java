package com.example.debugz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Verifies the compile-time constants and pure helper logic exported by UserSession.
 * SharedPreferences behaviour cannot be tested in JVM unit tests (requires Android runtime),
 * so this suite focuses on the role constants and the admin password constant.
 */
public class UserSessionPureTest {

    @Test
    public void testRoleConstants_areDistinct() {
        assertFalse(UserSession.ROLE_STUDENT.equals(UserSession.ROLE_ORGANIZER));
        assertFalse(UserSession.ROLE_STUDENT.equals(UserSession.ROLE_ADMIN));
        assertFalse(UserSession.ROLE_ORGANIZER.equals(UserSession.ROLE_ADMIN));
    }

    @Test
    public void testAdminPassword_notEmpty() {
        assertFalse("Admin password should not be empty",
                UserSession.ADMIN_PASSWORD.isEmpty());
    }

    @Test
    public void testRoleConstants_haveExpectedValues() {
        assertEquals("STUDENT",   UserSession.ROLE_STUDENT);
        assertEquals("ORGANIZER", UserSession.ROLE_ORGANIZER);
        assertEquals("ADMIN",     UserSession.ROLE_ADMIN);
    }

    @Test
    public void testAdminPassword_matchesExpected() {
        assertEquals("admin2026", UserSession.ADMIN_PASSWORD);
    }
}

