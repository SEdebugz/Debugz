package com.example.debugz.controller;

import com.example.debugz.models.Account;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates Firestore account operations for signup, login, and admin approval.
 * Manages the lifecycle of user accounts including registration, role-based login,
 * and status updates.
 * Outstanding issues:
 * 1. Passwords are currently stored in plain text (as noted in the Account model).
 * 2. No email verification step is implemented.
 * 3. Session timeout logic is not handled here (managed by UserSession).
 *
 * ROLE: Controller Pattern.
 */
public class AccountController {

    private final FirebaseFirestore db;

    /**
     * Creates an AccountController and obtains the shared Firestore instance.
     */
    public AccountController() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback interface for single account operations such as signup and status updates.
     */
    public interface OnAccountOperationListener {
        /**
         * Called when the operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when the operation fails.
         *
         * @param message Human-readable reason for the failure.
         */
        void onFailure(String message);
    }

    /**
     * Callback interface for login operations.
     */
    public interface OnLoginListener {
        /**
         * Called when credentials are verified and the account is approved.
         *
         * @param account The authenticated Account object ready for session storage.
         */
        void onSuccess(Account account);

        /**
         * Called when login fails for any reason.
         *
         * @param message Human-readable reason for the failure, e.g. wrong password,
         *                pending approval, or role mismatch.
         */
        void onFailure(String message);
    }

    /**
     * Callback interface for operations that return a list of accounts.
     */
    public interface OnAccountsFetchedListener {
        /**
         * Called when the account list is successfully retrieved.
         *
         * @param accounts The list of Account objects returned by the query.
         */
        void onSuccess(List<Account> accounts);

        /**
         * Called when the fetch operation fails.
         *
         * @param message Human-readable reason for the failure.
         */
        void onFailure(String message);
    }

    /**
     * Submits a new account for admin approval.
     * Checks for a duplicate account ID before writing; rejects immediately if one exists.
     * The account is written with status PENDING and will not be able to log in
     * until an admin calls {@link #updateStatus(String, String, OnAccountOperationListener)}.
     *
     * @param account  The Account object to register; accountId must not be null or empty.
     * @param listener Callback invoked on success or failure.
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
     * Attempts to log in a student or organizer account.
     * The account must exist in Firestore, match the expected role, have a correct
     * password, and have status APPROVED. Any other state results in a failure callback.
     *
     * @param accountId    The unique account ID (roll number or org ID) to look up.
     * @param password     The plain-text password provided by the user.
     * @param expectedRole The role the user is attempting to log in as; one of
     *                     {@link com.example.debugz.UserSession#ROLE_STUDENT} or
     *                     {@link com.example.debugz.UserSession#ROLE_ORGANIZER}.
     * @param listener     Callback invoked with the Account on success, or an error
     *                     message on failure.
     */
    public void login(String accountId, String password, String expectedRole, OnLoginListener listener) {
        db.collection("accounts").document(accountId).get()
                .addOnSuccessListener(snapshot -> handleLoginSnapshot(snapshot, password, expectedRole, listener))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Validates a Firestore document snapshot against the provided credentials and role.
     * Called internally by {@link #login(String, String, String, OnLoginListener)}.
     *
     * @param snapshot     The Firestore document snapshot for the account.
     * @param password     The plain-text password to validate.
     * @param expectedRole The role the login attempt requires.
     * @param listener     Callback invoked on success or failure.
     */
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
     * Used by AdminDashboardActivity to populate the pending approvals list.
     *
     * @param listener Callback invoked with the list of PENDING accounts on success,
     *                 or an error message on failure.
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
     * Updates the approval status of an account.
     * Called by AdminDashboardActivity to approve or reject a pending signup.
     *
     * @param accountId  The ID of the account to update.
     * @param newStatus  The new status to apply; use {@link Account#STATUS_APPROVED}
     *                   or {@link Account#STATUS_REJECTED}.
     * @param listener   Callback invoked on success or failure.
     */
    public void updateStatus(String accountId, String newStatus, OnAccountOperationListener listener) {
        db.collection("accounts")
                .document(accountId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
}