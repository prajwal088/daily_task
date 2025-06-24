package com.prajwaldarekar.dailytask.adapters;

import android.content.Context;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList = new ArrayList<>();
    private final Context context;

    private OnTaskClickListener taskClickListener;
    private OnTaskCheckChangedListener checkChangedListener;

    private static final SimpleDateFormat dateTimeFormat =
            new SimpleDateFormat("dd MMM yyyy | hh:mm a", Locale.getDefault());

    public TaskAdapter(Context context) {
        this.context = context;
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskCheckChangedListener {
        void onCheckChanged(Task task, boolean isChecked);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.taskClickListener = listener;
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
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
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
            textDate = itemView.findViewById(R.id.textViewDateTime);
            checkBox = itemView.findViewById(R.id.checkBoxDone);
            viewColorBadge = itemView.findViewById(R.id.viewColorBadge);
        }

        public void bind(Task task) {
            textTitle.setText(task.getTitle());

            // 🔷 Type label
            String typeLabel = task.getType().name();
            if (task.getType() == TaskType.REMINDER) {
                String repeat = (task.getRepeatMode() != null) ? task.getRepeatMode().toString() : "None";
                typeLabel += " | " + repeat;
            }
            textType.setText(typeLabel);

            // 🔶 Handle Date Display + Missed Reminder Color
            Date taskDate = task.getDate();
            boolean isMissedReminder = false;

            if (taskDate != null) {
                textDate.setText(dateTimeFormat.format(taskDate));
                if (task.getType() == TaskType.REMINDER && !task.isCompleted()) {
                    isMissedReminder = taskDate.before(new Date());
                }
            } else {
                textDate.setText("No Date");
            }

            // 🎨 Color Badge Logic
            int colorResId = R.color.green;
            switch (task.getType()) {
                case REMINDER:
                    colorResId = isMissedReminder ? R.color.red : R.color.green;
                    break;
                case NOTE:
                    colorResId = R.color.blue;
                    break;
                case TASK:
                    colorResId = R.color.purple_500;
                    break;
            }
            viewColorBadge.setBackgroundColor(ContextCompat.getColor(context, colorResId));

            // ✅ CheckBox setup
            checkBox.setOnCheckedChangeListener(null); // avoid recycling issues
            checkBox.setChecked(task.isCompleted());

            applyStrikeThrough(task.isCompleted());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (checkChangedListener != null && isChecked != task.isCompleted()) {
                    checkChangedListener.onCheckChanged(task, isChecked);
                }
            });

            // 📱 Item Click
            itemView.setOnClickListener(v -> {
                if (taskClickListener != null) {
                    taskClickListener.onTaskClick(task);
                }
            });
        }

        private void applyStrikeThrough(boolean isCompleted) {
            for (TextView tv : new TextView[]{textTitle, textType, textDate}) {
                tv.setPaintFlags(isCompleted
                        ? tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                        : tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG
                );
            }
        }
    }
}
