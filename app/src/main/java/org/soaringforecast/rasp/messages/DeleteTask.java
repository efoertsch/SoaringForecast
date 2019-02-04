package org.soaringforecast.rasp.messages;

import org.soaringforecast.rasp.repository.Task;

public class DeleteTask {
    private final Task task;

    public DeleteTask(Task task){
        this.task = task;
    }

    public Task getTask() {
        return task;
    }
}
