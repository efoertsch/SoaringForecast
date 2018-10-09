package com.fisincorporated.aviationweather.task.list;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericListClickListener;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.aviationweather.databinding.TaskView;
import com.fisincorporated.aviationweather.repository.Task;
import com.fisincorporated.aviationweather.touchhelper.ItemTouchHelperAdapter;
import com.fisincorporated.aviationweather.touchhelper.OnStartDragListener;

import java.util.Collections;
import java.util.List;

public class TaskListRecyclerViewAdapter extends GenericRecyclerViewAdapter<Task,TaskListViewHolder>
        implements ItemTouchHelperAdapter {

    private GenericListClickListener<Task> itemClickListener;
    private OnStartDragListener dragStartListener;


    public TaskListRecyclerViewAdapter(List<Task> items){
        super(items);
    }


    public void setOnItemClickListener(GenericListClickListener<Task> genericListClickListener ) {
        this.itemClickListener =  genericListClickListener;

    }

    public TaskListRecyclerViewAdapter setOnStartDragListener(OnStartDragListener dragStartListener) {
        this.dragStartListener = dragStartListener;
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
        //TODO get better way to do following
        holder.getViewDataBinding().setClickListener(itemClickListener);
    }

    public void updateTaskList(List<Task> Tasks){
        getItems().clear();
        getItems().addAll(Tasks);
        notifyDataSetChanged();
    }


    public void onTaskClick(Task task, Integer position){
        // TODO edit task (task name or add/delete/change task turnpoints
       // EventBus.getDefault().post(new ImportFile(file));

    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(getItems(), fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        getItems().remove(position);
        notifyItemRemoved(position);
    }
}
