package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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
 * Lets students find others and SEND mutual friend requests.
 * ROLE: View.
 */
public class AddFriendActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView rvStudents;
    private EditText etSearchStudent;
    private TextView tvEmpty;
    private StudentAdapter adapter;

    private final List<Account> allAccounts = new ArrayList<>();
    private final List<Account> filteredAccounts = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        db = FirebaseFirestore.getInstance();
        currentUserId = UserSession.getInstance(this).getUserId();

        etSearchStudent = findViewById(R.id.etSearchStudent);
        tvEmpty = findViewById(R.id.tvAddFriendEmpty);
        rvStudents = findViewById(R.id.rvStudents);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(filteredAccounts, currentUserId, (targetAccount, action) -> {
            if ("SEND_REQUEST".equals(action)) {
                sendFriendRequest(targetAccount);
            }
        });
        rvStudents.setAdapter(adapter);

        etSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnViewRequests).setOnClickListener(v -> 
            startActivity(new Intent(this, FriendRequestsActivity.class)));

        loadFriendsThenAccounts();
    }

    private void sendFriendRequest(Account target) {
        db.collection("accounts").document(target.getAccountId())
                .update("friendRequests", FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFriendsThenAccounts() {
        db.collection("accounts").document(currentUserId).get().addOnSuccessListener(snapshot -> {
            Account me = snapshot.toObject(Account.class);
            List<String> friends = (me != null) ? me.getFriendIds() : new ArrayList<>();
            
            db.collection("accounts")
                    .whereEqualTo("role", UserSession.ROLE_STUDENT)
                    .whereEqualTo("status", Account.STATUS_APPROVED)
                    .get()
                    .addOnSuccessListener(querySnap -> {
                        allAccounts.clear();
                        for (QueryDocumentSnapshot doc : querySnap) {
                            Account account = doc.toObject(Account.class);
                            account.setAccountId(doc.getId());
                            if (!doc.getId().equals(currentUserId)) {
                                allAccounts.add(account);
                            }
                        }
                        adapter.setCurrentFriendIds(friends);
                        filter(etSearchStudent.getText().toString());
                    });
        });
    }

    private void filter(String query) {
        filteredAccounts.clear();
        String lower = query.toLowerCase();
        for (Account a : allAccounts) {
            if (a.getName().toLowerCase().contains(lower) || a.getAccountId().toLowerCase().contains(lower)) {
                filteredAccounts.add(a);
            }
        }
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredAccounts.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
