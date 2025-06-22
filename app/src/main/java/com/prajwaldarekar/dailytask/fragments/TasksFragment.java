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
import com.prajwaldarekar.dailytask.viewmodel.TaskViewModel;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;
    private static final String TAG = "TasksFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();
        setupRecyclerView();
        observeTasks();
        setupFab();
        setupSwipeActions();
    }

    private void initViewModel() {
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskAdapter = new TaskAdapter(requireContext());

        taskAdapter.setOnTaskCheckChangedListener((task, isChecked) -> {
            task.setCompleted(isChecked);
            taskViewModel.update(task);
        });

        taskAdapter.setOnTaskClickListener(task -> {
            try {
                TaskDetailsDialogFragment dialog = TaskDetailsDialogFragment.newInstance(task);
                if (getParentFragmentManager() != null) {
                    dialog.show(getParentFragmentManager(), "taskDetail");
                }
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
                taskAdapter.setTasks(tasks);
            }
        });
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
                } else if (direction == ItemTouchHelper.RIGHT) {
                    try {
                        AddTaskDialogFragment.newInstance(task)
                                .show(getParentFragmentManager(), "editTask");
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening edit task dialog", e);
                        Toast.makeText(requireContext(), "Unable to edit task", Toast.LENGTH_SHORT).show();
                    }
                }

                taskAdapter.notifyItemChanged(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                float height = (float) itemView.getBottom() - itemView.getTop();
                Paint paint = new Paint();
                paint.setTextSize(40);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setAntiAlias(true);

                if (dX < 0) {
                    // Swipe Left → Delete
                    paint.setColor(Color.RED);
                    RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    canvas.drawRect(background, paint);

                    paint.setColor(Color.WHITE);
                    canvas.drawText("Delete", itemView.getRight() - 150,
                            itemView.getTop() + height / 2 + 15, paint);

                } else if (dX > 0) {
                    // Swipe Right → Edit
                    paint.setColor(Color.BLUE);
                    RectF background = new RectF(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + dX, itemView.getBottom());
                    canvas.drawRect(background, paint);

                    paint.setColor(Color.WHITE);
                    canvas.drawText("Edit", itemView.getLeft() + 150,
                            itemView.getTop() + height / 2 + 15, paint);
                }
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerViewTasks);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
