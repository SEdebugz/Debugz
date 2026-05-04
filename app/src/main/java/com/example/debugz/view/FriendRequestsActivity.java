package com.example.debugz.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.models.Account;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen where users can view and respond to incoming friend requests.
 * ROLE: View.
 */
public class FriendRequestsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String currentUserId;
    private RecyclerView rvRequests;
    private TextView tvEmpty;
    private RequestsAdapter adapter;
    private final List<Account> requestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        db = FirebaseFirestore.getInstance();
        currentUserId = UserSession.getInstance(this).getUserId();

        Toolbar toolbar = findViewById(R.id.toolbarRequests);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvEmpty = findViewById(R.id.tvRequestsEmpty);
        rvRequests = findViewById(R.id.rvFriendRequests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestsAdapter();
        rvRequests.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        // Get the current user's document to see their friendRequests array
        db.collection("accounts").document(currentUserId).get().addOnSuccessListener(snapshot -> {
            Account me = snapshot.toObject(Account.class);
            if (me != null && !me.getFriendRequests().isEmpty()) {
                // Fetch the Account details for every ID in friendRequests
                db.collection("accounts")
                        .whereIn("accountId", me.getFriendRequests())
                        .get()
                        .addOnSuccessListener(querySnap -> {
                            requestList.clear();
                            for (com.google.firebase.firestore.DocumentSnapshot doc : querySnap) {
                                requestList.add(doc.toObject(Account.class));
                            }
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
                        });
            } else {
                requestList.clear();
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleAccept(Account other, int position) {
        WriteBatch batch = db.batch();
        
        // 1. Add other to my friends
        batch.update(db.collection("accounts").document(currentUserId), 
                "friendIds", FieldValue.arrayUnion(other.getAccountId()));
        
        // 2. Remove from my requests
        batch.update(db.collection("accounts").document(currentUserId), 
                "friendRequests", FieldValue.arrayRemove(other.getAccountId()));
        
        // 3. Add me to other's friends (Mutual)
        batch.update(db.collection("accounts").document(other.getAccountId()), 
                "friendIds", FieldValue.arrayUnion(currentUserId));

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show();
            requestList.remove(position);
            adapter.notifyItemRemoved(position);
            tvEmpty.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void handleDecline(Account other, int position) {
        db.collection("accounts").document(currentUserId)
                .update("friendRequests", FieldValue.arrayRemove(other.getAccountId()))
                .addOnSuccessListener(aVoid -> {
                    requestList.remove(position);
                    adapter.notifyItemRemoved(position);
                    tvEmpty.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_account, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Account acc = requestList.get(position);
            holder.tvName.setText(acc.getName());
            holder.tvMeta.setText(acc.getAccountId());
            holder.btnAccept.setOnClickListener(v -> handleAccept(acc, holder.getAdapterPosition()));
            holder.btnDecline.setOnClickListener(v -> handleDecline(acc, holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() { return requestList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMeta;
            Button btnAccept, btnDecline;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvPendingAccountName);
                tvMeta = v.findViewById(R.id.tvPendingAccountMeta);
                btnAccept = v.findViewById(R.id.btnApproveAccount);
                btnDecline = v.findViewById(R.id.btnRejectAccount);
                btnAccept.setText("Accept");
                btnDecline.setText("Decline");
            }
        }
    }
}
