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
import com.example.debugz.controller.EventController;
import com.example.debugz.models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin-only screen that lists all events from Firestore and allows deletion.
 * Covers US15: administrator manages event listings so inappropriate or duplicate events
 * can be removed.
 *
 * Deletion removes the event document AND all associated registration documents from Firestore,
 * so the change is immediately reflected for all users (students' My Events, discovery feed).
 * Demo mode uses hardcoded admin ID "demo_admin_001".
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private static final String DEMO_ADMIN_ID = "demo_admin_001";

    private RecyclerView rvAdminEvents;
    private AdminEventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private EventController eventController;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        eventController = new EventController();

        tvEmpty = findViewById(R.id.tvAdminEmpty);
        rvAdminEvents = findViewById(R.id.rvAdminEvents);
        rvAdminEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminEventAdapter(eventList);
        rvAdminEvents.setAdapter(adapter);

        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            UserSession.getInstance(this).logout();
            Intent intent = new Intent(this, LandingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadAllEvents();
    }

    private void loadAllEvents() {
        eventController.fetchAllEvents(new EventController.OnEventsFetchedListener() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.GONE);
                rvAdminEvents.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDatabaseEmpty() {
                eventList.clear();
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.VISIBLE);
                rvAdminEvents.setVisibility(View.GONE);
            }
        });
    }

    /** Called by the adapter when a delete is requested; shows a confirmation dialog first. */
    private void confirmDeleteEvent(int position) {
        Event event = eventList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Remove \"" + event.getTitle() + "\"?\n\n" +
                        "This will also remove all registrations for this event.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(Event event, int position) {
        eventController.deleteEvent(event.getEventId(), new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                // Remove from local list and notify adapter
                if (position < eventList.size()) {
                    eventList.remove(position);
                    adapter.notifyItemRemoved(position);
                }
                if (eventList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvAdminEvents.setVisibility(View.GONE);
                }
                Toast.makeText(AdminDashboardActivity.this,
                        "Event deleted.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ──────────────────────────────────────────────
    // Inner RecyclerView adapter for admin event list
    // ──────────────────────────────────────────────

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
                if (pos != RecyclerView.NO_ID) {
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

