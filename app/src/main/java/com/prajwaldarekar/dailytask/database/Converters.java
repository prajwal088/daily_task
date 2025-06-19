package com.prajwaldarekar.dailytask.database;

import androidx.room.TypeConverter;

import com.prajwaldarekar.dailytask.models.RepeatMode;
import com.prajwaldarekar.dailytask.models.TaskType;

import java.util.Date;

public class Converters {

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static String fromTaskType(TaskType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static TaskType toTaskType(String type) {
        return type == null ? null : TaskType.valueOf(type);
    }

    @TypeConverter
    public static String fromRepeatMode(RepeatMode mode) {
        return mode == null ? null : mode.name();
    }

    @TypeConverter
    public static RepeatMode toRepeatMode(String mode) {
        return mode == null ? null : RepeatMode.valueOf(mode);
    }
}
