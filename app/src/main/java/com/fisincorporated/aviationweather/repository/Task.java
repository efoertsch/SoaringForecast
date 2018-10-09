package com.fisincorporated.aviationweather.repository;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String taskName = "";

    //In Kilometers
    private int distance;

    private int taskOrder;

    public Task(String taskName){
        this.taskName = taskName;
    }

    public void setId(int id) {
        this.id = id;
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

    /**
     *
     * @return task distance in kilometers
     */
    public int getDistance() {
        return distance;
    }

    /**
     *
     * @param distance in Kilometers
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(int taskOrder) {
        this.taskOrder = taskOrder;
    }
}
