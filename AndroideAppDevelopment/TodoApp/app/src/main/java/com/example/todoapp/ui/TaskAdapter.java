package com.example.todoapp.ui;

import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    /** Bridges row interactions back to the hosting Activity. */
    public interface Listener {
        void onTaskCheckedChanged(Task task, boolean isChecked);
        void onTaskDeleteClicked(Task task);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final Listener listener;

    public TaskAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the full task list and refreshes the UI. Safe to call repeatedly/rapidly. */
    public void submitList(List<Task> newTasks) {
        tasks.clear();
        if (newTasks != null) {
            tasks.addAll(newTasks);
        }
        notifyDataSetChanged();
    }

    public int getTaskCount() {
        return tasks.size();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        if (position < 0 || position >= tasks.size()) {
            return; // defensive: avoids crashes if the list mutates mid-bind on rapid taps
        }
        Task task = tasks.get(position);

        // Clear listener before setting checked state, so restoring the checkbox
        // for a recycled row doesn't fire a spurious callback.
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted());
        applyCompletedStyle(holder, task.isCompleted());

        holder.tvTaskTitle.setText(task.getTitle());

        if (TextUtils.isEmpty(task.getNotes())) {
            holder.tvTaskNotes.setVisibility(View.GONE);
        } else {
            holder.tvTaskNotes.setVisibility(View.VISIBLE);
            holder.tvTaskNotes.setText(task.getNotes());
        }

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyCompletedStyle(holder, isChecked);
            if (listener != null) {
                listener.onTaskCheckedChanged(task, isChecked);
            }
        });

        holder.btnDeleteTask.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskDeleteClicked(task);
            }
        });
    }

    private void applyCompletedStyle(TaskViewHolder holder, boolean completed) {
        if (completed) {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_FLAG);
            holder.tvTaskTitle.setAlpha(0.55f);
            holder.tvTaskNotes.setAlpha(0.55f);
        } else {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_FLAG);
            holder.tvTaskTitle.setAlpha(1f);
            holder.tvTaskNotes.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final CheckBox cbCompleted;
        final TextView tvTaskTitle;
        final TextView tvTaskNotes;
        final ImageButton btnDeleteTask;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskNotes = itemView.findViewById(R.id.tvTaskNotes);
            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask);
        }
    }
}
