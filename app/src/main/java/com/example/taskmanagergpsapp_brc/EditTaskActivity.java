package com.example.taskmanagergpsapp_brc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.taskmanagergpsapp_brc.Data.Task;

public class EditTaskActivity extends AppCompatActivity {

    EditText edtTitle, edtDesc;
    Spinner statusSpinner;
    Button btnSave;
    int position;

    private Task task;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtTitle = findViewById(R.id.edtTitle);
        edtDesc = findViewById(R.id.edtDescription);
        statusSpinner = findViewById(R.id.spinnerStatus);
        btnSave = findViewById(R.id.btnSave);

        Intent i = getIntent();
        task = (Task) i.getSerializableExtra("task");
        edtTitle.setText(task.getTitle());
        edtDesc.setText(task.getDescription());
        //set spinner to match status value

        btnSave.setOnClickListener(v -> {
            task.setTitle(edtTitle.getText().toString());
            task.setDescription(edtDesc.getText().toString());

            Intent result = new Intent();
            result.putExtra("updatedTask", task);
            setResult(RESULT_OK, result);
            finish();
        });
    }
}