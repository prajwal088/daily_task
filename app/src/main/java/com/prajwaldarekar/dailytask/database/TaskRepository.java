package com.prajwaldarekar.dailytask.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.prajwaldarekar.dailytask.models.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> completedTasks;
    private final LiveData<List<Task>> pendingTasks;

    private final ExecutorService executorService;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        taskDao = db.taskDao();
        allTasks = taskDao.getAllTasks();
        completedTasks = taskDao.getCompletedTasks();
        pendingTasks = taskDao.getPendingTasks();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return completedTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return pendingTasks;
    }

    public void insert(Task task) {
        executorService.execute(() -> taskDao.insertTask(task));
    }

    public void update(Task task) {
        executorService.execute(() -> taskDao.updateTask(task));
    }

    public void delete(Task task) {
        executorService.execute(() -> taskDao.deleteTask(task));
    }

    public void deleteById(int id) {
        executorService.execute(() -> taskDao.deleteTaskById(id));
    }
}
