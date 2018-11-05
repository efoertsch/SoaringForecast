package com.fisincorporated.soaringforecast.repository;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String taskName = "";

    //In Kilometers
    private float distance;

    private int taskOrder;

    public Task(){ }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
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
    public float getDistance() {
        return distance;
    }

    /**
     *
     * @param distance in Kilometers
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(int taskOrder) {
        this.taskOrder = taskOrder;
    }
}
