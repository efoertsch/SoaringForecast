package com.fisincorporated.soaringforecast.task.edit;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.databinding.EditTaskView;
import com.fisincorporated.soaringforecast.messages.AddTurnpointsToTask;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.touchhelper.OnStartDragListener;
import com.fisincorporated.soaringforecast.touchhelper.SimpleItemTouchHelperCallback;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class EditTaskFragment extends DaggerFragment implements OnStartDragListener {

    @Inject
    AppRepository appRepository;

    private long taskId;

    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;
    private TaskTurnpointsRecyclerViewAdapter recyclerViewAdapter;
    private FloatingActionButton saveFab;
    private ItemTouchHelper itemTouchHelper;
    private EditTaskView editTaskView;

    public EditTaskFragment setTaskId(long taskId) {
        this.taskId = taskId;
        return this;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note viewmodel is shared by activity
        taskAndTurnpointsViewModel = ViewModelProviders.of(getActivity())
                .get(TaskAndTurnpointsViewModel.class)
                .setAppRepository(appRepository)
                .setTaskId(taskId);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        editTaskView = DataBindingUtil.inflate(inflater, R.layout.task_edit_layout, container, false);
        editTaskView.setLifecycleOwner(this); // update UI based on livedata changes.

        editTaskView.setTaskAndTurnpointsViewModel(taskAndTurnpointsViewModel);

        RecyclerView recyclerView = editTaskView.editTaskRecyclerView;
        recyclerViewAdapter = new TaskTurnpointsRecyclerViewAdapter(taskAndTurnpointsViewModel);

        //TODO DRY
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext()
                , linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(recyclerViewAdapter);

        taskAndTurnpointsViewModel.getTaskTurnpoints().observe(this, taskTurnpoints -> {
            recyclerViewAdapter.setItems(taskTurnpoints);
            recyclerViewAdapter.notifyDataSetChanged();
        });

        // Could not get taskDistance in xml to update with new distance automagically on task distance change
        // so set text this way
        taskAndTurnpointsViewModel.getTaskDistance().observe(this, taskDistance ->{
            editTaskView.editTaskDistance.setText(getString(R.string.distance_km,taskDistance));
        });


        //TODO DRY
        //DragListener
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(recyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        editTaskView.editTaskAddTurnpoints.setOnClickListener(v -> goToAddTaskTurnpoints());

        saveFab = editTaskView.editTaskSaveTask;
        saveFab.setOnClickListener(v -> taskAndTurnpointsViewModel.saveTask());

        return editTaskView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayTitle();
        // if just added all turnpoints via search, the task distance will not be updated by observer in createView
        // ( as this fragment paused while adding turnpoints, so make sure ui update occurs.
        editTaskView.editTaskDistance.setText(getString(R.string.distance_km,taskAndTurnpointsViewModel.getTaskDistance().getValue()));
    }

    private void displayTitle() {
        if (taskId == -1){
            getActivity().setTitle(R.string.add_task);
        } else {
            getActivity().setTitle(R.string.edit_task);
        }
    }



    private void goToAddTaskTurnpoints() {
        EventBus.getDefault().post(new AddTurnpointsToTask());
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
