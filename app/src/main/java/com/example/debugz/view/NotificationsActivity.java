package com.example.debugz.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.UserSession;
import com.example.debugz.controller.NotificationController;
import com.example.debugz.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display recent notifications for the logged-in student.
 * ROLE: View.
 */
public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList = new ArrayList<>();
    private NotificationController notificationController;
    private TextView tvEmpty;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        userId = UserSession.getInstance(this).getUserId();
        notificationController = new NotificationController();

        Toolbar toolbar = findViewById(R.id.toolbarNotifications);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvEmpty = findViewById(R.id.tvNotificationsEmpty);
        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        findViewById(R.id.btnMarkRead).setOnClickListener(v -> markAllAsRead());

        loadNotifications();
    }

    private void loadNotifications() {
        notificationController.fetchNotificationsForUser(userId, new NotificationController.OnNotificationsFetchedListener() {
            @Override
            public void onSuccess(List<NotificationModel> notifications) {
                notificationList.clear();
                notificationList.addAll(notifications);
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(NotificationsActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAllAsRead() {
        notificationController.markAllAsRead(userId, new NotificationController.OnOperationListener() {
            @Override
            public void onSuccess() {
                for (NotificationModel n : notificationList) {
                    n.setRead(true);
                }
                adapter.notifyDataSetChanged();
                Toast.makeText(NotificationsActivity.this, "All marked as read", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(NotificationsActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
