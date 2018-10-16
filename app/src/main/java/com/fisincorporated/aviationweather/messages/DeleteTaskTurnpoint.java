package com.fisincorporated.aviationweather.messages;

import com.fisincorporated.aviationweather.repository.TaskTurnpoint;

public class DeleteTaskTurnpoint {
    private final TaskTurnpoint taskTurnpoint;

    public DeleteTaskTurnpoint(TaskTurnpoint taskTurnpoint){
        this.taskTurnpoint = taskTurnpoint;
    }

    public TaskTurnpoint getTaskTurnpoint() {
        return taskTurnpoint;
    }
}
