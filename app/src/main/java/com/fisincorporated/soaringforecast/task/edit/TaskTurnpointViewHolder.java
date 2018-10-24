package com.fisincorporated.soaringforecast.task.edit;

import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.TaskTurnpointView;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;

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
