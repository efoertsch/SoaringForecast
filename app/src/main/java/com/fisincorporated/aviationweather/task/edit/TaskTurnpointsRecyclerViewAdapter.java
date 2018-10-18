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

public class TaskTurnpointsRecyclerViewAdapter
        extends GenericRecyclerViewAdapter<TaskTurnpoint, TaskTurnpointViewHolder>
        implements ItemTouchHelperAdapter {

    private GenericListClickListener<TaskTurnpoint> itemClickListener;
    private EditTaskViewModel editTaskViewModel;

    public TaskTurnpointsRecyclerViewAdapter(EditTaskViewModel editTaskViewModel){
        super(editTaskViewModel.getTaskTurnpoints().getValue());
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


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        editTaskViewModel.swapTurnpoints(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
       editTaskViewModel.deleteTaskTurnpoint(position);
        getItems().remove(position);
        notifyItemRemoved(position);
    }



}
