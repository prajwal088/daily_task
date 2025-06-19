package com.prajwaldarekar.dailytask.adapters;

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
        notifyDataSetChanged();
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
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textViewTitle);
            textType = itemView.findViewById(R.id.textViewType);
            textDate = itemView.findViewById(R.id.textViewDate);
            checkBox = itemView.findViewById(R.id.checkBoxDone);
            viewColorBadge = itemView.findViewById(R.id.viewColorBadge); // 🟩 Reference from XML
        }

        public void bind(Task task) {
            textTitle.setText(task.getTitle());
            textType.setText(task.getType().name());

            if (task.getDate() != null) {
                textDate.setText(dateFormat.format(task.getDate()));
            } else {
                textDate.setText("No Date");
            }

            // ✅ Set badge color based on TaskType
            int colorResId;
            switch (task.getType()) {
                case REMINDER:
                    colorResId = R.color.red;
                    break;
                case NOTE:
                    colorResId = R.color.blue;
                    break;
                case TASK:
                    colorResId = R.color.purple_500;
                    break;
                default:
                    colorResId = R.color.green;
                    break;
            }

            viewColorBadge.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), colorResId));

            // ✅ Checkbox state
            checkBox.setOnCheckedChangeListener(null); // prevent flicker on recycling
            checkBox.setChecked(task.isCompleted());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (checkChangedListener != null) {
                    checkChangedListener.onCheckChanged(task, isChecked);
                }
            });
        }
    }
}
