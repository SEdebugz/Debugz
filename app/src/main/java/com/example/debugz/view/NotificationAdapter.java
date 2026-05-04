package com.example.debugz.view;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.models.NotificationModel;

import java.util.List;

/**
 * Adapter for displaying notification items in a list.
 * ROLE: Adapter Pattern.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<NotificationModel> notifications;

    public NotificationAdapter(List<NotificationModel> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel n = notifications.get(position);
        holder.tvTitle.setText(n.getTitle());
        holder.tvMessage.setText(n.getMessage());
        
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                n.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS);
        holder.tvTime.setText(timeAgo);

        // Icon based on type
        int iconRes = R.drawable.ic_notifications; // Default
        if ("RSVP_CONFIRMED".equals(n.getType())) iconRes = R.drawable.ic_check_circle;
        else if ("EVENT_REMINDER".equals(n.getType())) iconRes = R.drawable.ic_calendar;
        else if ("FRIEND_RSVP".equals(n.getType())) iconRes = R.drawable.ic_person;
        else if ("EVENT_UPDATED".equals(n.getType())) iconRes = R.drawable.ic_edit;
        else if ("CAPACITY_LOW".equals(n.getType())) iconRes = R.drawable.ic_warning;
        
        holder.ivIcon.setImageResource(iconRes);

        // Unread styling
        if (!n.isRead()) {
            holder.container.setBackgroundColor(Color.parseColor("#F0F7FF"));
            holder.unreadIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.container.setBackgroundColor(Color.WHITE);
            holder.unreadIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView ivIcon;
        View container, unreadIndicator;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvNotificationTitle);
            tvMessage = v.findViewById(R.id.tvNotificationMessage);
            tvTime = v.findViewById(R.id.tvNotificationTime);
            ivIcon = v.findViewById(R.id.ivNotificationIcon);
            container = v.findViewById(R.id.notificationContainer);
            unreadIndicator = v.findViewById(R.id.unreadIndicator);
        }
    }
}
