package com.prajwaldarekar.dailytask.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.prajwaldarekar.dailytask.models.TaskCompletion;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskCompletionRepository {

    private final TaskCompletionDao completionDao;
    private final ExecutorService executorService;

    public TaskCompletionRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        completionDao = db.taskCompletionDao();
        executorService = Executors.newFixedThreadPool(2); // More responsive for concurrent ops
    }

    /**
     * Mark a task as completed on a specific date.
     * If already exists, it will be updated.
     */
    public void markTaskCompleted(long taskId, long date) {
        executorService.execute(() -> {
            long completedAt = System.currentTimeMillis();
            TaskCompletion completion = new TaskCompletion(taskId, date, true, completedAt);
            completionDao.insertOrUpdate(completion);
        });
    }

    /**
     * Returns LiveData of completion status for a specific task on a specific date.
     */
    public LiveData<TaskCompletion> getAllCompletionsForTaskOnDate(long taskId, long date) {
        return completionDao.getAllCompletionsForTaskOnDate(taskId, date);
    }

    /**
     * Returns LiveData list of all task completions for a specific date.
     */
    public LiveData<List<TaskCompletion>> getAllForDate(long date) {
        return completionDao.getAllForDate(date);
    }

    /**
     * Returns LiveData list of all completion records for a specific task.
     */
    public LiveData<List<TaskCompletion>> getAllCompletionsForTask(long taskId) {
        return completionDao.getAllCompletionsForTask(taskId);
    }

    /**
     * Deletes all completion records associated with a given task.
     */
    public void deleteAllCompletionsForTask(long taskId) {
        executorService.execute(() -> completionDao.deleteAllCompletionsForTask(taskId));
    }

    /**
     * Deletes a specific completion record for a given task and date.
     */
    public void deleteCompletion(long taskId, long date) {
        executorService.execute(() -> completionDao.deleteCompletion(taskId, date));
    }

    /**
     * Optional cleanup method (e.g., call in ViewModel.onCleared).
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
