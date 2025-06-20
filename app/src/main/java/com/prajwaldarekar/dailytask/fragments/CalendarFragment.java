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

import com.prajwaldarekar.dailytask.adapters.CalendarTaskAdapter;
import com.prajwaldarekar.dailytask.databinding.FragmentCalendarBinding;
import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarTaskAdapter calendarTaskAdapter;
    private TaskViewModel taskViewModel;

    private final Calendar selectedDate = Calendar.getInstance();
    private List<Task> allTasks = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        initViewModel();
        initCalendarListener();
        updateDateHeader();
    }

    private void initRecyclerView() {
        calendarTaskAdapter = new CalendarTaskAdapter(requireContext(), new ArrayList<>(), this::showUpdateDialog);
        binding.recyclerViewCalendarTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCalendarTasks.setAdapter(calendarTaskAdapter);
    }

    private void initViewModel() {
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks != null ? tasks : new ArrayList<>();
            filterTasksByDate();
        });
    }

    private void initCalendarListener() {
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            updateDateHeader();
            filterTasksByDate();
        });
    }

    private void updateDateHeader() {
        String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate.getTime());
        binding.textViewSelectedDate.setText("Tasks for: " + formattedDate);
    }

    private void filterTasksByDate() {
        Date selected = selectedDate.getTime();
        List<Task> filtered = new ArrayList<>();

        for (Task task : allTasks) {
            if (isSameDay(task.getDate(), selected)) {
                filtered.add(task);
            }
        }

        calendarTaskAdapter.setTasks(filtered);

        if (filtered.isEmpty()) {
            binding.textViewSelectedDate.append(" (No tasks)");
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void showUpdateDialog(Task task) {
        AddTaskDialogFragment dialogFragment = AddTaskDialogFragment.newInstance(task);
        dialogFragment.show(getParentFragmentManager(), "UpdateTaskDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
