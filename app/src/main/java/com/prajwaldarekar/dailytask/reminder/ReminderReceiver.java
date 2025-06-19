package com.prajwaldarekar.dailytask.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prajwaldarekar.dailytask.utils.NotificationUtils;

import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract task details from intent
        String title = intent.getStringExtra("title");
        String repeatMode = intent.getStringExtra("repeatMode");
        long taskId = intent.getLongExtra("taskId", -1);
        long createdAt = intent.getLongExtra("createdAt", System.currentTimeMillis());

        // 📣 Ensure the channel is registered & show notification
        NotificationUtils.createNotificationChannel(context);
        NotificationUtils.showReminderNotification(
                context,
                title != null ? title : "Reminder",
                "⏰ Don't forget this task!"
        );

        // 🔁 If repeating, reschedule the next instance
        if (taskId != -1 && repeatMode != null && !"NONE".equals(repeatMode)) {
            long nextTriggerTime = calculateNextTriggerTime(System.currentTimeMillis(), repeatMode);
            ReminderUtils.scheduleReminder(context, taskId, title, nextTriggerTime, repeatMode, createdAt);
        }
    }

    /**
     * 🔁 Calculate next reminder time based on repeat mode.
     */
    private long calculateNextTriggerTime(long currentTime, String repeatMode) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);

        switch (repeatMode) {
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