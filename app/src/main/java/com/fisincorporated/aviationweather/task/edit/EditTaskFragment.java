package com.fisincorporated.aviationweather.task.edit;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.messages.AddTurnpointsToTask;
import com.fisincorporated.aviationweather.messages.AddTurnpointsToTaskRefused;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import dagger.android.support.DaggerFragment;

public class EditTaskFragment extends DaggerFragment {

    private static final String TASK_ID = "TASK_ID";

    private AppRepository appRepository;
    private long taskId;

    private List<TaskTurnpoint> taskTurnpoints;
    private TaskTurnpointsRecyclerViewAdapter recyclerViewAdapter;
    private EditTaskViewModel editTaskViewModel;

    public EditTaskFragment() {
    }

    public static EditTaskFragment newInstance(AppRepository appRepository, long taskId){
        EditTaskFragment editTaskFragment = new EditTaskFragment();
        editTaskFragment.appRepository = appRepository;
        editTaskFragment.taskId = taskId;
        return editTaskFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_task_layout, container, false);

        editTaskViewModel = ViewModelProviders.of(this).get(EditTaskViewModel.class).setAppRepository(appRepository);


        RecyclerView recyclerView = view.findViewById(R.id.edit_task_recycler_view);
        recyclerViewAdapter = new TaskTurnpointsRecyclerViewAdapter(taskTurnpoints);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext()
                , linearLayoutManager.getOrientation());

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(recyclerViewAdapter);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO refresh file list
        refreshTaskTurnpointList();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void refreshTaskTurnpointList() {
         final long taskId = getArguments().getLong(TASK_ID, -1);
        editTaskViewModel.listTaskTurnpoints(taskId)
                .observe(this, taskTurnpointlist ->
                {if (taskTurnpointlist.size() == 0) {
                    displayAddTurnpointsDialog(taskId);
                } else {
                        recyclerViewAdapter.updateTaskTurpointList(taskTurnpointlist);
                }});
    }

    private void displayAddTurnpointsDialog(long taskId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.add_turnpoints_to_task)
                .setTitle(R.string.no_task_turnpoints_found)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    dialog.dismiss();
                    goToAddTaskTurnpoints(taskId);
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    dialog.dismiss();
                    doNotAddTaskTurnpoints();
                });
        builder.create().show();
    }

    private void goToAddTaskTurnpoints(long taskId) {
        EventBus.getDefault().post(new AddTurnpointsToTask(taskId));
    }

    private void doNotAddTaskTurnpoints() {
        EventBus.getDefault().post(new AddTurnpointsToTaskRefused());
    }

    public static class BundleBuilder {

        private Bundle bundle;

        private BundleBuilder() {
            bundle = new Bundle();
        }

        public static EditTaskFragment.BundleBuilder getBundlerBuilder() {
            EditTaskFragment.BundleBuilder builder = new EditTaskFragment.BundleBuilder();
            return builder;
        }

        public BundleBuilder setTaskId(long taskId){
            bundle.putLong(TASK_ID, taskId);
            return this;
        }

        public void assign(Fragment fragment){
            fragment.setArguments(bundle);
        }

    }

}
