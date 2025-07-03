package com.prajwaldarekar.dailytask.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prajwaldarekar.dailytask.models.TaskCompletion;

import java.util.List;

/**
 * DAO for managing task completion records for recurring tasks on a per-day basis.
 */
@Dao
public interface TaskCompletionDao {

    /**
     * Insert or update a task completion record for a specific date.
     * If the record already exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(TaskCompletion completion);

    /**
     * Get the LiveData of the task's completion status on a specific date.
     * Useful for observing changes in completion state.
     */
    @Query("SELECT * FROM task_completion WHERE task_id = :taskId AND completion_date = :date LIMIT 1")
    LiveData<TaskCompletion> getAllCompletionsForTaskOnDate(long taskId, long date);

    /**
     * Get all completion records for a specific date.
     * Used to show completion status of all tasks on a given day.
     */
    @Query("SELECT * FROM task_completion WHERE completion_date = :date")
    LiveData<List<TaskCompletion>> getAllForDate(long date);

    /**
     * Get full completion history of a specific task.
     * Useful for analytics, streak tracking, etc.
     */
    @Query("SELECT * FROM task_completion WHERE task_id = :taskId ORDER BY completion_date ASC")
    LiveData<List<TaskCompletion>> getAllCompletionsForTask(long taskId);

    /**
     * Delete all completion records associated with a task.
     * Called when a task is permanently deleted.
     */
    @Query("DELETE FROM task_completion WHERE task_id = :taskId")
    void deleteAllCompletionsForTask(long taskId);

    /**
     * Delete a specific completion record for a task on a given date.
     * Used when unmarking completion for that day.
     */
    @Query("DELETE FROM task_completion WHERE task_id = :taskId AND completion_date = :date")
    void deleteCompletion(long taskId, long date);
}
