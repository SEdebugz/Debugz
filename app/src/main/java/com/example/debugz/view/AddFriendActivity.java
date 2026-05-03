package com.example.debugz.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.models.Account;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets the logged-in student search for other approved student accounts
 * and add them as friends.  Friend IDs are stored in the "accounts" collection
 * under the friendIds array field.
 */
public class AddFriendActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView      rvStudents;
    private EditText          etSearchStudent;
    private TextView          tvEmpty;
    private StudentAdapter    adapter;

    private final List<Account> allAccounts      = new ArrayList<>();
    private final List<Account> filteredAccounts = new ArrayList<>();

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        db            = FirebaseFirestore.getInstance();
        currentUserId = UserSession.getInstance(this).getUserId();

        etSearchStudent = findViewById(R.id.etSearchStudent);
        tvEmpty         = findViewById(R.id.tvAddFriendEmpty);
        rvStudents      = findViewById(R.id.rvStudents);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StudentAdapter(filteredAccounts, targetAccount -> {
            // Write the new friend ID into the current user's friendIds array
            db.collection("accounts").document(currentUserId)
                    .update("friendIds", FieldValue.arrayUnion(targetAccount.getAccountId()))
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this,
                                    "Added " + targetAccount.getName() + "!",
                                    Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Failed to add friend: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });

        rvStudents.setAdapter(adapter);

        etSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });

        loadFriendsThenAccounts();
    }

    /**
     * First fetches the current user's existing friendIds so the adapter can
     * mark already-added people, then loads all other approved student accounts.
     */
    private void loadFriendsThenAccounts() {
        db.collection("accounts").document(currentUserId).get()
                .addOnSuccessListener(myDoc -> {
                    List<String> myFriendIds = new ArrayList<>();
                    if (myDoc.exists()) {
                        Account me = myDoc.toObject(Account.class);
                        if (me != null) myFriendIds = me.getFriendIds();
                    }

                    final List<String> friendIds = myFriendIds;

                    // Now load everyone else who is an approved student
                    db.collection("accounts")
                            .whereEqualTo("role",   UserSession.ROLE_STUDENT)
                            .whereEqualTo("status", Account.STATUS_APPROVED)
                            .get()
                            .addOnSuccessListener(querySnap -> {
                                allAccounts.clear();
                                for (QueryDocumentSnapshot doc : querySnap) {
                                    Account account = doc.toObject(Account.class);
                                    account.setAccountId(doc.getId());
                                    // Exclude yourself from the list
                                    if (!account.getAccountId().equals(currentUserId)) {
                                        allAccounts.add(account);
                                    }
                                }
                                adapter.setCurrentFriendIds(friendIds);
                                filter(etSearchStudent.getText().toString());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Failed to load students: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load your profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void filter(String query) {
        filteredAccounts.clear();
        if (query.isEmpty()) {
            filteredAccounts.addAll(allAccounts);
        } else {
            String lower = query.toLowerCase();
            for (Account a : allAccounts) {
                if (a.getName().toLowerCase().contains(lower)
                        || a.getAccountId().toLowerCase().contains(lower)) {
                    filteredAccounts.add(a);
                }
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredAccounts.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvStudents.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvStudents.setVisibility(View.VISIBLE);
        }
    }
}