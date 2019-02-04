package org.soaringforecast.rasp.messages;


// User wants to edit/add a new task
public final class EditTask {

    private long taskId;

    public EditTask(long taskId){
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }
}
