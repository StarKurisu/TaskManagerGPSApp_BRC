package com.example.taskmanagergpsapp_brc;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskmanagergpsapp_brc.Data.AppDatabase;
import com.example.taskmanagergpsapp_brc.Data.Status;
import com.example.taskmanagergpsapp_brc.Data.Task;
import com.example.taskmanagergpsapp_brc.Data.TaskDao;
import com.example.taskmanagergpsapp_brc.Data.TaskJsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final TaskDao taskDao;
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TaskViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        taskDao = db.taskDao();
        loadTasks();
    }

    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }


    private void loadTasks() {
        executor.execute(() -> {
            List<Task> list = taskDao.getAllTasks();
            tasksLiveData.postValue(list);
        });
    }


    public void addTask(Task task) {
        executor.execute(() -> {
            long id = taskDao.insert(task);
            task.id = id;
            loadTasks();
        });
    }


    public void updateTask(Task task) {
        executor.execute(() -> {
            taskDao.update(task);
            loadTasks();
        });
    }


    public void moveTask(long taskId, Status newStatus) {
        executor.execute(() -> {
            Task t = null;
            List<Task> allTasks = taskDao.getAllTasks();
            for (Task task : allTasks) {
                if (task.id == taskId) {
                    t = task;
                    break;
                }
            }
            if (t != null) {
                t.setStatus(newStatus);
                taskDao.update(t);
            }
            loadTasks();
        });
    }


    public void deleteTask(Task task) {
        executor.execute(() -> {
            taskDao.delete(task);
            loadTasks();
        });
    }
    public void deleteAllDoneTasks() {
        executor.execute(() -> {
            taskDao.deleteByStatus(Status.DONE);
        });
    }


    public void exportTasks(Context context, String filename) {
        executor.execute(() -> {
            List<Task> allTasks = taskDao.getAllTasks();
            TaskJsonHelper.exportTasksToJson(context, allTasks, filename);
        });
    }

    public void importTasks(Context context, String filename) {

        executor.execute(() -> {
            taskDao.deleteAll();
            List<Task> importedTasks = TaskJsonHelper.importTasksFromJson(context, filename);
            if (importedTasks != null) {
                for (Task t : importedTasks) {
                    taskDao.insert(t);
                }
                loadTasks();
            }
        });
    }
}