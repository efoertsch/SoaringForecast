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
import com.fisincorporated.aviationweather.messages.RenumberedTaskTurnpointList;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class EditTaskFragment extends Fragment {

    private static final String TASK_ID = "TASK_ID";

    private AppRepository appRepository;
    private long taskId;

    private List<TaskTurnpoint> taskTurnpoints = new ArrayList<>();
    private TaskTurnpointsRecyclerViewAdapter recyclerViewAdapter;
    private EditTaskViewModel editTaskViewModel;
    private boolean firstTimeCheck ;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public static EditTaskFragment newInstance(AppRepository appRepository, long taskId) {
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
        getActivity().setTitle(R.string.create_task);
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
        compositeDisposable.dispose();
    }

    private void refreshTaskTurnpointList() {
        firstTimeCheck = true;
        editTaskViewModel.listTaskTurnpoints(taskId)
                .observe(this, taskTurnpointlist ->
                {
                    determineAddTurnpointsDisplay(taskTurnpointlist);
                });
    }

    private void determineAddTurnpointsDisplay(List<TaskTurnpoint> taskTurnpointlist) {
        if (taskTurnpointlist.size() == 0) {
                if (!firstTimeCheck) {
                    displayAddTurnpointsDialog(taskId);
                }
                firstTimeCheck = false;
            } else {
                recyclerViewAdapter.updateTaskTurpointList(taskTurnpointlist);
            }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RenumberedTaskTurnpointList  renumberedTaskTurnpointList) {
        Completable completable = appRepository.updateTaskTurnpointOrder(renumberedTaskTurnpointList.getTaskTurnpoints());
        Disposable disposable = completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    //complete
                }, throwable -> {
                    // TODO Display some error

                });
        compositeDisposable.add(disposable);
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
        EventBus.getDefault().post(new AddTurnpointsToTask(taskId));
    }

    private void doNotAddTaskTurnpoints() {
        EventBus.getDefault().post(new AddTurnpointsToTaskRefused());
    }

//    public static class BundleBuilder {
//
//        private Bundle bundle;
//
//        private BundleBuilder() {
//            bundle = new Bundle();
//        }
//
//        public static EditTaskFragment.BundleBuilder getBundlerBuilder() {
//            EditTaskFragment.BundleBuilder builder = new EditTaskFragment.BundleBuilder();
//            return builder;
//        }
//
//        public BundleBuilder setTaskId(long taskId) {
//            bundle.putLong(TASK_ID, taskId);
//            return this;
//        }
//
//        public void assign(Fragment fragment) {
//            fragment.setArguments(bundle);
//        }
//
//    }

}
