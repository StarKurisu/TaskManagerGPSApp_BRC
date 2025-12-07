package com.example.taskmanagergpsapp_brc.Data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
@Entity(tableName = "tasks")
public class Task implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String description = "";
    public Status status;

    public String img = "";
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] imageData;

    public double latitude;
    public double longitude;
    public String finalDateTime;

    public Task(int id, String title, String description, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(int id, String title, String description, Status status, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Task(String title, String description, Status status, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Task(String title, String description, Status status, double latitude, double longitude, String finalDateTime) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.finalDateTime = finalDateTime;
    }

    public Task() {

    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Status getStatus() {return status;}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Status status) { this.status = status;}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getFinalDateTime() {
        return finalDateTime;
    }

    public void setFinalDateTime(String finalDateTime) {
        this.finalDateTime = finalDateTime;
    }
}


