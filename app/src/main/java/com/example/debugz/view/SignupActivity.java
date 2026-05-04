package com.example.debugz.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.controller.AccountController;
import com.example.debugz.models.Account;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Handles user registration for Students, Organizers, and new Admins.
 * All signups are PENDING until an existing admin approves them in the dashboard.
 *
 * ROLE: View (Activity).
 * DESIGN PATTERN: Controller Pattern.
 *
 * Outstanding issues:
 * 1. Email format validation is basic; does not strictly enforce @lums.edu.pk domains.
 * 2. Does not yet support profile picture upload during registration.
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etId, etPassword, etConfirmPassword;
    private RadioButton rbStudent, rbOrganizer, rbAdmin;
    private Button btnSubmit;
    private AccountController accountController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        accountController = new AccountController();

        etName = findViewById(R.id.etSignupName);
        etEmail = findViewById(R.id.etSignupEmail);
        etId = findViewById(R.id.etSignupId);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etSignupConfirmPassword);
        rbStudent = findViewById(R.id.rbSignupStudent);
        rbOrganizer = findViewById(R.id.rbSignupOrganizer);
        rbAdmin = findViewById(R.id.rbSignupAdmin);
        btnSubmit = findViewById(R.id.btnSubmitSignup);
        TextView tvBack = findViewById(R.id.tvBackToLogin);

        btnSubmit.setOnClickListener(v -> submitSignup());
        tvBack.setOnClickListener(v -> finish());
    }

    /**
     * Collects form data and submits a new account request to Firestore via the controller.
     */
    private void submitSignup() {
        String name = getInput(etName);
        String email = getInput(etEmail);
        String accountId = getInput(etId);
        String password = getInput(etPassword);
        String confirmPassword = getInput(etConfirmPassword);
        
        String role = UserSession.ROLE_STUDENT;
        if (rbOrganizer.isChecked()) role = UserSession.ROLE_ORGANIZER;
        else if (rbAdmin.isChecked()) role = UserSession.ROLE_ADMIN;

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(accountId)) {
            etId.setError("ID is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        btnSubmit.setEnabled(false);

        Account account = new Account(
                accountId,
                name,
                email,
                password,
                role,
                Account.STATUS_PENDING,
                System.currentTimeMillis()
        );

        accountController.submitSignup(account, new AccountController.OnAccountOperationListener() {
            @Override
            public void onSuccess() {
                btnSubmit.setEnabled(true);
                Toast.makeText(SignupActivity.this,
                        "Signup submitted! Wait for admin approval.",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(String message) {
                btnSubmit.setEnabled(true);
                Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Helper to safely extract trimmed text from input fields.
     */
    private String getInput(TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text != null ? text.toString().trim() : "";
    }
}
