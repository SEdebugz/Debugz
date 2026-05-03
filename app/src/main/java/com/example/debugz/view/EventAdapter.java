package com.example.debugz.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.models.Event;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

/**
 * Binds event model data into the cards shown by the discovery and saved-events lists.
 * The adapter follows the standard RecyclerView adapter/view-holder pattern and also
 * renders the capacity indicators used by the event list design.
 * Outstanding issues: capacity text assumes attendee data is already loaded on each event,
 * and click handling is intentionally delegated to callers instead of enforced here.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventClickListener listener;

    /**
     * Listener for item-click events emitted from an event card.
     */
    public interface OnEventClickListener {
        /**
         * Handles a click on a specific event.
         *
         * @param event the event associated with the clicked card.
         */
        void onEventClick(Event event);
    }

    /**
     * Creates an adapter backed by the provided event list and click listener.
     *
     * @param eventList the events to render.
     * @param listener the callback for item selections.
     */
    public EventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(event.getDate());

        // Capacity Logic for US5
        int currentAttendees = (event.getAttendeeIds() != null) ? event.getAttendeeIds().size() : 0;
        int max = event.getMaxCapacity();
        holder.tvCapacityInfo.setText(currentAttendees + " / " + max);
        holder.tvLeftInfo.setText((max - currentAttendees) + " left");

        if (max > 0) {
            int progress = (int) (((float) currentAttendees / max) * 100);
            holder.pbCapacity.setProgress(progress);
            holder.tvCapacityBadge.setText(progress + "% Full");
        }

        // US6: upvote count
        holder.tvUpvotes.setText("♥ " + event.getUpvoteCount());

        // Both the card and "View Details" button navigate to event detail
        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
        holder.btnItemRsvp.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvCapacityInfo, tvLeftInfo, tvCapacityBadge, tvUpvotes;
        LinearProgressIndicator pbCapacity;
        android.widget.Button btnItemRsvp;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle        = itemView.findViewById(R.id.tvTitle);
            tvDate         = itemView.findViewById(R.id.tvDate);
            tvCapacityInfo = itemView.findViewById(R.id.tvCapacityInfo);
            tvLeftInfo     = itemView.findViewById(R.id.tvLeftInfo);
            tvCapacityBadge = itemView.findViewById(R.id.tvCapacityBadge);
            pbCapacity     = itemView.findViewById(R.id.pbCapacity);
            tvUpvotes      = itemView.findViewById(R.id.tvItemUpvotes);
            btnItemRsvp    = itemView.findViewById(R.id.btnItemRSVP);
        }
    }
}
