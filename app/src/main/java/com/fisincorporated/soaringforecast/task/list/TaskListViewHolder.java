package com.fisincorporated.soaringforecast.task.list;


import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.TaskView;
import com.fisincorporated.soaringforecast.repository.Task;

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
