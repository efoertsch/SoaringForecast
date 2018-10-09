package com.fisincorporated.aviationweather.messages;

import com.fisincorporated.aviationweather.repository.Task;

import java.util.List;

public class RenumberedTaskList {
    private List<Task> taskList;

    public RenumberedTaskList(List<Task> taskList){
        this.taskList = taskList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }
}
