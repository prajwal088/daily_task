package com.prajwaldarekar.dailytask.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prajwaldarekar.dailytask.adapters.TaskAdapter;
import com.prajwaldarekar.dailytask.databinding.FragmentCalendarBinding;
import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.models.TaskCompletion;
import com.prajwaldarekar.dailytask.models.TaskType;
import com.prajwaldarekar.dailytask.utils.TaskUtils;
import com.prajwaldarekar.dailytask.viewmodel.TaskCompletionViewModel;
import com.prajwaldarekar.dailytask.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;
    private TaskCompletionViewModel taskCompletionViewModel;

    private final Calendar selectedDate = Calendar.getInstance();
    private List<Task> allTasks = new ArrayList<>();
    private List<TaskCompletion> taskCompletionsForDate = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupViewModels();
        setupCalendarView();
        updateDateHeader();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(requireContext());

        taskAdapter.setOnTaskClickListener(this::showUpdateDialog);

        // ✅ Completion listener for checkbox interaction
        taskAdapter.setOnTaskCheckChangedListener((task, isChecked) -> {
            long dateEpoch = getSelectedDateEpoch();

            if (task.getType() == TaskType.REMINDER) {
                if (isChecked) {
                    taskCompletionViewModel.markTaskCompleted(task.getId(), dateEpoch);
                } else {
                    taskCompletionViewModel.deleteCompletion(task.getId(), dateEpoch);
                }
            } else {
                task.setCompleted(isChecked);
                taskViewModel.update(task);
            }
        });

        binding.recyclerViewCalendarTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCalendarTasks.setAdapter(taskAdapter);
    }

    private void setupViewModels() {
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskCompletionViewModel = new ViewModelProvider(requireActivity()).get(TaskCompletionViewModel.class);

        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks != null ? tasks : new ArrayList<>();
            filterTasksByDate();
        });

        observeCompletionsForSelectedDate(); // Observe reminder completions
    }

    private void setupCalendarView() {
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            updateDateHeader();
            observeCompletionsForSelectedDate();
        });
    }

    private void observeCompletionsForSelectedDate() {
        taskCompletionViewModel.getAllForDate(getSelectedDateEpoch())
                .observe(getViewLifecycleOwner(), completions -> {
                    taskCompletionsForDate = completions != null ? completions : new ArrayList<>();
                    filterTasksByDate();
                });
    }

    private void updateDateHeader() {
        binding.textViewSelectedDate.setText("Tasks for: " + formatDate(selectedDate.getTime()));
    }

    private void filterTasksByDate() {
        Date selected = selectedDate.getTime();
        long selectedEpoch = getSelectedDateEpoch();
        List<Task> filteredTasks = new ArrayList<>();

        for (Task task : allTasks) {
            boolean isReminder = task.getType() == TaskType.REMINDER;
            Date effectiveDate = TaskUtils.getEffectiveDisplayDate(task, selected);
            task.setDisplayDate(effectiveDate);
            boolean isScheduledForSelectedDay = isSameDay(effectiveDate, selected);

            if (isScheduledForSelectedDay || isReminder) {
                if (isReminder) {
                    task.setCompleted(isTaskMarkedCompleted(task.getId(), selectedEpoch));
                }
                filteredTasks.add(task);
            }
        }

        taskAdapter.setTasks(filteredTasks);

        if (filteredTasks.isEmpty()) {
            binding.textViewSelectedDate.setText(formatDate(selected) + " (No tasks)");
        }
    }

    private boolean isTaskMarkedCompleted(long taskId, long dateEpoch) {
        for (TaskCompletion completion : taskCompletionsForDate) {
            if (completion.getTaskId() == taskId && completion.getDate() == dateEpoch) {
                return completion.isCompleted();
            }
        }
        return false;
    }

    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private long getSelectedDateEpoch() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
    }

    private void showUpdateDialog(Task task) {
        AddTaskDialogFragment.newInstance(task)
                .show(getParentFragmentManager(), "UpdateTaskDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
