package com.fisincorporated.soaringforecast.messages;

import com.fisincorporated.soaringforecast.repository.Task;

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
