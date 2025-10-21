package com.example.taskmanagergpsapp_brc;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskmanagergpsapp_brc.Data.Status;
import com.example.taskmanagergpsapp_brc.Data.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskViewModel extends ViewModel {
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        List<Task> current = new ArrayList<>(tasks.getValue());
        current.add(task);
        tasks.setValue(current);
    }
    public void moveTask(int id, Status newStatus) {
        List<Task> current = new ArrayList<>(tasks.getValue());
        for (int i = 0; i < current.size(); i++) {
            Task t = current.get(i);
            if (t.getId() == id) {
                t.setStatus(newStatus);
                break;
            }
        }
        tasks.setValue(current);
    }
}
