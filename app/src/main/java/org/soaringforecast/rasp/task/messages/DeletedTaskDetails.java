package org.soaringforecast.rasp.task.messages;

import org.soaringforecast.rasp.repository.Task;
import org.soaringforecast.rasp.repository.TaskTurnpoint;

import java.util.List;

public class DeletedTaskDetails {
    private  final Task task;
    private  List<TaskTurnpoint> taskTurnpoints;

    public DeletedTaskDetails(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public void setTaskTurnpoints(List<TaskTurnpoint> taskTurnpoints) {
        this.taskTurnpoints = taskTurnpoints;
    }

    public List<TaskTurnpoint> getTaskTurnpoints() {
        return taskTurnpoints;
    }

}
