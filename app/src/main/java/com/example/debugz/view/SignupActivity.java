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
 * Manual signup screen for students and organizers.
 * Signups are stored in Firestore with PENDING status and require admin approval
 * before the account can log in from the landing page.
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etId, etPassword, etConfirmPassword;
    private RadioButton rbStudent, rbOrganizer;
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
        btnSubmit = findViewById(R.id.btnSubmitSignup);
        TextView tvBack = findViewById(R.id.tvBackToLogin);

        btnSubmit.setOnClickListener(v -> submitSignup());
        tvBack.setOnClickListener(v -> finish());
    }

    private void submitSignup() {
        String name = getInput(etName);
        String email = getInput(etEmail);
        String accountId = getInput(etId);
        String password = getInput(etPassword);
        String confirmPassword = getInput(etConfirmPassword);
        String role = rbOrganizer.isChecked() ? UserSession.ROLE_ORGANIZER : UserSession.ROLE_STUDENT;

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
        if (password.length() < 4) {
            etPassword.setError("Use at least 4 characters");
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
                        "Signup submitted! Wait for admin approval before logging in.",
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

    private String getInput(TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text != null ? text.toString().trim() : "";
    }
}

