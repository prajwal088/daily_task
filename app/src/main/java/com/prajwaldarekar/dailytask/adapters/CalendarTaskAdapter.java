package com.prajwaldarekar.dailytask.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prajwaldarekar.dailytask.R;
import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarTaskAdapter extends RecyclerView.Adapter<CalendarTaskAdapter.TaskViewHolder> {

    private final Context context;
    private List<Task> taskList;
    private final OnTaskClickListener listener;
    private final TaskViewModel viewModel;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public CalendarTaskAdapter(Context context, List<Task> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;

        // Optional: ViewModel can be used here if needed, else remove
        this.viewModel = null;
    }

/*    public CalendarTaskAdapter(Context context, List<Task> taskList, OnTaskClickListener listener, TaskViewModel viewModel) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
        this.viewModel = viewModel;
    }*/

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.titleTextView.setText(task.getTitle());
        holder.descTextView.setText(task.getDescription());

        if (task.getDate() != null) {
            String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(task.getDate());
            holder.dateTextView.setText(formattedDate);
        } else {
            holder.dateTextView.setText("");
        }

        holder.checkBox.setChecked(task.isCompleted());
        updateStrikeThrough(holder, task.isCompleted());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            updateStrikeThrough(holder, isChecked);
            if (viewModel != null) {
                viewModel.update(task);  // Persist change
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }

    private void updateStrikeThrough(TaskViewHolder holder, boolean isCompleted) {
        int flag = isCompleted ? Paint.STRIKE_THRU_TEXT_FLAG : 0;
        holder.titleTextView.setPaintFlags(flag);
        holder.descTextView.setPaintFlags(flag);
        holder.dateTextView.setPaintFlags(flag);
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, descTextView, dateTextView;
        CheckBox checkBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            descTextView = itemView.findViewById(R.id.textViewType);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            checkBox = itemView.findViewById(R.id.checkBoxDone);
        }
    }
}