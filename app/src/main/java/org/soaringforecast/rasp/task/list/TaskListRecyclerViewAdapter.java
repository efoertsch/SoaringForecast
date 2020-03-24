package org.soaringforecast.rasp.task.list;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericEditClickListener;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.databinding.TaskView;
import org.soaringforecast.rasp.repository.Task;
import org.soaringforecast.rasp.task.messages.DeleteTask;
import org.soaringforecast.rasp.task.messages.RenumberedTaskList;
import org.soaringforecast.rasp.touchhelper.ItemTouchHelperAdapter;

import java.util.Collections;
import java.util.List;

import androidx.databinding.DataBindingUtil;

public class TaskListRecyclerViewAdapter
        extends GenericRecyclerViewAdapter<Task, TaskListViewHolder>
        implements ItemTouchHelperAdapter {

    private GenericListClickListener<Task> itemClickListener;
    private GenericEditClickListener<Task> editClickListener;

    public TaskListRecyclerViewAdapter(List<Task> items) {
        super(items);
    }

    public TaskListRecyclerViewAdapter setEditItemClickListener(GenericEditClickListener<Task> genericEditClickListener) {
        this.editClickListener = genericEditClickListener;
        return this;

    }

    public TaskListRecyclerViewAdapter setItemClickListener(GenericListClickListener<Task> genericListClickListener) {
        this.itemClickListener = genericListClickListener;
        return this;
    }

    @Override
    public TaskListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TaskView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.task_detail, parent, false);
        return new TaskListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TaskListViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (itemClickListener != null) {
            holder.getViewDataBinding().setClickListener(itemClickListener);
        }
        if (editClickListener != null) {
            holder.getViewDataBinding().setEditClickListener(editClickListener);
        }
    }

    public void updateTaskList(List<Task> Tasks) {
        getItems().clear();
        getItems().addAll(Tasks);
        notifyDataSetChanged();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(getItems(), fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        renumberTaskOrder();
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        post(new DeleteTask(getItems().get(position)));
        getItems().remove(position);
        notifyItemRemoved(position);
        renumberTaskOrder();
    }

    private void renumberTaskOrder() {
        int i = 0;
        for (Task task : getItems()) {
            task.setTaskOrder(i++);
        }
        post(new RenumberedTaskList(getItems()));
    }

    private void post(Object object){
        EventBus.getDefault().post(object);
    }

}
