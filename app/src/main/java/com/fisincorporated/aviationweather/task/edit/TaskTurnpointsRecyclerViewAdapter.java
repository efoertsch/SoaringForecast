package com.fisincorporated.aviationweather.task.edit;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericListClickListener;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.aviationweather.databinding.TaskTurnpointView;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;
import com.fisincorporated.aviationweather.touchhelper.ItemTouchHelperAdapter;
import com.fisincorporated.aviationweather.touchhelper.OnStartDragListener;

import java.util.Collections;
import java.util.List;

public class TaskTurnpointsRecyclerViewAdapter
        extends GenericRecyclerViewAdapter<TaskTurnpoint, TaskTurnpointViewHolder>
        implements ItemTouchHelperAdapter {

    private GenericListClickListener<TaskTurnpoint> itemClickListener;
    private OnStartDragListener dragStartListener;


    public TaskTurnpointsRecyclerViewAdapter(List<TaskTurnpoint> items){
        super(items);
    }


    public void setOnItemClickListener(GenericListClickListener<TaskTurnpoint> genericListClickListener ) {
        this.itemClickListener =  genericListClickListener;

    }

    public TaskTurnpointsRecyclerViewAdapter setOnStartDragListener(OnStartDragListener dragStartListener) {
        this.dragStartListener = dragStartListener;
        return this;
    }

    @Override
    public TaskTurnpointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TaskTurnpointView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.task_turnpoint_detail, parent, false);
        return new TaskTurnpointViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TaskTurnpointViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        //TODO get better way to do following
        holder.getViewDataBinding().setClickListener(itemClickListener);
    }

    public void updateTaskTurpointList(List<TaskTurnpoint> taskTurnpoints){
        getItems().clear();
        getItems().addAll(taskTurnpoints);
        notifyDataSetChanged();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(getItems(), fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        renumberTurnpoints();
        //TODO implement reorder of turnpoints in task
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        getItems().remove(position);
        notifyItemRemoved(position);
        renumberTurnpoints();
    }

    private void renumberTurnpoints(){
        int i = 0;
        for (TaskTurnpoint taskTurnpoint: getItems()){
            taskTurnpoint.setTaskOrder(i++);
        }
    }


}
