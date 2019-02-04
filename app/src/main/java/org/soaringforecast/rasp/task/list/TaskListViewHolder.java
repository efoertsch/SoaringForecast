package org.soaringforecast.rasp.task.list;


import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericViewHolder;
import org.soaringforecast.rasp.databinding.TaskView;
import org.soaringforecast.rasp.repository.Task;
import org.soaringforecast.rasp.touchhelper.ItemTouchHelperViewHolder;

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
