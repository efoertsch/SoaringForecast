package com.fisincorporated.aviationweather.task.edit;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.GenericFabClickListener;
import com.fisincorporated.aviationweather.databinding.EditTaskView;
import com.fisincorporated.aviationweather.messages.AddTurnpointsToTask;
import com.fisincorporated.aviationweather.messages.AddTurnpointsToTaskRefused;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Task;
import com.fisincorporated.aviationweather.touchhelper.OnStartDragListener;
import com.fisincorporated.aviationweather.touchhelper.SimpleItemTouchHelperCallback;

import org.greenrobot.eventbus.EventBus;

public class EditTaskFragment extends Fragment implements GenericFabClickListener<Task>, OnStartDragListener {

    private static final String TASK_ID = "TASK_ID";

    private AppRepository appRepository;
    private long taskId;

    private TaskTurnpointsRecyclerViewAdapter recyclerViewAdapter;
    private EditTaskViewModel editTaskViewModel;
    private ItemTouchHelper itemTouchHelper;

    private FloatingActionButton saveFab;
    private boolean showSaveFab = false;

    public static EditTaskFragment newInstance(AppRepository appRepository, long taskId) {
        EditTaskFragment editTaskFragment = new EditTaskFragment();
        editTaskFragment.appRepository = appRepository;
        editTaskFragment.taskId = taskId;
        return editTaskFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        EditTaskView editTaskView = DataBindingUtil.inflate(inflater, R.layout.edit_task_layout, container, false);

        editTaskViewModel = ViewModelProviders.of(getActivity()).get(EditTaskViewModel.class).setAppRepository(appRepository).setTaskId(taskId);
        editTaskView.setEditTaskViewModel(editTaskViewModel);

        RecyclerView recyclerView = editTaskView.editTaskRecyclerView;
        recyclerViewAdapter = new TaskTurnpointsRecyclerViewAdapter(editTaskViewModel);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext()
                , linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(recyclerViewAdapter);

        //DragListener
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(recyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        saveFab = editTaskView.editTaskSaveTask;
        saveFab.setVisibility(View.GONE);
        saveFab.setOnClickListener(v -> editTaskViewModel.saveTask());

        return editTaskView.getRoot();
    }


    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.create_task);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        editTaskViewModel.getTaskTurnpoints();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void displaySaveFab(boolean b) {
        //TODO implement
    }


    @Override
    public void onFabItemClick(Task task) {
        goToAddTaskTurnpoints(task.getId());
    }


    private void displayAddTurnpointsDialog(long taskId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.add_turnpoints_to_task)
                .setTitle(R.string.no_task_turnpoints_found)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    goToAddTaskTurnpoints(taskId);
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    doNotAddTaskTurnpoints();
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void goToAddTaskTurnpoints(long taskId) {
        EventBus.getDefault().post(new AddTurnpointsToTask());
    }

    private void doNotAddTaskTurnpoints() {
        EventBus.getDefault().post(new AddTurnpointsToTaskRefused());
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
