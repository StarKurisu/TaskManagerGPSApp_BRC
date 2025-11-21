package com.example.taskmanagergpsapp_brc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagergpsapp_brc.Data.Status;
import com.example.taskmanagergpsapp_brc.Data.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private Context context;

    private OnEditClickListener editListener;

    public interface OnEditClickListener {
        void onEdit(Task task);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editListener = listener;
    }
    public TaskAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;

    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        Button btnEdit;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.title.setText(tasks.get(position).getTitle());

        holder.btnEdit.setOnClickListener(v -> {
            Task task = tasks.get(holder.getAdapterPosition());
            if (editListener != null) {
                editListener.onEdit(task);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Task task = tasks.get(holder.getAdapterPosition());
            Status newStatus;
            switch (task.getStatus()) {
                case TODO: newStatus = Status.IN_PROGRESS; break;
                case IN_PROGRESS: newStatus = Status.DONE; break;
                default: newStatus = Status.TODO; break;
            }
            if (onTaskClickListener != null)
                onTaskClickListener.onTaskClicked(task, newStatus);
        });

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    public interface OnTaskClickListener {
        void onTaskClicked(Task task, Status newStatus);
    }

    private OnTaskClickListener onTaskClickListener;

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.onTaskClickListener = listener;
    }

}
