package com.example.debugz;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Utility class for creating the notification channel and posting event-related
 * local notifications (US7 – RSVP confirmation, US11 – event reminder).
 */
public class NotificationHelper {

    public static final String CHANNEL_ID   = "debugz_events";
    public static final String CHANNEL_NAME = "Event Notifications";

    /**
     * Creates the notification channel (Android 8.0+).
     * Safe to call multiple times — creation is idempotent.
     */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Campus event RSVP confirmations and reminders");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    /**
     * Posts an immediate local notification confirming an RSVP (US7).
     * Notification is silently skipped if POST_NOTIFICATIONS permission is not granted
     * (Android 13+).
     *
     * @param context    application context
     * @param eventTitle title of the event the student registered for
     */
    public static void postRsvpConfirmation(Context context, String eventTitle) {
        if (!hasNotificationPermission(context)) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("RSVP Confirmed! 🎉")
                .setContentText("You're registered for: " + eventTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context)
                .notify(("rsvp_" + eventTitle).hashCode(), builder.build());
    }

    /**
     * Posts a reminder notification for an event (US11).
     * Called by {@link ReminderWorker} when the scheduled delay has elapsed.
     *
     * @param context    application context
     * @param eventTitle title of the event
     */
    public static void postReminder(Context context, String eventTitle) {
        if (!hasNotificationPermission(context)) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Event Tomorrow 🔔")
                .setContentText(eventTitle + " is coming up!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context)
                .notify(("reminder_" + eventTitle).hashCode(), builder.build());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 33) { // Android 13 / TIRAMISU
            return ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // permission not required on older APIs
    }
}

