package org.soaringforecast.rasp.repository;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import static androidx.room.ForeignKey.CASCADE;

@Entity (foreignKeys = @ForeignKey(entity = Task.class
        , parentColumns = "id", childColumns = "taskId", onDelete = CASCADE)
        ,indices = @Index(value = {"taskId"}))
public class TaskTurnpoint {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private long taskId;

    // Order in which the turnpoint is in the task
    @NonNull
    private int taskOrder;

    // Following 2 fields refer back to same fields as in Turnpoint
    // Doing this rather than turnpoint.id due to  OnConflictStrategy.REPLACE stategy
    // used for any turnpoint updates.
    @NonNull
    private String title = "";

    @NonNull
    private String code = "";

    // For convenience store lat/long so can recalc distances w/o having to get it from turnpoint table
    // Also in case turnpoints deleted still can plot task
    private float latitudeDeg;

    private float longitudeDeg;

    private float distanceFromPriorTurnpoint;

    private float distanceFromStartingPoint;

    // used for taskTurnpoint display
    private boolean lastTurnpoint;

    public TaskTurnpoint(long taskId, String title, String code, float latitudeDeg, float longitudeDeg) {
        this.taskId = taskId;
        this.title = title;
        this.code = code;
        this.latitudeDeg = latitudeDeg;
        this.longitudeDeg = longitudeDeg;

    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @NonNull
    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(@NonNull long taskId) {
        this.taskId = taskId;
    }

    @NonNull
    public int getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(@NonNull int taskOrder) {
        this.taskOrder = taskOrder;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    public void setCode(@NonNull String code) {
        this.code = code;
    }

    public float getLatitudeDeg() {
        return latitudeDeg;
    }

    public void setLatitudeDeg(float latitudeDeg) {
        this.latitudeDeg = latitudeDeg;
    }

    public float getLongitudeDeg() {
        return longitudeDeg;
    }

    public void setLongitudeDeg(float longitudeDeg) {
        this.longitudeDeg = longitudeDeg;
    }


    public float getDistanceFromPriorTurnpoint() {
        return distanceFromPriorTurnpoint;
    }

    public void setDistanceFromPriorTurnpoint(float distanceFromPriorTurnpoint) {
        this.distanceFromPriorTurnpoint = distanceFromPriorTurnpoint;
    }

    public float getDistanceFromStartingPoint() {
        return distanceFromStartingPoint;
    }

    public void setDistanceFromStartingPoint(float distanceFromStartingPoint) {
        this.distanceFromStartingPoint = distanceFromStartingPoint;
    }

    public boolean isLastTurnpoint() {
        return lastTurnpoint;
    }

    public void setLastTurnpoint(boolean lastTurnpoint) {
        this.lastTurnpoint = lastTurnpoint;
    }

}
