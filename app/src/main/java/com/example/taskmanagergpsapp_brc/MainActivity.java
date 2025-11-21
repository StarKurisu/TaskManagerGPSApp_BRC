package com.example.taskmanagergpsapp_brc;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.util.Log;
import android.widget.Button;

import com.example.taskmanagergpsapp_brc.Data.Status;
import com.example.taskmanagergpsapp_brc.Data.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private TaskViewModel viewModel;
    private TaskAdapter todoAdapter;
    private TaskAdapter inProgressAdapter;
    private TaskAdapter doneAdapter;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private double lastLatitude = 0.0;
    private double lastLongitude = 0.0;
    private static final int EDIT_TASK_REQUEST = 2001;
    private void openEditTask(Task task) {
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("task", task);
        startActivityForResult(intent, EDIT_TASK_REQUEST);
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(
                this, location -> {
                    if (location != null) {
                        lastLatitude = location.getLatitude();
                        lastLongitude = location.getLongitude();
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }
        androidx.recyclerview.widget.RecyclerView todoRecycler = findViewById(R.id.todoRecycler);
        androidx.recyclerview.widget.RecyclerView inProgressRecycler = findViewById(R.id.inProgressRecycler);
        androidx.recyclerview.widget.RecyclerView doneRecycler = findViewById(R.id.doneRecycler);

        todoAdapter = new TaskAdapter(getApplicationContext(), new ArrayList<>());
        inProgressAdapter = new TaskAdapter(getApplicationContext(), new ArrayList<>());
        doneAdapter = new TaskAdapter(getApplicationContext(), new ArrayList<>());

        todoRecycler.setLayoutManager(new LinearLayoutManager(this));
        inProgressRecycler.setLayoutManager(new LinearLayoutManager(this));
        doneRecycler.setLayoutManager(new LinearLayoutManager(this));

        todoRecycler.setAdapter(todoAdapter);
        inProgressRecycler.setAdapter(inProgressAdapter);
        doneRecycler.setAdapter(doneAdapter);

        todoAdapter.setOnEditClickListener(this::openEditTask);
        inProgressAdapter.setOnEditClickListener(this::openEditTask);
        doneAdapter.setOnEditClickListener(this::openEditTask);

        FloatingActionButton btnExport = findViewById(R.id.btnExport);
        FloatingActionButton btnImport = findViewById(R.id.btnImport);

        btnExport.setOnClickListener(v -> viewModel.exportTasks(this, "tasks.json"));
        btnImport.setOnClickListener(v -> viewModel.importTasks(this, "tasks.json"));

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

        /*
        viewModel.addTask(new Task("Diseñar interfaz", "Task nº 1", Status.TODO, lastLongitude, lastLatitude));
        viewModel.addTask(new Task( "Prototipado básico", "", Status.IN_PROGRESS, lastLongitude, lastLatitude));
        viewModel.addTask(new Task( "Test de prueba", "", Status.DONE, lastLongitude, lastLatitude));
*/

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
                viewModel.addTask(new Task(title, "", Status.TODO, lastLatitude, lastLongitude));
                Log.d("Task Created", "Lat: " + lastLatitude + ", Lng: " + lastLongitude);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_TASK_REQUEST && resultCode == RESULT_OK && data != null) {
            Task updated = (Task) data.getSerializableExtra("updatedTask");
            viewModel.updateTask(updated);
        }
    }
}