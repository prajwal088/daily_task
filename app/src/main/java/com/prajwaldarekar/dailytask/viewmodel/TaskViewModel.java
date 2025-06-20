package com.prajwaldarekar.dailytask.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.prajwaldarekar.dailytask.database.TaskRepository;
import com.prajwaldarekar.dailytask.models.Task;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;

    // LiveData for observing task states
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> completedTasks;
    private final LiveData<List<Task>> pendingTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        this.repository = new TaskRepository(application);
        this.allTasks = repository.getAllTasks();
        this.completedTasks = repository.getCompletedTasks();
        this.pendingTasks = repository.getPendingTasks();
    }

    // 🟡 Get All Tasks
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    // ✅ Get Completed Tasks
    public LiveData<List<Task>> getCompletedTasks() {
        return completedTasks;
    }

    // ❗ Get Pending Tasks
    public LiveData<List<Task>> getPendingTasks() {
        return pendingTasks;
    }

    // 🔧 Task Actions
    public void insert(Task task) {
        repository.insert(task);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }
}
