package com.fisincorporated.aviationweather.messages;


// User wants to add a new task
public final class AddNewTask {

    private long taskId;

    public AddNewTask(long taskId){
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }
}
