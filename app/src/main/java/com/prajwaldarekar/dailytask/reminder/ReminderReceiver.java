package com.prajwaldarekar.dailytask.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.prajwaldarekar.dailytask.utils.NotificationUtils;

import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            // ✅ Extract task details from intent
            String title = intent.getStringExtra("title");
            String repeatMode = intent.getStringExtra("repeatMode");
            long taskId = intent.getLongExtra("taskId", -1);
            long createdAt = intent.getLongExtra("createdAt", System.currentTimeMillis());

            // ✅ Validate title
            String taskTitle = title != null ? title : "Reminder";

            // ⏰ Check if this reminder is overdue (missed trigger)
            long currentTime = System.currentTimeMillis();
            if (currentTime - createdAt > 10 * 60 * 1000) { // Overdue by >10 mins
                Toast.makeText(context, "⏰ Missed reminder: " + taskTitle, Toast.LENGTH_LONG).show();
            }

            // 🔔 Show the notification
            NotificationUtils.createNotificationChannel(context);
            NotificationUtils.showReminderNotification(
                    context,
                    taskTitle,
                    "⏰ Don't forget this task!"
            );

            // 🔁 Reschedule if it's a repeating task
            if (taskId != -1 && repeatMode != null && !"NONE".equalsIgnoreCase(repeatMode)) {
                long nextTriggerTime = calculateNextTriggerTime(currentTime, repeatMode);

                // 📦 Fallback: Inexact if next time is invalid
                if (nextTriggerTime <= currentTime) {
                    nextTriggerTime = currentTime + 12 * 60 * 60 * 1000; // +12 hrs fallback
                    FirebaseCrashlytics.getInstance().log("⏰ Fallback inexact alarm set for task ID: " + taskId);
                }

                ReminderUtils.scheduleReminder(context, taskId, taskTitle, nextTriggerTime, repeatMode, currentTime);
            }

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(context, "⚠️ Reminder failed!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 🔁 Calculate the next reminder time based on the repeat mode.
     */
    private long calculateNextTriggerTime(long currentTime, String repeatMode) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);

        switch (repeatMode.toUpperCase()) {
            case "DAILY":
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case "WEEKLY":
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "MONTHLY":
                calendar.add(Calendar.MONTH, 1);
                break;
            default:
                break;
        }

        return calendar.getTimeInMillis();
    }
}
