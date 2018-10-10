package com.fisincorporated.aviationweather.messages;

import com.fisincorporated.aviationweather.repository.Task;

public class DeleteTask {
    private final Task task;

    public DeleteTask(Task task){
        this.task = task;
    }

    public Task getTask() {
        return task;
    }
}
