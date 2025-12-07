package com.example.taskmanagergpsapp_brc.Data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(Task task);
    @Update
    void update(Task task);
    @Delete
    void delete(Task task);
    @Query("DELETE FROM tasks")
    void deleteAll();
    @Query("DELETE FROM tasks WHERE status = :status")
    void deleteByStatus(Status status);

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();
    @Query("SELECT * FROM tasks WHERE status = :status")
    List<Task> getTasksByStatus(Status status);

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskById(long id);
}
