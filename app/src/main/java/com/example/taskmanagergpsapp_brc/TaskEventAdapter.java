package com.example.taskmanagergpsapp_brc;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagergpsapp_brc.Data.Task;

import java.util.List;

public class TaskEventAdapter extends RecyclerView.Adapter<TaskEventAdapter.EventHolder> {

    private List<Task> tasks;
    private Context context;

    public TaskEventAdapter(List<Task> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @Override
    public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new EventHolder(v);
    }

    @Override
    public void onBindViewHolder(EventHolder holder, int position) {
        Task t = tasks.get(position);
        holder.textView.setText(t.getTitle());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTaskActivity.class);
            intent.putExtra("taskId", t.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class EventHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public EventHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
