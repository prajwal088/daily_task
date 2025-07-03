package com.prajwaldarekar.dailytask.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents the completion state of a task on a specific date.
 * Used for tracking per-day status of recurring tasks (e.g., daily/weekly).
 */
@Entity(tableName = "task_completion")
public class TaskCompletion {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "task_id")
    private long taskId;

    @ColumnInfo(name = "completion_date")
    private long date;  // Epoch millis at 00:00 of the day

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    // Constructor
    public TaskCompletion(long taskId, long date, boolean isCompleted) {
        this.taskId = taskId;
        this.date = date;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    @NonNull
    @Override
    public String toString() {
        return "TaskCompletion{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", date=" + date +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
