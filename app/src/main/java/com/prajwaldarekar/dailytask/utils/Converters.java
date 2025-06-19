package com.prajwaldarekar.dailytask.utils;

import androidx.room.TypeConverter;

import com.prajwaldarekar.dailytask.models.RepeatMode;
import com.prajwaldarekar.dailytask.models.TaskType;

import java.util.Date;

public class Converters {

    // 🔁 --- Date converters ---
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value != null ? new Date(value) : null;
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date != null ? date.getTime() : null;
    }

    // 🔁 --- TaskType enum converters ---
    @TypeConverter
    public static TaskType fromTaskTypeString(String value) {
        return value != null ? TaskType.valueOf(value) : TaskType.TASK;
    }

    @TypeConverter
    public static String taskTypeToString(TaskType type) {
        return type != null ? type.name() : TaskType.TASK.name();
    }

    // 🔁 --- RepeatMode enum converters (using ordinal) ---
    @TypeConverter
    public static RepeatMode fromRepeatModeOrdinal(int ordinal) {
        RepeatMode[] values = RepeatMode.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : RepeatMode.NONE;
    }

    @TypeConverter
    public static int toRepeatModeOrdinal(RepeatMode mode) {
        return mode != null ? mode.ordinal() : RepeatMode.NONE.ordinal();
    }
}
