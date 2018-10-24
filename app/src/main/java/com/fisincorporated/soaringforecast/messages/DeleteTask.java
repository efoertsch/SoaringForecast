package com.fisincorporated.soaringforecast.messages;

import com.fisincorporated.soaringforecast.repository.Task;

public class DeleteTask {
    private final Task task;

    public DeleteTask(Task task){
        this.task = task;
    }

    public Task getTask() {
        return task;
    }
}
