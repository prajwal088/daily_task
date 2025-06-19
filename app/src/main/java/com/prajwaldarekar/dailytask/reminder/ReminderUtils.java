package com.prajwaldarekar.dailytask.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.prajwaldarekar.dailytask.models.Task;

import java.util.Calendar;

public class ReminderUtils {

    /**
     * ✅ Public method to schedule reminder using full Task object.
     */
    public static void scheduleReminder(Context context, Task task) {
        try {
        if (task.getDate() == null || task.getTime() == null) return;

        // Combine date and time
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(task.getDate());

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(task.getTime());

        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerTime = calendar.getTimeInMillis();
        if (triggerTime < System.currentTimeMillis()) return; // Don't schedule past reminders

        // Cancel existing reminder to avoid duplicates
        cancelReminder(context, task.getId());

        // Call internal method with fields
        scheduleReminderInternal(context, task.getId(), task.getTitle(), triggerTime,
                task.getRepeatMode().name(), task.getCreatedAt());
        } catch (Exception e) {
            e.printStackTrace();
            // Optionally log to crash analytics or show toast
        }
    }

    /**
     * ✅ Used in BroadcastReceiver to reschedule repeat reminders
     */
    public static void scheduleReminder(Context context, long taskId, String title, long triggerTime, String repeatMode, long createdAt) {
        scheduleReminderInternal(context, taskId, title, triggerTime, repeatMode, createdAt);
    }

    /**
     * ✅ Internal common method to set AlarmManager
     */
    private static void scheduleReminderInternal(Context context, long taskId, String title,
                                                 long triggerTime, String repeatMode, long createdAt) {
        try {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("taskId", taskId);
        intent.putExtra("repeatMode", repeatMode);
        intent.putExtra("createdAt", createdAt);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    // Log warning or inform user to allow exact alarms manually in settings
                    // Optionally fallback to inexact alarm
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } catch (SecurityException se) {
            se.printStackTrace();
            // Optionally show warning that exact alarms permission is not granted
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ✅ Cancel scheduled reminder
     */
    public static void cancelReminder(Context context, int taskId) {
        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
