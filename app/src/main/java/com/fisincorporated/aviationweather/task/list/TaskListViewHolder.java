package com.fisincorporated.aviationweather.task.list;


import com.fisincorporated.aviationweather.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.aviationweather.databinding.TaskView;
import com.fisincorporated.aviationweather.repository.Task;

public class TaskListViewHolder extends GenericViewHolder<Task, TaskView> {

    private TaskView viewDataBinding;

    TaskListViewHolder(TaskView bindingView) {
        super(bindingView);
        viewDataBinding = bindingView;
    }

    public void onBind(Task item, int position) {
        viewDataBinding.setTask(item);
        viewDataBinding.setPosition(position);
    }

    @Override
    public TaskView getViewDataBinding() {
        return viewDataBinding;
    }

}
