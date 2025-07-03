package com.prajwaldarekar.dailytask.fragments;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.prajwaldarekar.dailytask.databinding.FragmentAddTaskDialogBinding;
import com.prajwaldarekar.dailytask.models.RepeatMode;
import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.models.TaskType;
import com.prajwaldarekar.dailytask.reminder.ReminderUtils;
import com.prajwaldarekar.dailytask.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTaskDialogFragment extends DialogFragment {

    private FragmentAddTaskDialogBinding binding;
    private TaskViewModel taskViewModel;
    private Task existingTask;
    private final Calendar selectedDateTime = Calendar.getInstance();

    public static AddTaskDialogFragment newInstance(@Nullable Task task) {
        AddTaskDialogFragment fragment = new AddTaskDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("task", task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddTaskDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        if (getArguments() != null) {
            existingTask = (Task) getArguments().getSerializable("task");
        }

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        setupRepeatModeSpinner();
        setupTaskTypeSpinner();

        if (existingTask != null) {
            binding.editTextTitle.setText(existingTask.getTitle());
            binding.editTextDescription.setText(existingTask.getDescription());

            selectedDateTime.setTime(existingTask.getDate() != null ? existingTask.getDate() : Calendar.getInstance().getTime());
            updateDateTimeLabels();

            binding.spinnerType.setSelection(existingTask.getType().ordinal());

            if (existingTask.getType() == TaskType.REMINDER) {
                binding.layoutRepeat.setVisibility(View.VISIBLE);
                binding.spinnerRepeatMode.setSelection(existingTask.getRepeatMode().ordinal());
            } else {
                binding.layoutRepeat.setVisibility(View.GONE);
            }
        } else {
            selectedDateTime.setTimeInMillis(System.currentTimeMillis());
            updateDateTimeLabels();
        }
    }

    // Select task type
    private void setupTaskTypeSpinner() {
        List<String> typeLabels = new ArrayList<>();
        for (TaskType type : TaskType.values()) {
            String label = type.name().toLowerCase(Locale.ROOT);
            label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
            typeLabels.add(label);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, typeLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerType.setAdapter(adapter);

        binding.spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TaskType selectedType = TaskType.values()[position];
                binding.layoutRepeat.setVisibility(selectedType == TaskType.REMINDER ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.layoutRepeat.setVisibility(View.GONE);
            }
        });
    }

    private void setupRepeatModeSpinner() {
        List<String> repeatLabels = new ArrayList<>();
        for (RepeatMode mode : RepeatMode.values()) {
            String label = mode.name().replace("_", " ").toLowerCase(Locale.ROOT);
            label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
            repeatLabels.add(label);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, repeatLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRepeatMode.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.buttonSelectDate.setOnClickListener(v -> showDatePicker());
        binding.buttonSelectTime.setOnClickListener(v -> showTimePicker());
        binding.buttonSave.setOnClickListener(v -> saveTask());
        binding.buttonCancel.setOnClickListener(v -> dismiss());
    }

    private void showDatePicker() {
        new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
                    updateDateTimeLabels();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeLabels();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void updateDateTimeLabels() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        binding.textViewDate.setText(dateFormat.format(selectedDateTime.getTime()));
        binding.textViewTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveTask() {
        try {
            String title = binding.editTextTitle.getText().toString().trim();
            String description = binding.editTextDescription.getText().toString().trim();
            TaskType type = TaskType.values()[binding.spinnerType.getSelectedItemPosition()];
            RepeatMode repeatMode = RepeatMode.NONE;

            if (type == TaskType.REMINDER) {
                int repeatIndex = binding.spinnerRepeatMode.getSelectedItemPosition();
                repeatMode = RepeatMode.values()[repeatIndex];

                if (repeatMode != RepeatMode.NONE && binding.textViewTime.getText().toString().equals("Select Time")) {
                    Toast.makeText(requireContext(), "Please select a time for recurring reminders", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (TextUtils.isEmpty(title)) {
                binding.editTextTitle.setError("Title is required");
                binding.editTextTitle.requestFocus();
                return;
            }

            if (existingTask != null) {
                existingTask.setTitle(title);
                existingTask.setDescription(description);
                existingTask.setDate(selectedDateTime.getTime());
                existingTask.setTime(selectedDateTime.getTime());
                existingTask.setType(type);
                existingTask.setRepeatMode(repeatMode);

                taskViewModel.update(existingTask);

                if (type == TaskType.REMINDER) {
                    ReminderUtils.scheduleReminder(requireContext(), existingTask);
                }

                Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show();
            } else {
                Task newTask = new Task();
                newTask.setTitle(title);
                newTask.setDescription(description);
                newTask.setDate(selectedDateTime.getTime());
                newTask.setTime(selectedDateTime.getTime());
                newTask.setType(type);
                newTask.setRepeatMode(repeatMode);
                newTask.setCompleted(false);
                newTask.setCreatedAt(System.currentTimeMillis());

                taskViewModel.insert(newTask);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                        startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                        Toast.makeText(requireContext(), "Please allow exact alarm permission to enable reminders", Toast.LENGTH_LONG).show();
                    }
                }

                if (type == TaskType.REMINDER) {
                    ReminderUtils.scheduleReminder(requireContext(), newTask);
                }

                Toast.makeText(requireContext(), "Task added", Toast.LENGTH_SHORT).show();
            }

            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save task: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}