package com.prajwaldarekar.dailytask.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.prajwaldarekar.dailytask.adapters.TaskAdapter;
import com.prajwaldarekar.dailytask.databinding.FragmentTasksBinding;
import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.models.TaskType;
import com.prajwaldarekar.dailytask.utils.TaskUtils;
import com.prajwaldarekar.dailytask.models.TaskCompletion;
import com.prajwaldarekar.dailytask.viewmodel.TaskCompletionViewModel;
import com.prajwaldarekar.dailytask.viewmodel.TaskViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;
    private TaskCompletionViewModel taskCompletionViewModel;

    private static final String TOAST_COMPLETE = "Marked completed for today";
    private static final String TOAST_INCOMPLETE = "Marked incomplete for today";

    private boolean isDialogOpen = false;

    private long todayEpoch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        todayEpoch = getTodayEpochMillis();
        initViewModels();
        setupRecyclerView();
        observeTasksWithCompletions();
        setupFab();
        setupSwipeActions();
    }

    private void initViewModels() {
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskCompletionViewModel = new ViewModelProvider(requireActivity()).get(TaskCompletionViewModel.class);
        taskAdapter = new TaskAdapter(requireContext());

        taskAdapter.setOnTaskCheckChangedListener((task, isChecked) -> {
            if (task.getType() == TaskType.REMINDER) {
                if (isChecked) {
                    taskCompletionViewModel.markTaskCompleted(task.getId(), todayEpoch);
                    Toast.makeText(requireContext(), TOAST_COMPLETE, Toast.LENGTH_SHORT).show();
                } else {
                    taskCompletionViewModel.deleteCompletion(task.getId(), todayEpoch);
                    Toast.makeText(requireContext(), TOAST_INCOMPLETE, Toast.LENGTH_SHORT).show();
                }
            } else {
                task.setCompleted(isChecked);
                taskViewModel.update(task);
            }
        });

        taskAdapter.setOnTaskClickListener(task -> {
            try {
                TaskDetailsDialogFragment.newInstance(task)
                        .show(getParentFragmentManager(), "taskDetail");
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Unable to open task details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void observeTasksWithCompletions() {
        MediatorLiveData<List<Task>> combinedData = new MediatorLiveData<>();
        List<Task> currentTasks = new ArrayList<>();
        List<TaskCompletion> currentCompletions = new ArrayList<>();

        Observer<Object> updateObserver = ignored -> {
            if (!currentTasks.isEmpty()) {
                Date today = new Date(todayEpoch);

                for (Task task : currentTasks) {
                    if (task.getType() == TaskType.REMINDER) {
                        task.setCompleted(isTaskMarkedCompleted(task.getId(), currentCompletions));
                        task.setDisplayDate(TaskUtils.getEffectiveDisplayDate(task, today));
                    }
                }

                List<Task> sorted = currentTasks.stream()
                        .sorted((t1, t2) -> {
                            boolean t1Completed = t1.isCompleted();
                            boolean t2Completed = t2.isCompleted();

                            if (t1Completed != t2Completed) {
                                return Boolean.compare(t1Completed, t2Completed); // incomplete first
                            }

                            Date now = new Date();
                            Date t1Date = t1.getDate() != null ? t1.getDate() : new Date(0);
                            Date t2Date = t2.getDate() != null ? t2.getDate() : new Date(0);

                            boolean t1Overdue = t1Date.before(now) && !t1Completed;
                            boolean t2Overdue = t2Date.before(now) && !t2Completed;

                            if (t1Overdue != t2Overdue)
                                return Boolean.compare(!t1Overdue, !t2Overdue); // overdue first

                            if (t1Completed) {
                                long t1Time = getCompletedAt(t1.getId(), currentCompletions);
                                long t2Time = getCompletedAt(t2.getId(), currentCompletions);
                                return Long.compare(t2Time, t1Time); // latest completed first
                            }

                            return Long.compare(t1Date.getTime(), t2Date.getTime());
                        })
                        .collect(Collectors.toList());

                taskAdapter.setTasks(sorted);
            }
        };

        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            currentTasks.clear();
            if (tasks != null) currentTasks.addAll(tasks);
            updateObserver.onChanged(null);
        });

        taskCompletionViewModel.getAllForDate(todayEpoch).observe(getViewLifecycleOwner(), completions -> {
            currentCompletions.clear();
            if (completions != null) currentCompletions.addAll(completions);
            updateObserver.onChanged(null);
        });
    }

    private long getCompletedAt(long taskId, List<TaskCompletion> completions) {
        for (TaskCompletion c : completions) {
            if (c.getTaskId() == taskId && c.isCompleted()) {
                return c.getCompletedAt();
            }
        }
        return 0;
    }

    private boolean isTaskMarkedCompleted(long taskId, List<TaskCompletion> completions) {
        for (TaskCompletion c : completions) {
            if (c.getTaskId() == taskId && c.isCompleted()) {
                return true;
            }
        }
        return false;
    }

    private static final long DEBOUNCE_DELAY_MS = 700;
    private boolean isFabClickable = true;

    private void setupFab() {
        binding.fabAddTask.setOnClickListener(v -> {
            if (!isFabClickable || isDialogOpen) return;

            isFabClickable = false;

            try {
                AddTaskDialogFragment dialog = AddTaskDialogFragment.newInstance(null);
                dialog.show(getChildFragmentManager(), "addTask");
                isDialogOpen = true;

            } catch (Exception e) {
                Toast.makeText(requireContext(), "Unable to open add task dialog", Toast.LENGTH_SHORT).show();
                isDialogOpen = false; // fallback if dialog fails
            }

            // Re-enable FAB tap after short delay
            binding.fabAddTask.postDelayed(() -> isFabClickable = true, DEBOUNCE_DELAY_MS);
        });
    }

    // Callback for dialog dismissal
    public void onDialogDismissed() {
        isDialogOpen = false;
        Log.d("TasksFragment", "Dialog dismissed and flag reset.");
    }


    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Task task = taskAdapter.getTaskAtPosition(position);
                if (task == null) return;

                if (direction == ItemTouchHelper.LEFT) {
                    taskViewModel.delete(task);
                    Snackbar.make(binding.getRoot(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> taskViewModel.insert(task))
                            .show();
                } else {
                    try {
                        AddTaskDialogFragment.newInstance(task)
                                .show(getParentFragmentManager(), "editTask");
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Unable to edit task", Toast.LENGTH_SHORT).show();
                    }
                }

                taskAdapter.notifyItemChanged(position); // reset swiped item
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                float height = itemView.getBottom() - itemView.getTop();
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setTextSize(40);
                paint.setTextAlign(Paint.Align.CENTER);

                if (dX < 0) { // Left = Delete
                    paint.setColor(Color.RED);
                    canvas.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
                    paint.setColor(Color.WHITE);
                    canvas.drawText("Delete", itemView.getRight() - 150, itemView.getTop() + height / 2 + 15, paint);
                } else if (dX > 0) { // Right = Edit
                    paint.setColor(Color.BLUE);
                    canvas.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), paint);
                    paint.setColor(Color.WHITE);
                    canvas.drawText("Edit", itemView.getLeft() + 150, itemView.getTop() + height / 2 + 15, paint);
                }
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerViewTasks);
    }

    private long getTodayEpochMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
