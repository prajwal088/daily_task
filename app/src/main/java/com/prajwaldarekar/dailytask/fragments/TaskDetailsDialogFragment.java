package com.prajwaldarekar.dailytask.fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.prajwaldarekar.dailytask.R;
import com.prajwaldarekar.dailytask.models.Task;

public class TaskDetailsDialogFragment extends DialogFragment {

    private static final String TAG = "TaskDetailsDialog";
    private static final String ARG_TASK = "task_object";

    private Task task;

    public static TaskDetailsDialogFragment newInstance(Task task) {
        TaskDetailsDialogFragment fragment = new TaskDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set style to show dialog in center
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_DailyTask_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_task_details_dialog, container, false);
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout", e);
            Toast.makeText(getContext(), "Failed to load task details", Toast.LENGTH_SHORT).show();
            dismissAllowingStateLoss();
            return null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (getDialog() != null && getDialog().getWindow() != null) {
                Window window = getDialog().getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting dialog layout", e);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            if (getArguments() != null) {
                Object obj = getArguments().getSerializable(ARG_TASK);
                if (obj instanceof Task) {
                    task = (Task) obj;
                }
            }

            if (task == null) {
                Toast.makeText(getContext(), "Task data not available", Toast.LENGTH_SHORT).show();
                dismissAllowingStateLoss();
                return;
            }

            TextView titleTextView = view.findViewById(R.id.taskDetailTitle);
            TextView descriptionTextView = view.findViewById(R.id.taskDetailDescription);
            MaterialButton editButton = view.findViewById(R.id.btnEditTask);
            MaterialButton closeButton = view.findViewById(R.id.btnCloseSheet);

            titleTextView.setText(task.getTitle());
            descriptionTextView.setText(
                    task.getDescription() != null && !task.getDescription().trim().isEmpty()
                            ? task.getDescription()
                            : getString(R.string.task_description_here)
            );

            editButton.setOnClickListener(v -> {
                try {
                    AddTaskDialogFragment editFragment = AddTaskDialogFragment.newInstance(task);
                    editFragment.show(getParentFragmentManager(), "EditTaskDialog");
                    dismissAllowingStateLoss();
                } catch (Exception e) {
                    Log.e(TAG, "Error opening edit dialog", e);
                    Toast.makeText(getContext(), "Unable to open edit screen", Toast.LENGTH_SHORT).show();
                }
            });

            closeButton.setOnClickListener(v -> {
                try {
                    dismissAllowingStateLoss();
                } catch (Exception e) {
                    Log.e(TAG, "Error dismissing dialog", e);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in TaskDetailsDialog", e);
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            dismissAllowingStateLoss();
        }
    }
}
