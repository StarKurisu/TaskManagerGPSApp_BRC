package com.example.taskmanagergpsapp_brc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagergpsapp_brc.Data.Status;
import com.example.taskmanagergpsapp_brc.Data.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.title.setText(tasks.get(position).getTitle());

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
