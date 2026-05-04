package com.example.debugz.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.models.Event;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

/**
 * Bridges Event data with the polished UI cards (Task 7).
 * ROLE: Adapter Pattern.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private final OnEventClickListener listener;

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
        holder.tvDate.setText(event.getDate() + " • " + event.getTime());
        holder.tvPrice.setText(event.getPrice() != null ? event.getPrice() : "Free");
        holder.tvCategory.setText(event.getCategory() != null ? event.getCategory().toUpperCase() : "OTHER");
        
        int current = (event.getAttendeeIds() != null) ? event.getAttendeeIds().size() : 0;
        int max = event.getMaxCapacity();
        
        if (max > 0) {
            int left = max - current;
            holder.tvSpotsLeft.setText(left + " spots left");
            
            int percent = (int) (((float) current / max) * 100);
            holder.tvPercent.setText(percent + "% full");
            holder.pbCapacity.setProgress(percent);

            // Dynamic colors for capacity urgency (Task 7)
            int color;
            if (left > (max * 0.2)) {
                color = Color.parseColor("#27AE60"); // Success Green
            } else if (left > (max * 0.1)) {
                color = Color.parseColor("#F39C12"); // Warning Amber
            } else {
                color = Color.parseColor("#E74C3C"); // Danger Red
            }
            holder.tvSpotsLeft.setTextColor(color);
            holder.pbCapacity.setIndicatorColor(color);
        } else {
            holder.tvSpotsLeft.setText("Unlimited spots");
            holder.tvPercent.setText("0% full");
            holder.pbCapacity.setProgress(0);
        }

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
        holder.btnRSVP.setOnClickListener(v -> listener.onEventClick(event));
        holder.btnCalendar.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvPrice, tvCategory, tvSpotsLeft, tvPercent;
        LinearProgressIndicator pbCapacity;
        Button btnRSVP, btnCalendar; // Corrected to Button to prevent ClassCastException

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvPrice = itemView.findViewById(R.id.tvPriceBadge);
            tvCategory = itemView.findViewById(R.id.tvCategoryBadge);
            tvSpotsLeft = itemView.findViewById(R.id.tvSpotsLeft);
            tvPercent = itemView.findViewById(R.id.tvCapacityPercent);
            pbCapacity = itemView.findViewById(R.id.pbCapacity);
            btnRSVP = itemView.findViewById(R.id.btnRSVP);
            btnCalendar = itemView.findViewById(R.id.btnCalendar);
        }
    }
}
