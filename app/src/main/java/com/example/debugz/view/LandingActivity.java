package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debugz.NotificationHelper;
import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.controller.AccountController;
import com.example.debugz.models.Account;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Shared login entry point for all roles.
 * Supports hardcoded master admin login and Firestore-based login for approved users.
 * ROLE: View / Controller.
 */
public class LandingActivity extends AppCompatActivity {

    private TextInputEditText etLoginId, etLoginPassword;
    private AccountController accountController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationHelper.createChannel(this);

        UserSession session = UserSession.getInstance(this);
        if (session.isLoggedIn()) {
            navigateByRole(session.getRole());
            return;
        }

        setContentView(R.layout.activity_landing);

        accountController = new AccountController();

        etLoginId = findViewById(R.id.etLoginId);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        Button btnStudent = findViewById(R.id.btnStudentLogin);
        Button btnOrganizer = findViewById(R.id.btnOrganizerLogin);
        Button btnAdmin = findViewById(R.id.btnAdminLogin);
        TextView tvSignupLink = findViewById(R.id.tvSignupLink);

        btnStudent.setOnClickListener(v -> attemptAccountLogin(UserSession.ROLE_STUDENT));
        btnOrganizer.setOnClickListener(v -> attemptAccountLogin(UserSession.ROLE_ORGANIZER));
        btnAdmin.setOnClickListener(v -> attemptAdminLogin());
        tvSignupLink.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void attemptAccountLogin(String role) {
        String accountId = getInput(etLoginId);
        String password = getInput(etLoginPassword);

        if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "ID and Password required", Toast.LENGTH_SHORT).show();
            return;
        }

        setButtonsEnabled(false);
        accountController.login(accountId, password, role, new AccountController.OnLoginListener() {
            @Override
            public void onSuccess(Account account) {
                setButtonsEnabled(true);
                UserSession.getInstance(LandingActivity.this)
                        .login(account.getAccountId(), account.getName(), account.getRole());
                navigateByRole(account.getRole());
            }

            @Override
            public void onFailure(String message) {
                setButtonsEnabled(true);
                Toast.makeText(LandingActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attemptAdminLogin() {
        String username = getInput(etLoginId);
        String password = getInput(etLoginPassword);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Admin ID and Password required", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Check Master Admin (Hardcoded)
        if (UserSession.ADMIN_USERNAME.equals(username) && UserSession.ADMIN_PASSWORD.equals(password)) {
            UserSession.getInstance(this).login("admin", "Master Admin", UserSession.ROLE_ADMIN);
            navigateByRole(UserSession.ROLE_ADMIN);
            return;
        }

        // 2. Check Firestore for Approved Custom Admins
        setButtonsEnabled(false);
        accountController.login(username, password, UserSession.ROLE_ADMIN, new AccountController.OnLoginListener() {
            @Override
            public void onSuccess(Account account) {
                setButtonsEnabled(true);
                UserSession.getInstance(LandingActivity.this)
                        .login(account.getAccountId(), account.getName(), account.getRole());
                navigateByRole(UserSession.ROLE_ADMIN);
            }

            @Override
            public void onFailure(String message) {
                setButtonsEnabled(true);
                Toast.makeText(LandingActivity.this, "Admin Login Failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
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

    private void setButtonsEnabled(boolean enabled) {
        findViewById(R.id.btnStudentLogin).setEnabled(enabled);
        findViewById(R.id.btnOrganizerLogin).setEnabled(enabled);
        findViewById(R.id.btnAdminLogin).setEnabled(enabled);
    }

    private String getInput(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}