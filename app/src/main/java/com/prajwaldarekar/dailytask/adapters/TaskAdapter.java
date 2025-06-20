package com.prajwaldarekar.dailytask.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prajwaldarekar.dailytask.R;
import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.models.TaskType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList = new ArrayList<>();
    private OnTaskCheckChangedListener checkChangedListener;

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnTaskCheckChangedListener {
        void onCheckChanged(Task task, boolean isChecked);
    }

    public void setOnTaskCheckChangedListener(OnTaskCheckChangedListener listener) {
        this.checkChangedListener = listener;
    }

    public void setTasks(List<Task> tasks) {
        taskList.clear();
        if (tasks != null) {
            taskList.addAll(tasks);
        }
        notifyDataSetChanged(); // Consider DiffUtil for optimization
    }

    public Task getTaskAtPosition(int position) {
        return taskList.get(position);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(taskList.get(position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        final TextView textTitle, textType, textDate;
        final CheckBox checkBox;
        final View viewColorBadge;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textViewTitle);
            textType = itemView.findViewById(R.id.textViewType);
            textDate = itemView.findViewById(R.id.textViewDate);
            checkBox = itemView.findViewById(R.id.checkBoxDone);
            viewColorBadge = itemView.findViewById(R.id.viewColorBadge);
        }

        public void bind(Task task) {
            textTitle.setText(task.getTitle());
            textType.setText(task.getType().name());

            if (task.getDate() != null) {
                textDate.setText(dateFormat.format(task.getDate()));
            } else {
                textDate.setText("No Date");
            }

            // 🎨 Badge color by type
            int colorResId = R.color.green;
            switch (task.getType()) {
                case REMINDER: colorResId = R.color.red; break;
                case NOTE:     colorResId = R.color.blue; break;
                case TASK:     colorResId = R.color.purple_500; break;
            }
            viewColorBadge.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), colorResId)
            );

            // ☑ Prevent rebinding callback
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(task.isCompleted());

            // ✏️ Strike-through
            if (task.isCompleted()) {
                textTitle.setPaintFlags(textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                textTitle.setPaintFlags(textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (checkChangedListener != null && isChecked != task.isCompleted()) {
                    checkChangedListener.onCheckChanged(task, isChecked);
                }
            });

            // Optional: Make whole item clickable
            itemView.setOnClickListener(v -> checkBox.performClick());
        }
    }
}
