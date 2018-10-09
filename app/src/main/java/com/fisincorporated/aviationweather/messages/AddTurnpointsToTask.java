package com.fisincorporated.aviationweather.messages;

public class AddTurnpointsToTask {
    private long taskId;

    public AddTurnpointsToTask(long taskId){
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }
}
