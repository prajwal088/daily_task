package com.prajwaldarekar.dailytask.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.prajwaldarekar.dailytask.models.Task;

import java.util.Date;
import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    // ✅ All Tasks (ordered by date/time)
    @Query("SELECT * FROM tasks ORDER BY date ASC, time ASC")
    LiveData<List<Task>> getAllTasks();

    // ✅ Tasks for a specific date (optional use)
    @Query("SELECT * FROM tasks WHERE date = :date")
    LiveData<List<Task>> getTasksByDate(Date date); // ✅ Matches the model field type

    // ✅ Only Completed Tasks
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY date ASC, time ASC")
    LiveData<List<Task>> getCompletedTasks();

    // ✅ Only Pending (Not Completed) Tasks
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY date ASC, time ASC")
    LiveData<List<Task>> getPendingTasks();

    // ✅ Delete by ID
    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteTaskById(int id);
}
