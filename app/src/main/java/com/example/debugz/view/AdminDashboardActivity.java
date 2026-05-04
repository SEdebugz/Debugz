package com.example.debugz.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.controller.AccountController;
import com.example.debugz.controller.EventController;
import com.example.debugz.models.Account;
import com.example.debugz.models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin-only screen that manages pending signups and moderates event listings.
 * Updated to support Event Approval (organizers' events must be approved by admin).
 *
 * ROLE: View (Activity).
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvPendingAccounts, rvAdminEvents;
    private PendingAccountAdapter pendingAdapter;
    private AdminEventAdapter eventAdapter;

    private final List<Account> pendingAccounts = new ArrayList<>();
    private final List<Event> eventList = new ArrayList<>();

    private AccountController accountController;
    private EventController eventController;
    
    private TextView tvPendingEmpty, tvEventEmpty, tvTotalEventsCount, tvPendingApprovalsCount;
    private TextView tvPendingHeader, tvEventsHeader;
    private Button btnSeedEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        accountController = new AccountController();
        eventController = new EventController();

        tvPendingEmpty = findViewById(R.id.tvPendingEmpty);
        tvEventEmpty = findViewById(R.id.tvAdminEmpty);
        tvTotalEventsCount = findViewById(R.id.tvTotalEventsCount);
        tvPendingApprovalsCount = findViewById(R.id.tvPendingApprovalsCount);
        tvPendingHeader = findViewById(R.id.tvPendingHeader);
        tvEventsHeader = findViewById(R.id.tvEventsHeader);
        btnSeedEvents = findViewById(R.id.btnSeedEvents);

        rvPendingAccounts = findViewById(R.id.rvPendingAccounts);
        rvPendingAccounts.setLayoutManager(new LinearLayoutManager(this));
        pendingAdapter = new PendingAccountAdapter(pendingAccounts);
        rvPendingAccounts.setAdapter(pendingAdapter);

        rvAdminEvents = findViewById(R.id.rvAdminEvents);
        rvAdminEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new AdminEventAdapter(eventList);
        rvAdminEvents.setAdapter(eventAdapter);

        btnSeedEvents.setOnClickListener(v -> {
            eventController.fetchAllEvents(new EventController.OnEventsFetchedListener() {
                @Override
                public void onSuccess(List<Event> events) {
                    Toast.makeText(AdminDashboardActivity.this, "Database already has events.", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Exception e) { seed(); }
                @Override
                public void onDatabaseEmpty() { seed(); }

                private void seed() {
                    eventController.seedDemoData(() -> {
                        Toast.makeText(AdminDashboardActivity.this, "Demo events seeded!", Toast.LENGTH_SHORT).show();
                        loadAllEvents();
                    });
                }
            });
        });

        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            UserSession.getInstance(this).logout();
            Intent intent = new Intent(this, LandingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingAccounts();
        loadAllEvents();
    }

    private void loadPendingAccounts() {
        accountController.fetchPendingAccounts(new AccountController.OnAccountsFetchedListener() {
            @Override
            public void onSuccess(List<Account> accounts) {
                pendingAccounts.clear();
                pendingAccounts.addAll(accounts);
                pendingAdapter.notifyDataSetChanged();
                updatePendingUI();
            }
            @Override
            public void onFailure(String message) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load accounts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllEvents() {
        eventController.fetchAllEvents(new EventController.OnEventsFetchedListener() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                eventAdapter.notifyDataSetChanged();
                updateEventsUI();
            }
            @Override
            public void onFailure(Exception e) { updateEventsUI(); }
            @Override
            public void onDatabaseEmpty() {
                eventList.clear();
                eventAdapter.notifyDataSetChanged();
                updateEventsUI();
            }
        });
    }

    private void updatePendingUI() {
        int count = pendingAccounts.size();
        tvPendingApprovalsCount.setText(String.valueOf(count));
        tvPendingHeader.setText("PENDING APPROVALS (" + count + ")");
        tvPendingEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    private void updateEventsUI() {
        int count = eventList.size();
        tvTotalEventsCount.setText(String.valueOf(count));
        tvEventsHeader.setText("ALL EVENTS (" + count + ")");
        tvEventEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    private void confirmDeleteEvent(int position) {
        Event event = eventList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Remove \"" + event.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    eventController.deleteEvent(event.getEventId(), new EventController.OnEventOperationListener() {
                        @Override
                        public void onSuccess() {
                            eventList.remove(position);
                            eventAdapter.notifyItemRemoved(position);
                            updateEventsUI();
                            Toast.makeText(AdminDashboardActivity.this, "Event deleted.", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdminDashboardActivity.this, "Delete failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEventStatus(Event event, int position, String status) {
        event.setStatus(status);
        eventController.updateEvent(event, new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                eventAdapter.notifyItemChanged(position);
                Toast.makeText(AdminDashboardActivity.this, "Event " + status.toLowerCase(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminDashboardActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class PendingAccountAdapter extends RecyclerView.Adapter<PendingAccountAdapter.ViewHolder> {
        private final List<Account> accounts;
        PendingAccountAdapter(List<Account> accounts) { this.accounts = accounts; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_account, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Account account = accounts.get(position);
            holder.tvName.setText(account.getName());
            holder.tvRole.setText(account.getRole());
            holder.tvMeta.setText(account.getAccountId() + " • " + (account.getEmail() != null ? account.getEmail() : "no email"));
            
            holder.btnApprove.setOnClickListener(v -> updateStatus(account, holder.getAdapterPosition(), Account.STATUS_APPROVED));
            holder.btnReject.setOnClickListener(v -> updateStatus(account, holder.getAdapterPosition(), Account.STATUS_REJECTED));
        }

        private void updateStatus(Account account, int position, String status) {
            accountController.updateStatus(account.getAccountId(), status, new AccountController.OnAccountOperationListener() {
                @Override
                public void onSuccess() {
                    accounts.remove(position);
                    notifyItemRemoved(position);
                    updatePendingUI();
                }
                @Override
                public void onFailure(String message) {}
            });
        }

        @Override
        public int getItemCount() { return accounts.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMeta, tvRole;
            Button btnApprove, btnReject;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvPendingAccountName);
                tvMeta = v.findViewById(R.id.tvPendingAccountMeta);
                tvRole = v.findViewById(R.id.tvRoleBadge);
                btnApprove = v.findViewById(R.id.btnApproveAccount);
                btnReject = v.findViewById(R.id.btnRejectAccount);
            }
        }
    }

    private class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {
        private final List<Event> events;
        AdminEventAdapter(List<Event> events) { this.events = events; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Event event = events.get(position);
            holder.tvTitle.setText(event.getTitle());
            holder.tvDate.setText(event.getDate());
            holder.tvOrganizer.setText("Organizer: " + (event.getOrganizerId() != null ? event.getOrganizerId() : "Unknown"));
            
            String status = event.getStatus() != null ? event.getStatus() : Event.STATUS_PENDING;
            holder.tvStatus.setText(status);
            
            // Show approve/reject buttons only if pending
            if (Event.STATUS_PENDING.equals(status)) {
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnApprove.setOnClickListener(v -> updateEventStatus(event, holder.getAdapterPosition(), Event.STATUS_APPROVED));
                holder.btnReject.setOnClickListener(v -> updateEventStatus(event, holder.getAdapterPosition(), Event.STATUS_REJECTED));
            } else {
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
            }

            holder.btnDelete.setOnClickListener(v -> confirmDeleteEvent(holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() { return events.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvOrganizer, tvStatus;
            Button btnDelete, btnApprove, btnReject;
            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvAdminEventTitle);
                tvDate = v.findViewById(R.id.tvAdminEventDate);
                tvOrganizer = v.findViewById(R.id.tvAdminEventOrganizer);
                tvStatus = v.findViewById(R.id.tvAdminEventStatus);
                btnDelete = v.findViewById(R.id.btnDeleteEvent);
                btnApprove = v.findViewById(R.id.btnApproveEvent);
                btnReject = v.findViewById(R.id.btnRejectEvent);
            }
        }
    }
}
