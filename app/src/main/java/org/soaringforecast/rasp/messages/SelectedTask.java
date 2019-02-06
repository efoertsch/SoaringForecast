package org.soaringforecast.rasp.messages;

public class SelectedTask {

    private long taskId;

    public SelectedTask(long taskId){
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }
}
