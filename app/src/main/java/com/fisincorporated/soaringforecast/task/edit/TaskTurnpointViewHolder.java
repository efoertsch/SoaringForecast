package com.fisincorporated.soaringforecast.task.edit;

import android.graphics.Color;

import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.TaskTurnpointView;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.touchhelper.ItemTouchHelperViewHolder;

public class TaskTurnpointViewHolder extends GenericViewHolder<TaskTurnpoint, TaskTurnpointView>
        implements ItemTouchHelperViewHolder {

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

    @Override
    public void onItemSelected() {
        itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear() {
        itemView.setBackgroundColor(0);
    }

}
