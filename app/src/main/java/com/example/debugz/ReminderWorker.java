package com.example.debugz;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * WorkManager background worker that fires an event reminder notification (US11).
 * Scheduled by {@link com.example.debugz.view.EventDetailActivity} after a successful RSVP
 * with a delay calculated as (eventDate − 24 h − now).
 */
public class ReminderWorker extends Worker {

    /** Input data key carrying the event title string. */
    public static final String KEY_EVENT_TITLE = "event_title";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String title = getInputData().getString(KEY_EVENT_TITLE);
        if (title == null || title.isEmpty()) title = "An upcoming event";
        NotificationHelper.postReminder(getApplicationContext(), title);
        return Result.success();
    }
}

