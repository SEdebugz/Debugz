package com.example.debugz.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.models.Registration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays the list of students who have RSVP'd to a specific event.
 * Covers US14: organizer can view the list of RSVPed attendees to estimate participation.
 *
 * Reads from the Firestore "registrations" collection filtered by eventId.
 */
public class AttendeesActivity extends AppCompatActivity {

    private RecyclerView rvAttendees;
    private AttendeeAdapter adapter;
    private List<Registration> registrationList = new ArrayList<>();
    private TextView tvAttendeesTitle, tvAttendeesCount, tvNoAttendees;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendees);

        db = FirebaseFirestore.getInstance();

        String eventId = getIntent().getStringExtra("eventId");
        String eventTitle = getIntent().getStringExtra("eventTitle");

        tvAttendeesTitle = findViewById(R.id.tvAttendeesTitle);
        tvAttendeesCount = findViewById(R.id.tvAttendeesCount);
        tvNoAttendees = findViewById(R.id.tvNoAttendees);
        rvAttendees = findViewById(R.id.rvAttendees);

        if (eventTitle != null && !eventTitle.isEmpty()) {
            tvAttendeesTitle.setText("Attendees: " + eventTitle);
        }

        rvAttendees.setLayoutManager(new LinearLayoutManager(this));
        rvAttendees.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new AttendeeAdapter(registrationList);
        rvAttendees.setAdapter(adapter);

        if (eventId != null && !eventId.isEmpty()) {
            loadAttendees(eventId);
        } else {
            tvNoAttendees.setVisibility(View.VISIBLE);
        }
    }

    private void loadAttendees(String eventId) {
        db.collection("registrations")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    registrationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Registration reg = doc.toObject(Registration.class);
                        registrationList.add(reg);
                    }
                    adapter.notifyDataSetChanged();

                    int count = registrationList.size();
                    tvAttendeesCount.setText(count + (count == 1 ? " attendee" : " attendees"));

                    if (count == 0) {
                        tvNoAttendees.setVisibility(View.VISIBLE);
                        rvAttendees.setVisibility(View.GONE);
                    } else {
                        tvNoAttendees.setVisibility(View.GONE);
                        rvAttendees.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load attendees: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ──────────────────────────────────────────────
    // Inner adapter for attendee rows
    // ──────────────────────────────────────────────

    private static class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.ViewHolder> {

        private final List<Registration> registrations;
        private final SimpleDateFormat sdf =
                new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

        AttendeeAdapter(List<Registration> registrations) {
            this.registrations = registrations;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attendee, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Registration reg = registrations.get(position);
            holder.tvStudentId.setText(reg.getStudentId() != null ? reg.getStudentId() : "—");

            String status = reg.getStatus();
            holder.tvStatus.setText(status != null ? status : "—");

            // Colour-code status badge
            if ("Confirmed".equals(status)) {
                holder.tvStatus.setBackgroundColor(0xFFDCFCE7); // green tint
                holder.tvStatus.setTextColor(0xFF15803D);
            } else if ("Waitlisted".equals(status)) {
                holder.tvStatus.setBackgroundColor(0xFFFEF9C3); // yellow tint
                holder.tvStatus.setTextColor(0xFFB45309);
            } else {
                holder.tvStatus.setBackgroundColor(0xFFE0F2FE); // blue tint
                holder.tvStatus.setTextColor(0xFF0369A1);
            }

            if (reg.getTimestamp() > 0) {
                holder.tvTimestamp.setText("Registered: " +
                        sdf.format(new Date(reg.getTimestamp())));
            } else {
                holder.tvTimestamp.setText("");
            }
        }

        @Override
        public int getItemCount() {
            return registrations.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStudentId, tvStatus, tvTimestamp;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStudentId = itemView.findViewById(R.id.tvAttendeeStudentId);
                tvStatus = itemView.findViewById(R.id.tvAttendeeStatus);
                tvTimestamp = itemView.findViewById(R.id.tvAttendeeTimestamp);
            }
        }
    }
}

