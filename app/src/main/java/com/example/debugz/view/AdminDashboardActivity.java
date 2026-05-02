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
 * Admin-only screen that now serves two responsibilities from one place:
 * 1) approve or reject pending student / organizer signups
 * 2) manage all event listings, including deletion
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvPendingAccounts, rvAdminEvents;
    private PendingAccountAdapter pendingAdapter;
    private AdminEventAdapter eventAdapter;

    private final List<Account> pendingAccounts = new ArrayList<>();
    private final List<Event> eventList = new ArrayList<>();

    private AccountController accountController;
    private EventController eventController;
    private TextView tvPendingEmpty, tvEventEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        accountController = new AccountController();
        eventController = new EventController();

        tvPendingEmpty = findViewById(R.id.tvPendingEmpty);
        tvEventEmpty = findViewById(R.id.tvAdminEmpty);

        rvPendingAccounts = findViewById(R.id.rvPendingAccounts);
        rvPendingAccounts.setLayoutManager(new LinearLayoutManager(this));
        pendingAdapter = new PendingAccountAdapter(pendingAccounts);
        rvPendingAccounts.setAdapter(pendingAdapter);

        rvAdminEvents = findViewById(R.id.rvAdminEvents);
        rvAdminEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new AdminEventAdapter(eventList);
        rvAdminEvents.setAdapter(eventAdapter);

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

    // ── Pending signup approvals ──────────────────────────────────────────

    private void loadPendingAccounts() {
        accountController.fetchPendingAccounts(new AccountController.OnAccountsFetchedListener() {
            @Override
            public void onSuccess(List<Account> accounts) {
                pendingAccounts.clear();
                pendingAccounts.addAll(accounts);
                pendingAdapter.notifyDataSetChanged();
                tvPendingEmpty.setVisibility(pendingAccounts.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Failed to load pending accounts: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void approveAccount(Account account, int position) {
        updateAccountStatus(account, position, Account.STATUS_APPROVED, "Account approved.");
    }

    private void rejectAccount(Account account, int position) {
        updateAccountStatus(account, position, Account.STATUS_REJECTED, "Account rejected.");
    }

    private void updateAccountStatus(Account account, int position, String status, String successMessage) {
        accountController.updateStatus(account.getAccountId(), status, new AccountController.OnAccountOperationListener() {
            @Override
            public void onSuccess() {
                if (position < pendingAccounts.size()) {
                    pendingAccounts.remove(position);
                    pendingAdapter.notifyItemRemoved(position);
                }
                tvPendingEmpty.setVisibility(pendingAccounts.isEmpty() ? View.VISIBLE : View.GONE);
                Toast.makeText(AdminDashboardActivity.this, successMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Status update failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Event moderation ──────────────────────────────────────────────────

    private void loadAllEvents() {
        eventController.fetchAllEvents(new EventController.OnEventsFetchedListener() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                eventAdapter.notifyDataSetChanged();
                tvEventEmpty.setVisibility(eventList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDatabaseEmpty() {
                eventList.clear();
                eventAdapter.notifyDataSetChanged();
                tvEventEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void confirmDeleteEvent(int position) {
        Event event = eventList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Remove \"" + event.getTitle() + "\"?\n\nThis will also remove all registrations for this event.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(Event event, int position) {
        eventController.deleteEvent(event.getEventId(), new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                if (position < eventList.size()) {
                    eventList.remove(position);
                    eventAdapter.notifyItemRemoved(position);
                }
                tvEventEmpty.setVisibility(eventList.isEmpty() ? View.VISIBLE : View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Event deleted.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Adapters ──────────────────────────────────────────────────────────

    private class PendingAccountAdapter extends RecyclerView.Adapter<PendingAccountAdapter.ViewHolder> {

        private final List<Account> accounts;

        PendingAccountAdapter(List<Account> accounts) {
            this.accounts = accounts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pending_account, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Account account = accounts.get(position);
            holder.tvName.setText(account.getName());
            String email = (account.getEmail() == null || account.getEmail().isEmpty())
                    ? "no email" : account.getEmail();
            holder.tvMeta.setText(account.getRole() + " • " + account.getAccountId() + " • " + email);

            holder.btnApprove.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    approveAccount(accounts.get(pos), pos);
                }
            });

            holder.btnReject.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    rejectAccount(accounts.get(pos), pos);
                }
            });
        }

        @Override
        public int getItemCount() {
            return accounts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMeta;
            Button btnApprove, btnReject;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPendingAccountName);
                tvMeta = itemView.findViewById(R.id.tvPendingAccountMeta);
                btnApprove = itemView.findViewById(R.id.btnApproveAccount);
                btnReject = itemView.findViewById(R.id.btnRejectAccount);
            }
        }
    }

    private class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

        private final List<Event> events;

        AdminEventAdapter(List<Event> events) {
            this.events = events;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_event, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Event event = events.get(position);
            holder.tvTitle.setText(event.getTitle());
            holder.tvDate.setText(event.getDate() + " • " + event.getLocation());
            String organizer = event.getOrganizerId() != null ? event.getOrganizerId() : "—";
            holder.tvOrganizer.setText("Organizer: " + organizer);

            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    confirmDeleteEvent(pos);
                }
            });
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvOrganizer;
            Button btnDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvAdminEventTitle);
                tvDate = itemView.findViewById(R.id.tvAdminEventDate);
                tvOrganizer = itemView.findViewById(R.id.tvAdminEventOrganizer);
                btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
            }
        }
    }
}
