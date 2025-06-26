package com.prajwaldarekar.dailytask.utils;

import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.models.TaskType;
import com.prajwaldarekar.dailytask.models.RepeatMode;

import java.util.Calendar;
import java.util.Date;

public class TaskUtils {

    /**
     * Returns the effective display date based on repeat mode and context.
     * @param task         The task object (must be REMINDER).
     * @param contextDate  The context (today for Task Tab, selected date for Calendar Tab).
     */
    public static Date getEffectiveDisplayDate(Task task, Date contextDate) {
        if (task.getType() != TaskType.REMINDER || task.getRepeatMode() == null) {
            return task.getDate();
        }

        Calendar base = Calendar.getInstance();
        base.setTime(task.getDate());

        Calendar context = Calendar.getInstance();
        context.setTime(contextDate);

        Calendar effective = Calendar.getInstance();
        effective.setTime(contextDate);

        switch (task.getRepeatMode()) {
            case DAILY:
                return context.getTime();

            case WEEKLY:
                effective.set(Calendar.DAY_OF_WEEK, base.get(Calendar.DAY_OF_WEEK));
                if (context.after(effective)) {
                    effective.add(Calendar.WEEK_OF_YEAR, 1);
                }
                return effective.getTime();

            case MONTHLY:
                effective.set(Calendar.DAY_OF_MONTH, base.get(Calendar.DAY_OF_MONTH));
                if (context.after(effective)) {
                    effective.add(Calendar.MONTH, 1);
                }
                return effective.getTime();

            default:
                return task.getDate();
        }
    }
}
