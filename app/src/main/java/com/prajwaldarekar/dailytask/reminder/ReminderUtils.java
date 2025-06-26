package com.prajwaldarekar.dailytask.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.prajwaldarekar.dailytask.models.Task;

import java.util.Calendar;

public class ReminderUtils {

    /**
     * 📅 Schedule a reminder from a Task object.
     */
    public static void scheduleReminder(Context context, Task task) {
        try {
            if (task.getDate() == null || task.getTime() == null) return;

            // 🕒 Combine date and time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(task.getDate());

            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(task.getTime());

            calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long triggerTime = calendar.getTimeInMillis();
            if (triggerTime < System.currentTimeMillis()) {
                Toast.makeText(context, "Reminder time is in the past. Skipped.", Toast.LENGTH_SHORT).show();
                FirebaseCrashlytics.getInstance().log("Skipped scheduling past reminder for task ID: " + task.getId());
                return;
            }

            // ❌ Cancel previous reminder if any
            cancelReminder(context, task.getId());

            // ✅ Schedule new reminder
            scheduleReminderInternal(
                    context,
                    task.getId(),
                    task.getTitle(),
                    triggerTime,
                    task.getRepeatMode().name(),
                    task.getCreatedAt()
            );

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(context, "Failed to schedule reminder.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 🔁 Public method for rescheduling (e.g., from receiver).
     */
    public static void scheduleReminder(Context context, long taskId, String title, long triggerTime, String repeatMode, long createdAt) {
        scheduleReminderInternal(context, taskId, title, triggerTime, repeatMode, createdAt);
    }

    /**
     * 🔧 Internal alarm scheduling logic.
     */
    private static void scheduleReminderInternal(Context context, long taskId, String title,
                                                 long triggerTime, String repeatMode, long createdAt) {
        try {
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("taskId", taskId);
            intent.putExtra("repeatMode", repeatMode);
            intent.putExtra("createdAt", createdAt);

            int requestCode = Long.valueOf(taskId).hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager == null) {
                Toast.makeText(context, "Alarm Manager unavailable", Toast.LENGTH_SHORT).show();
                FirebaseCrashlytics.getInstance().log("AlarmManager is null for task ID: " + taskId);
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    FirebaseCrashlytics.getInstance().log("Fallback to inexact alarm for task ID: " + taskId);
                    Toast.makeText(context, "Reminder scheduled inexactly. Allow exact alarms in settings.", Toast.LENGTH_LONG).show();
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

        } catch (SecurityException se) {
            FirebaseCrashlytics.getInstance().recordException(se);
            Toast.makeText(context, "Permission issue scheduling reminder.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(context, "Failed to schedule reminder.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ❌ Cancel a previously scheduled reminder.
     */
    public static void cancelReminder(Context context, long taskId) {
        try {
            Intent intent = new Intent(context, ReminderReceiver.class);

            int requestCode = Long.valueOf(taskId).hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                FirebaseCrashlytics.getInstance().log("Canceled reminder for task ID: " + taskId);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
}
