package com.example.taskmanagergpsapp_brc.Data;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class TaskJsonHelper {

    public static void exportTasksToJson(Context context, List<Task> tasks, String filename) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(tasks);

        File file = new File(context.getFilesDir(), filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
            Log.d("Export", "Tasks exported to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Task> importTasksFromJson(Context context, String filename) {
        File file = new File(context.getFilesDir(), filename);
        if (!file.exists()) return null;

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Task>>(){}.getType();
            List<Task> tasks = gson.fromJson(reader, listType);
            Log.d("Import", "Tasks imported from " + file.getAbsolutePath());
            return tasks;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}