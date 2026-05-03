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
 * ROLE: Adapter Pattern.
 * PURPOSE: Bridges the Event data models with the RecyclerView UI components.
 * Supports real-time capacity updates (US5) and displays event prices/categories.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

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
        
        // Price and Category UI
        holder.tvItemPrice.setText(event.getPrice() != null ? event.getPrice() : "Free");
        holder.tvItemCategory.setText(event.getCategory() != null ? event.getCategory() : "Other");
        
        // Capacity Logic (US5)
        int currentAttendees = (event.getAttendeeIds() != null) ? event.getAttendeeIds().size() : 0;
        int max = event.getMaxCapacity();
        holder.tvCapacityInfo.setText(currentAttendees + " / " + max);
        holder.tvLeftInfo.setText((max - currentAttendees) + " left");
        
        if (max > 0) {
            int progress = (int) (((float) currentAttendees / max) * 100);
            holder.pbCapacity.setProgress(progress);
            holder.tvCapacityBadge.setText(progress + "% Full");
        }

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvCapacityInfo, tvLeftInfo, tvCapacityBadge, tvItemPrice, tvItemCategory;
        LinearProgressIndicator pbCapacity;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCapacityInfo = itemView.findViewById(R.id.tvCapacityInfo);
            tvLeftInfo = itemView.findViewById(R.id.tvLeftInfo);
            tvCapacityBadge = itemView.findViewById(R.id.tvCapacityBadge);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            tvItemCategory = itemView.findViewById(R.id.tvItemCategory);
            pbCapacity = itemView.findViewById(R.id.pbCapacity);
        }
    }
}
