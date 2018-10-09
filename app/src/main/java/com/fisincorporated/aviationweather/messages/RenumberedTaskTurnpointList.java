package com.fisincorporated.aviationweather.messages;

import com.fisincorporated.aviationweather.repository.TaskTurnpoint;

import java.util.List;

public final class RenumberedTaskTurnpointList {

    private List<TaskTurnpoint> taskTurnpoints;

    public RenumberedTaskTurnpointList(List<TaskTurnpoint> taskTurnpoints){
        this.taskTurnpoints = taskTurnpoints;
    }

    public List<TaskTurnpoint> getTaskTurnpoints() {
        return taskTurnpoints;
    }
}
