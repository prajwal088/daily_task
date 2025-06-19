package com.prajwaldarekar.dailytask.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.prajwaldarekar.dailytask.utils.Converters;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "tasks")
@TypeConverters(Converters.class)
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private Date date;      // Task date
    private Date time;      // Optional time
    private boolean isCompleted;
    private TaskType type;
    private RepeatMode repeatMode;      // NONE, DAILY, WEEKLY, MONTHLY
    private long createdAt;     // Unix timestamp
    private String notes;

    // 🔨 Default Constructor
    public Task() {
        this.createdAt = System.currentTimeMillis();
        this.repeatMode = RepeatMode.NONE;
    }

    @Ignore
    // 🔨 Full Constructor (for inserting new tasks)
    public Task(String title, String description, Date date, TaskType type, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
        this.isCompleted = isCompleted;
        this.createdAt = System.currentTimeMillis();
        this.repeatMode = RepeatMode.NONE;
    }

    // --- 🧭 Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }

    public RepeatMode getRepeatMode() { return repeatMode; }
    public void setRepeatMode(RepeatMode repeatMode) { this.repeatMode = repeatMode; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}