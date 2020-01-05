package org.soaringforecast.rasp.repository;


import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String taskName = "";

    //In Kilometers
    private float distance = 0;

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
