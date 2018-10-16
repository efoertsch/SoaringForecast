package com.fisincorporated.aviationweather.messages;

public final class AddTurnpointsToTask {
    private long taskId;
    private int maxTurnpointOrderNumber;

    public AddTurnpointsToTask(long taskId, int maxTurnpointOrderNumber){
        this.taskId = taskId;
        this.maxTurnpointOrderNumber = maxTurnpointOrderNumber;
    }

    public long getTaskId() {
        return taskId;
    }

    public int getMaxTurnpointOrderNumber() {
        return maxTurnpointOrderNumber;
    }
}
