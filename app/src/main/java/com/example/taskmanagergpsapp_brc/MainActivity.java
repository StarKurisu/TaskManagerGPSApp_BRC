package com.example.taskmanagergpsapp_brc;

import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.taskmanagergpsapp_brc.Data.Status;
import com.example.taskmanagergpsapp_brc.Data.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel viewModel;
    private TaskAdapter todoAdapter;
    private TaskAdapter inProgressAdapter;
    private TaskAdapter doneAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        androidx.recyclerview.widget.RecyclerView todoRecycler = findViewById(R.id.todoRecycler);
        androidx.recyclerview.widget.RecyclerView inProgressRecycler = findViewById(R.id.inProgressRecycler);
        androidx.recyclerview.widget.RecyclerView doneRecycler = findViewById(R.id.doneRecycler);

        todoAdapter = new TaskAdapter(new ArrayList<>());
        inProgressAdapter = new TaskAdapter(new ArrayList<>());
        doneAdapter = new TaskAdapter(new ArrayList<>());

        todoRecycler.setLayoutManager(new LinearLayoutManager(this));
        inProgressRecycler.setLayoutManager(new LinearLayoutManager(this));
        doneRecycler.setLayoutManager(new LinearLayoutManager(this));

        todoRecycler.setAdapter(todoAdapter);
        inProgressRecycler.setAdapter(inProgressAdapter);
        doneRecycler.setAdapter(doneAdapter);

        viewModel.getTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                List<Task> todo = new ArrayList<>();
                List<Task> inProgress = new ArrayList<>();
                List<Task> done = new ArrayList<>();

                for (Task t : tasks) {
                    switch (t.getStatus()) {
                        case TODO: todo.add(t); break;
                        case IN_PROGRESS: inProgress.add(t); break;
                        case DONE: done.add(t); break;
                    }
                }

                todoAdapter.updateTasks(todo);
                inProgressAdapter.updateTasks(inProgress);
                doneAdapter.updateTasks(done);
            }
        });

        // Sample data
        viewModel.addTask(new Task(1, "Diseñar interfaz", "Task nº 1", Status.TODO));
        viewModel.addTask(new Task(2, "Prototipado básico", "", Status.IN_PROGRESS));
        viewModel.addTask(new Task(3, "Test de prueba", "", Status.DONE));


        FloatingActionButton addTaskButton = findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> showAddTaskDialog());

        todoAdapter.setOnTaskClickListener((task, newStatus) -> viewModel.moveTask(task.getId(), newStatus));
        inProgressAdapter.setOnTaskClickListener((task, newStatus) -> viewModel.moveTask(task.getId(), newStatus));
        doneAdapter.setOnTaskClickListener((task, newStatus) -> viewModel.moveTask(task.getId(), newStatus));


    }

    private void showAddTaskDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("New Task");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Task title");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString();
            if (!title.isEmpty()) {
                int id = (int) (Math.random() * 10000);
                viewModel.addTask(new Task(id, title, "", Status.TODO));
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}