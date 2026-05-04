package com.example.debugz.controller;

import com.example.debugz.models.Account;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates Firestore account operations for signup, login, and admin approval.
 * Manages the lifecycle of user accounts including registration, role-based login, and status updates.
 *
 * ROLE: Controller Pattern.
 *
 * Outstanding issues:
 * 1. Passwords are currently stored in plain text (as noted in the Account model).
 * 2. No email verification step is implemented.
 * 3. Session timeout logic is not handled here (managed by UserSession).
 */
public class AccountController {

    private final FirebaseFirestore db;

    /**
     * Initializes the controller with a Firestore instance.
     */
    public AccountController() {
        db = FirebaseFirestore.getInstance();
    }

    /** Callback interface for single account operations. */
    public interface OnAccountOperationListener {
        /** Called when the operation completes successfully. */
        void onSuccess();
        /** @param message The error message to display. */
        void onFailure(String message);
    }

    /** Callback interface for login operations. */
    public interface OnLoginListener {
        /** @param account The authenticated account object. */
        void onSuccess(Account account);
        /** @param message The reason for login failure. */
        void onFailure(String message);
    }

    /** Callback interface for account listing operations. */
    public interface OnAccountsFetchedListener {
        /** @param accounts The list of accounts retrieved. */
        void onSuccess(List<Account> accounts);
        /** @param message The error message. */
        void onFailure(String message);
    }

    /**
     * Submits a new account for admin approval.
     * Duplicate account IDs are rejected immediately.
     * @param account The account details to register.
     * @param listener The callback for success or failure.
     */
    public void submitSignup(Account account, OnAccountOperationListener listener) {
        if (account == null || account.getAccountId() == null || account.getAccountId().isEmpty()) {
            listener.onFailure("Account ID is required");
            return;
        }

        db.collection("accounts").document(account.getAccountId()).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        listener.onFailure("That ID is already registered or pending approval.");
                        return;
                    }
                    db.collection("accounts")
                            .document(account.getAccountId())
                            .set(account)
                            .addOnSuccessListener(aVoid -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Attempts login for a student/organizer account.
     * The account must exist, match the role, password, and be approved.
     * @param accountId The unique ID of the account.
     * @param password The password provided by the user.
     * @param expectedRole The role required (e.g., Student, Organizer).
     * @param listener The callback for login results.
     */
    public void login(String accountId, String password, String expectedRole, OnLoginListener listener) {
        db.collection("accounts").document(accountId).get()
                .addOnSuccessListener(snapshot -> handleLoginSnapshot(snapshot, password, expectedRole, listener))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    private void handleLoginSnapshot(DocumentSnapshot snapshot,
                                     String password,
                                     String expectedRole,
                                     OnLoginListener listener) {
        if (!snapshot.exists()) {
            listener.onFailure("No account found. Please sign up first.");
            return;
        }

        Account account = snapshot.toObject(Account.class);
        if (account == null) {
            listener.onFailure("Unable to read account data.");
            return;
        }
        account.setAccountId(snapshot.getId());

        if (!expectedRole.equals(account.getRole())) {
            listener.onFailure("This account is not registered as a " + expectedRole.toLowerCase() + ".");
            return;
        }
        if (account.getPassword() == null || !account.getPassword().equals(password)) {
            listener.onFailure("Incorrect password.");
            return;
        }
        if (account.isPending()) {
            listener.onFailure("Your signup is still pending admin approval.");
            return;
        }
        if (account.isRejected()) {
            listener.onFailure("Your signup was rejected. Please contact the admin.");
            return;
        }
        if (!account.isApproved()) {
            listener.onFailure("Your account is not active.");
            return;
        }

        listener.onSuccess(account);
    }

    /**
     * Fetches all accounts currently awaiting admin approval.
     * @param listener The callback for results.
     */
    public void fetchPendingAccounts(OnAccountsFetchedListener listener) {
        db.collection("accounts")
                .whereEqualTo("status", Account.STATUS_PENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Account> accounts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Account account = doc.toObject(Account.class);
                        account.setAccountId(doc.getId());
                        accounts.add(account);
                    }
                    listener.onSuccess(accounts);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Approves or rejects an account by updating its status.
     * @param accountId The ID of the account to update.
     * @param newStatus The new status (APPROVED or REJECTED).
     * @param listener The callback for operation status.
     */
    public void updateStatus(String accountId, String newStatus, OnAccountOperationListener listener) {
        db.collection("accounts")
                .document(accountId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
}
