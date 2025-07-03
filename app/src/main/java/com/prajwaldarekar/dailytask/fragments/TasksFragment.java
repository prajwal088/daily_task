package com.prajwaldarekar.dailytask.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prajwaldarekar.dailytask.adapters.TaskAdapter;
import com.prajwaldarekar.dailytask.databinding.FragmentTasksBinding;
import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.models.TaskType;
import com.prajwaldarekar.dailytask.utils.TaskUtils;
import com.prajwaldarekar.dailytask.viewmodel.TaskCompletionViewModel;
import com.prajwaldarekar.dailytask.viewmodel.TaskViewModel;

import java.util.Calendar;
import java.util.Date;

// 🔄 Imports remain the same...

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;
    private TaskCompletionViewModel taskCompletionViewModel;

    private static final String TAG = "TasksFragment";
    private static final String TOAST_COMPLETE = "Marked completed for today";
    private static final String TOAST_INCOMPLETE = "Marked incomplete for today";

    private long todayEpoch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        todayEpoch = getTodayEpochMillis();
        initViewModels();
        setupRecyclerView();
        observeTasks();
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
                Log.e(TAG, "Error showing TaskDetailsDialogFragment", e);
                Toast.makeText(requireContext(), "Unable to open task details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void observeTasks() {
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                taskCompletionViewModel.getAllForDate(todayEpoch)
                        .observe(getViewLifecycleOwner(), completions -> {
                            Date today = new Date(todayEpoch);
                            for (Task task : tasks) {
                                if (task.getType() == TaskType.REMINDER) {
                                    task.setCompleted(isTaskMarkedCompleted(task.getId(), completions));
                                    task.setDisplayDate(TaskUtils.getEffectiveDisplayDate(task, today));
                                }
                            }

                            // ✅ Sort logic:
                            tasks.sort((t1, t2) -> {
                                // Incomplete tasks first
                                boolean t1Done = t1.isCompleted();
                                boolean t2Done = t2.isCompleted();

                                if (t1Done != t2Done) {
                                    return t1Done ? 1 : -1; // Completed = lower priority
                                }

                                // ✅ If both are completed, prioritize latest completion
                                if (t1Done && t2Done) {
                                    long t1Time = t1.getDate().getTime();
                                    long t2Time = t2.getDate().getTime();
                                    return Long.compare(t2Time, t1Time); // latest first
                                }

                                // ✅ If both are incomplete, sort by scheduled time
                                return Long.compare(t1.getDate().getTime(), t2.getDate().getTime());
                            });

                            taskAdapter.setTasks(tasks);
                        });
            }
        });
    }


    private boolean isTaskMarkedCompleted(long taskId, java.util.List<com.prajwaldarekar.dailytask.models.TaskCompletion> completions) {
        for (com.prajwaldarekar.dailytask.models.TaskCompletion c : completions) {
            if (c.getTaskId() == taskId && c.isCompleted()) {
                return true;
            }
        }
        return false;
    }

    private void setupFab() {
        binding.fabAddTask.setOnClickListener(v -> {
            try {
                AddTaskDialogFragment.newInstance(null)
                        .show(getParentFragmentManager(), "addTask");
            } catch (Exception e) {
                Log.e(TAG, "Error opening AddTaskDialogFragment", e);
                Toast.makeText(requireContext(), "Unable to open add task dialog", Toast.LENGTH_SHORT).show();
            }
        });
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
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        AddTaskDialogFragment.newInstance(task)
                                .show(getParentFragmentManager(), "editTask");
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening edit task dialog", e);
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