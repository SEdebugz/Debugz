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
 * Shows the organizer's own events loaded from Firestore and allows them to create new events,
 * edit existing ones (US12, US13), and view the attendee list (US14).
 * Uses the logged-in organizer ID from {@link UserSession}.
 *
 * ROLE: View (Activity).
 * DESIGN PATTERN: Controller Pattern, Adapter Pattern.
 */
public class OrganizerDashboardActivity extends AppCompatActivity {

    private String organizerId;

    private RecyclerView rvOrganizerEvents;
    private OrganizerEventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private EventController eventController;
    private TextView tvEmpty, tvOrganizerName, tvEventCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        organizerId = UserSession.getInstance(this).getUserId();
        eventController = new EventController();

        tvEmpty = findViewById(R.id.tvEmptyOrganizer);
        tvOrganizerName = findViewById(R.id.tvOrganizerName);
        tvEventCount = findViewById(R.id.tvOrganizerEventCount);
        rvOrganizerEvents = findViewById(R.id.rvOrganizerEvents);
        
        tvOrganizerName.setText(UserSession.getInstance(this).getUserName());

        rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizerEventAdapter(eventList);
        rvOrganizerEvents.setAdapter(adapter);

        findViewById(R.id.btnCreateEvent).setOnClickListener(v -> {
            startActivity(new Intent(this, EditEventActivity.class));
        });

        findViewById(R.id.btnOrgLogout).setOnClickListener(v -> {
            UserSession.getInstance(this).logout();
            Intent intent = new Intent(this, LandingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrganizerEvents();
    }

    private void loadOrganizerEvents() {
        eventController.fetchEventsByOrganizer(organizerId, new EventController.OnEventsFetchedListener() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.GONE);
                rvOrganizerEvents.setVisibility(View.VISIBLE);
                tvEventCount.setText("Managing " + events.size() + " events");
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerDashboardActivity.this,
                        "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDatabaseEmpty() {
                eventList.clear();
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.VISIBLE);
                rvOrganizerEvents.setVisibility(View.GONE);
                tvEventCount.setText("No events created yet");
            }
        });
    }

    private class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {

        private final List<Event> events;

        OrganizerEventAdapter(List<Event> events) {
            this.events = events;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event_manage, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Event event = events.get(position);
            holder.tvTitle.setText(event.getTitle());
            holder.tvDate.setText(event.getDate() + " • " + event.getLocation());
            int attendees = event.getAttendeeIds() != null ? event.getAttendeeIds().size() : 0;
            holder.tvCapacity.setText("Capacity: " + attendees + " / " + event.getMaxCapacity());

            holder.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerDashboardActivity.this, EditEventActivity.class);
                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("title", event.getTitle());
                intent.putExtra("description", event.getDescription());
                intent.putExtra("location", event.getLocation());
                intent.putExtra("date", event.getDate());
                intent.putExtra("time", event.getTime());
                intent.putExtra("maxCapacity", event.getMaxCapacity());
                intent.putExtra("price", event.getPrice());
                intent.putExtra("category", event.getCategory());
                startActivity(intent);
            });

            holder.btnAttendees.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerDashboardActivity.this, AttendeesActivity.class);
                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("eventTitle", event.getTitle());
                startActivity(intent);
            });

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(OrganizerDashboardActivity.this)
                        .setTitle("Delete " + event.getTitle() + "?")
                        .setMessage("This will remove all RSVPs. Cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            eventController.deleteEvent(event.getEventId(), new EventController.OnEventOperationListener() {
                                @Override
                                public void onSuccess() {
                                    int currentPos = holder.getAdapterPosition();
                                    if (currentPos != RecyclerView.NO_POSITION) {
                                        events.remove(currentPos);
                                        notifyItemRemoved(currentPos);
                                        Toast.makeText(OrganizerDashboardActivity.this, "Event deleted.", Toast.LENGTH_SHORT).show();
                                        if (events.isEmpty()) {
                                            tvEmpty.setVisibility(View.VISIBLE);
                                            rvOrganizerEvents.setVisibility(View.GONE);
                                            tvEventCount.setText("No events created yet");
                                        } else {
                                            tvEventCount.setText("Managing " + events.size() + " events");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(OrganizerDashboardActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvCapacity;
            Button btnEdit, btnAttendees, btnDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvOrgEventTitle);
                tvDate = itemView.findViewById(R.id.tvOrgEventDate);
                tvCapacity = itemView.findViewById(R.id.tvOrgEventCapacity);
                btnEdit = itemView.findViewById(R.id.btnEditEvent);
                btnAttendees = itemView.findViewById(R.id.btnViewAttendees);
                btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
            }
        }
    }
}
