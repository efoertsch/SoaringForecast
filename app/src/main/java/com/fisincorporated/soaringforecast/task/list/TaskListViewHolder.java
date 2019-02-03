package com.fisincorporated.soaringforecast.task.list;


import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.TaskView;
import com.fisincorporated.soaringforecast.repository.Task;
import com.fisincorporated.soaringforecast.touchhelper.ItemTouchHelperViewHolder;

public class TaskListViewHolder extends GenericViewHolder<Task, TaskView> implements ItemTouchHelperViewHolder {

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

    @Override
    public void onItemSelected() {
       itemView.setBackgroundColor(itemView.getResources().getColor(R.color.drag_color));
    }

    @Override
    public void onItemClear() {
       itemView.setBackgroundColor(itemView.getResources().getColor(R.color.drag_drop));
    }

}
