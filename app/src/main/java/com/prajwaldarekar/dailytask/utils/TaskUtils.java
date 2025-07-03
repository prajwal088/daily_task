package com.prajwaldarekar.dailytask.utils;

import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.models.TaskType;
import com.prajwaldarekar.dailytask.models.RepeatMode;

import java.util.Calendar;
import java.util.Date;

public class TaskUtils {

    /**
     * Returns the effective display date based on repeat mode and context.
     * Used to decide which date the reminder is "visible" on.
     *
     * @param task        The reminder task.
     * @param contextDate The reference date (today in Tasks tab, selected date in Calendar).
     * @return Date when the task should appear (with original reminder time).
     */
    public static Date getEffectiveDisplayDate(Task task, Date contextDate) {
        if (task.getType() != TaskType.REMINDER || task.getRepeatMode() == null) {
            return task.getDate();
        }

        Calendar base = Calendar.getInstance();
        base.setTime(task.getDate()); // Original reminder date (contains original time)

        Calendar context = Calendar.getInstance();
        context.setTime(contextDate); // Today or selected calendar date

        Calendar effective = Calendar.getInstance();
        effective.setTime(contextDate); // Start with context date

        switch (task.getRepeatMode()) {
            case DAILY:
                // Use today's date or selected date + original time
                break;

            case WEEKLY:
                // Set to the same day of week as the original task
                effective.set(Calendar.DAY_OF_WEEK, base.get(Calendar.DAY_OF_WEEK));
                break;

            case MONTHLY:
                // Set to same day of month, or nearest valid day
                int baseDay = base.get(Calendar.DAY_OF_MONTH);
                int maxDay = effective.getActualMaximum(Calendar.DAY_OF_MONTH);
                effective.set(Calendar.DAY_OF_MONTH, Math.min(baseDay, maxDay));
                break;

            default:
                return task.getDate(); // Fallback
        }

        // ✅ Retain original hour/minute from task date
        effective.set(Calendar.HOUR_OF_DAY, base.get(Calendar.HOUR_OF_DAY));
        effective.set(Calendar.MINUTE, base.get(Calendar.MINUTE));
        effective.set(Calendar.SECOND, 0);
        effective.set(Calendar.MILLISECOND, 0);

        return effective.getTime();
    }
}
