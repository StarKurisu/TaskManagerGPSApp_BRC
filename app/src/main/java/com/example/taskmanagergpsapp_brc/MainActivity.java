package com.example.taskmanagergpsapp_brc;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.taskmanagergpsapp_brc.Data.Status;
import com.example.taskmanagergpsapp_brc.Data.Task;
import com.example.taskmanagergpsapp_brc.Data.TaskDao;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;


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

    private Handler handler = new Handler();
    private Runnable deadlineChecker;

    private boolean dialogShown = false;
    private List<Task> cachedTasks = new ArrayList<>();

    private static final String SERVICE_ID = "com.example.taskmanagergpsapp_brc.nearby";
    private ConnectionsClient connectionsClient;
    private String myName;
    private String connectedEndpoint = null;

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
                    } else {

                        lastLatitude = 43.374;
                        lastLongitude = 8.4;
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

        connectionsClient = Nearby.getConnectionsClient(this);
        myName = "User" + new Random().nextInt(1000);

        FloatingActionButton btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(v -> startDiscovery());

        FloatingActionButton btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.deleteAllDoneTasks();
                Toast.makeText(getApplicationContext(), "Deleted all completed tasks ):", Toast.LENGTH_SHORT);
            }
        });
    }

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {

                    if (payload.getType() == Payload.Type.BYTES) {
                        String json = new String(payload.asBytes(), StandardCharsets.UTF_8);

                        Gson gson = new Gson();

                        Type taskListType = new TypeToken<List<Task>>(){}.getType();
                        List<Task> received = gson.fromJson(json, taskListType);


                        for (Task t : received) {
                            viewModel.addTask(t);
                        }

                        Toast.makeText(MainActivity.this,
                                "Received " + received.size() + " tasks",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) { }
            };


    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo info) {

                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            connectedEndpoint = endpointId;

                            Toast.makeText(MainActivity.this,
                                    "Connected to " + endpointId,
                                    Toast.LENGTH_SHORT).show();

                            connectionsClient.stopDiscovery();

                            sendTasksToDevice();
                            break;

                        default:
                            Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    connectedEndpoint = null;
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                }
            };

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {

                    connectionsClient.requestConnection(myName, endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) { }
            };

    private void startDiscovery() {
        DiscoveryOptions options =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();

        connectionsClient.startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                options
        ).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Discovering nearby users…", Toast.LENGTH_SHORT).show();
        });
    }
    private void sendTasksToDevice() {
        if (connectedEndpoint == null) return;

        Gson gson = new Gson();

        String json = gson.toJson(cachedTasks);
        Payload payload = Payload.fromBytes(json.getBytes(StandardCharsets.UTF_8));

        connectionsClient.sendPayload(connectedEndpoint, payload);
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
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String defaultDateTime = sdf.format(calendar.getTime());
                viewModel.addTask(new Task(title, "", Status.TODO, lastLatitude, lastLongitude, defaultDateTime));
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

    @Override
    protected void onResume() {
        super.onResume();

        viewModel.getTasks().observe(this, tasks -> {
            cachedTasks.clear();
            cachedTasks.addAll(tasks);
        });

        deadlineChecker = () -> {

            long now = System.currentTimeMillis();

            for (Task task : cachedTasks) {

                if (task.finalDateTime == null) continue;

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    long deadline = sdf.parse(task.finalDateTime).getTime();

                    if (now >= deadline && !dialogShown) {
                        dialogShown = true;

                        new AlertDialog.Builder(this)
                                .setTitle("Deadline reached")
                                .setMessage("A task has reached its final date.")
                                .setPositiveButton("OK", (d,w) -> { dialogShown = false; })
                                .show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            handler.postDelayed(deadlineChecker, 30000);
        };

        handler.post(deadlineChecker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(deadlineChecker);
    }

}