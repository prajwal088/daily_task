package com.prajwaldarekar.dailytask.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.prajwaldarekar.dailytask.models.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM tasks ORDER BY date ASC, time ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE date = :date")
    LiveData<List<Task>> getTasksByDate(long date); // Optional

    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteTaskById(int id);
}
