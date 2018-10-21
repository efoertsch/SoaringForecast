package com.fisincorporated.aviationweather.task.edit;

import com.fisincorporated.aviationweather.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.aviationweather.databinding.TaskTurnpointView;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;

public class TaskTurnpointViewHolder extends GenericViewHolder<TaskTurnpoint, TaskTurnpointView> {

    private TaskTurnpointView viewDataBinding;

    TaskTurnpointViewHolder(TaskTurnpointView bindingView) {
        super(bindingView);
        viewDataBinding = bindingView;
    }

    public void onBind(TaskTurnpoint item, int position) {
        viewDataBinding.setTaskTurnpoint(item);
        viewDataBinding.setPosition(position);
    }

    @Override
    public TaskTurnpointView getViewDataBinding() {
        return viewDataBinding;
    }

}
