package com.example.debugz;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lightweight SharedPreferences-based session that persists the current user's identity
 * across app restarts without requiring Firebase Auth.
 *
 * Three roles are supported:
 *   STUDENT   — campus student browsing and RSVPing to events.
 *   ORGANIZER — club / department organizer who creates and manages events.
 *   ADMIN     — platform administrator who moderates events and approves signups.
 */
public class UserSession {

    // ── Role constants ────────────────────────────────────────────────────
    public static final String ROLE_STUDENT   = "STUDENT";
    public static final String ROLE_ORGANIZER = "ORGANIZER";
    public static final String ROLE_ADMIN     = "ADMIN";

    /** Hardcoded admin credentials for this prototype. */
    public static final String ADMIN_USERNAME = "sefinalboss";
    public static final String ADMIN_PASSWORD = "1234";

    // ── SharedPreferences keys ────────────────────────────────────────────
    private static final String PREFS_NAME  = "debugz_session";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAME    = "userName";
    private static final String KEY_ROLE    = "userRole";

    // ── Singleton ─────────────────────────────────────────────────────────
    private static UserSession instance;

    private final SharedPreferences prefs;

    private UserSession(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static UserSession getInstance(Context context) {
        if (instance == null) {
            instance = new UserSession(context);
        }
        return instance;
    }

    // ── Session state ─────────────────────────────────────────────────────

    /** @return true if a user has already completed the login form. */
    public boolean isLoggedIn() {
        return !getRole().isEmpty();
    }

    /** @return the stored user/org/admin ID, or an empty string if not set. */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    /** @return the stored display name, or an empty string if not set. */
    public String getUserName() {
        return prefs.getString(KEY_NAME, "");
    }

    /** @return one of {@link #ROLE_STUDENT}, {@link #ROLE_ORGANIZER}, {@link #ROLE_ADMIN}, or "". */
    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public boolean isStudent()   { return ROLE_STUDENT.equals(getRole()); }
    public boolean isOrganizer() { return ROLE_ORGANIZER.equals(getRole()); }
    public boolean isAdmin()     { return ROLE_ADMIN.equals(getRole()); }

    // ── Session lifecycle ─────────────────────────────────────────────────

    /**
     * Persists the user's identity so subsequent app launches skip the login screen.
     *
     * @param userId   roll number / org ID / "admin"
     * @param userName display name
     * @param role     one of the ROLE_* constants
     */
    public void login(String userId, String userName, String role) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_NAME, userName)
                .putString(KEY_ROLE, role)
                .apply();
    }

    /** Clears all stored session data. User will be returned to the landing screen. */
    public void logout() {
        prefs.edit().clear().apply();
    }
}
