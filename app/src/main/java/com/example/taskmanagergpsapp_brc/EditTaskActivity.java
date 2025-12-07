package com.example.taskmanagergpsapp_brc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.taskmanagergpsapp_brc.Data.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    EditText edtTitle, edtDesc;
    Spinner statusSpinner;
    Button btnSave;
    int position;

    ImageView img;
    byte[] selectedImageData;
    ActivityResultLauncher<String> imagePicker;
    TextView latT, lonT;
    TextView dat;


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
        img = findViewById(R.id.imgTaskImage);
        latT = findViewById(R.id.latText);
        lonT = findViewById(R.id.lonText);
        dat = findViewById(R.id.tvFinalDate);

        Intent i = getIntent();
        task = (Task) i.getSerializableExtra("task");
        edtTitle.setText(task.getTitle());
        edtDesc.setText(task.getDescription());
        if (task.imageData != null)
            img.setImageBitmap(BitmapFactory.decodeByteArray(task.imageData, 0, task.imageData.length));

        if (task.latitude != 0)
            latT.setText(Double.toString(task.latitude));
        if (task.longitude != 0)
            lonT.setText(Double.toString(task.longitude));
        dat.setText(task.getFinalDateTime());

        dat.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            try {
                c.setTime(sdf.parse(dat.getText().toString()));
            } catch (ParseException e) {

            }

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            DatePickerDialog datePicker = new DatePickerDialog(EditTaskActivity.this,
                    (view, y, m, d) -> {
                        TimePickerDialog timePicker = new TimePickerDialog(
                                EditTaskActivity.this,
                                (tp, h, min) -> {
                                    Calendar selected = Calendar.getInstance();
                                    selected.set(y, m, d, h, min);
                                    dat.setText(sdf.format(selected.getTime()));
                                },
                                hour, minute, true
                        );

                        timePicker.show();
                    },
                    year, month, day
                    );

            datePicker.show();
        });

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            byte[] data = readBytesFromUri(uri);
                            selectedImageData = data;
                            img.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
        );

        img.setOnClickListener(v -> imagePicker.launch("image/*"));

        latT.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:" + latT.getText().toString() + "," + lonT.getText().toString() + "?q=" + latT.getText().toString() + "," + lonT.getText().toString());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(mapIntent);
            } catch (ActivityNotFoundException e) {

                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(unrestrictedIntent);
            }
        });

        lonT.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:" + latT.getText().toString() + "," + lonT.getText().toString() + "?q=" + latT.getText().toString() + "," + lonT.getText().toString());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(mapIntent);
            } catch (ActivityNotFoundException e) {

                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(unrestrictedIntent);
            }
        });

        btnSave.setOnClickListener(v -> {
            task.setTitle(edtTitle.getText().toString());
            task.setDescription(edtDesc.getText().toString());
            task.setImageData(selectedImageData);
            task.setFinalDateTime(dat.getText().toString());
            Intent result = new Intent();
            result.putExtra("updatedTask", task);
            setResult(RESULT_OK, result);
            finish();
        });
    }
    private byte[] readBytesFromUri(Uri uri) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;

        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        input.close();
        return buffer.toByteArray();
    }

}