package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debugz.NotificationHelper;
import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Entry point that handles role-based login.
 * If the user already completed the login form in a previous session, they are
 * redirected immediately to their role-specific screen without needing to re-enter details.
 *
 * Roles:
 *   Student   — name + roll number → Discover Events feed (US1–US11)
 *   Organizer — name + org ID      → Organizer Dashboard  (US12–US14)
 *   Admin     — name + admin password ("admin2026") → Admin Dashboard (US15)
 */
public class LandingActivity extends AppCompatActivity {

    private TextInputEditText etUserName, etUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise notification channel (safe to call repeatedly — idempotent)
        NotificationHelper.createChannel(this);

        // If already logged in, skip the form and go straight to the right screen
        UserSession session = UserSession.getInstance(this);
        if (session.isLoggedIn()) {
            navigateByRole(session.getRole());
            return;
        }

        setContentView(R.layout.activity_landing);

        etUserName = findViewById(R.id.etUserName);
        etUserId   = findViewById(R.id.etUserId);

        Button btnStudent   = findViewById(R.id.btnStudentLogin);
        Button btnOrganizer = findViewById(R.id.btnOrganizerLogin);
        Button btnAdmin     = findViewById(R.id.btnAdminLogin);

        btnStudent.setOnClickListener(v   -> attemptLogin(UserSession.ROLE_STUDENT));
        btnOrganizer.setOnClickListener(v -> attemptLogin(UserSession.ROLE_ORGANIZER));
        btnAdmin.setOnClickListener(v     -> attemptLogin(UserSession.ROLE_ADMIN));
    }

    // ── Login logic ────────────────────────────────────────────────────────

    private void attemptLogin(String role) {
        String name = getInput(etUserName);
        String id   = getInput(etUserId);

        if (TextUtils.isEmpty(name)) {
            etUserName.setError("Name is required");
            return;
        }

        if (role.equals(UserSession.ROLE_ADMIN)) {
            // Admin: validate secret password
            if (!UserSession.ADMIN_PASSWORD.equals(id)) {
                etUserId.setError("Incorrect admin password");
                Toast.makeText(this, "Incorrect admin password", Toast.LENGTH_SHORT).show();
                return;
            }
            UserSession.getInstance(this).login("admin", name, UserSession.ROLE_ADMIN);
        } else {
            // Student / Organizer: require a non-empty ID
            if (TextUtils.isEmpty(id)) {
                etUserId.setError("ID is required");
                return;
            }
            UserSession.getInstance(this).login(id, name, role);
        }

        navigateByRole(role);
    }

    private void navigateByRole(String role) {
        Intent intent;
        switch (role) {
            case UserSession.ROLE_ORGANIZER:
                intent = new Intent(this, OrganizerDashboardActivity.class);
                break;
            case UserSession.ROLE_ADMIN:
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                intent = new Intent(this, MainActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }

    private String getInput(TextInputEditText et) {
        CharSequence text = et.getText();
        return text != null ? text.toString().trim() : "";
    }
}
