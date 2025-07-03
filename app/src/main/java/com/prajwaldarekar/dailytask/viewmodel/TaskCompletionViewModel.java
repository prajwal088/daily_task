package com.prajwaldarekar.dailytask.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.prajwaldarekar.dailytask.database.TaskCompletionRepository;
import com.prajwaldarekar.dailytask.models.TaskCompletion;

import java.util.List;

public class TaskCompletionViewModel extends AndroidViewModel {

    private final TaskCompletionRepository repository;

    public TaskCompletionViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskCompletionRepository(application);
    }

    /**
     * ✅ Mark task as completed on a specific date (non-blocking).
     */
    public void markTaskCompleted(long taskId, long date) {
        repository.markTaskCompleted(taskId, date);
    }

    /**
     * ✅ Get LiveData of completion status for a specific task on a specific date.
     */
    public LiveData<TaskCompletion> getAllCompletionsForTaskOnDate(long taskId, long date) {
        return repository.getAllCompletionsForTaskOnDate(taskId, date);
    }

    /**
     * ✅ Get LiveData of all completions on a specific date.
     */
    public LiveData<List<TaskCompletion>> getAllForDate(long date) {
        return repository.getAllForDate(date);
    }

    /**
     * ✅ Get LiveData list of completions for a specific task.
     */
    public LiveData<List<TaskCompletion>> getAllCompletionsForTask(long taskId) {
        return repository.getAllCompletionsForTask(taskId);
    }

    /**
     * ✅ Delete all completion records of a task.
     */
    public void deleteAllCompletionsForTask(long taskId) {
        repository.deleteAllCompletionsForTask(taskId);
    }

    /**
     * ✅ Delete completion entry for a task on a specific date.
     */
    public void deleteCompletion(long taskId, long date) {
        repository.deleteCompletion(taskId, date);
    }
}
