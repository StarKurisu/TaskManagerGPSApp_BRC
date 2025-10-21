package com.example.taskmanagergpsapp_brc.Data;

public class Task {
    int id;
    String title;
    String description = "";
    Status status;

    public Task(int id, String title, String description, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Status getStatus() {return status;}
    public void setStatus(Status status) { this.status = status;}
}


