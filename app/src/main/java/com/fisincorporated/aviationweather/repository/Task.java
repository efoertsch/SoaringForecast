package com.fisincorporated.aviationweather.repository;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    private String taskName = "";

    public Task(String taskName){
        this.taskName = taskName;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(@NonNull String taskName) {
        this.taskName = taskName;
    }
}
