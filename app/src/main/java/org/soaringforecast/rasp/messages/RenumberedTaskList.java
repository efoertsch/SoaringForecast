package org.soaringforecast.rasp.messages;

import org.soaringforecast.rasp.repository.Task;

import java.util.List;

public final class RenumberedTaskList {
    private List<Task> taskList;

    public RenumberedTaskList(List<Task> taskList){
        this.taskList = taskList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }
}
