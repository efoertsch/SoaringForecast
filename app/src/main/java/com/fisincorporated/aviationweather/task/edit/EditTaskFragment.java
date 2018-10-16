package com.fisincorporated.aviationweather.task.edit;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.fisincorporated.aviationweather.messages.DeleteTaskTurnpoint;
import com.fisincorporated.aviationweather.messages.RenumberedTaskList;
import com.fisincorporated.aviationweather.messages.RenumberedTaskTurnpointList;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Task;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;
import com.fisincorporated.aviationweather.touchhelper.OnStartDragListener;
import com.fisincorporated.aviationweather.touchhelper.SimpleItemTouchHelperCallback;

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

public class EditTaskFragment extends Fragment implements GenericFabClickListener<Task>, OnStartDragListener {

    private static final String TASK_ID = "TASK_ID";

    private AppRepository appRepository;
    private long taskId;

    private List<TaskTurnpoint> taskTurnpoints = new ArrayList<>();
    private TaskTurnpointsRecyclerViewAdapter recyclerViewAdapter;
    private EditTaskViewModel editTaskViewModel;
    private boolean firstTimeCheck;
    private ItemTouchHelper itemTouchHelper;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private int maxTurnpointOrderNumber = 0;

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

        editTaskViewModel = ViewModelProviders.of(this).get(EditTaskViewModel.class).setAppRepository(appRepository);

        editTaskView.setEditTaskViewModel(editTaskViewModel);

        RecyclerView recyclerView = editTaskView.editTaskRecyclerView;
        recyclerViewAdapter = new TaskTurnpointsRecyclerViewAdapter(taskTurnpoints);
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

        editTaskView.setFabClickListener(this);

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
        editTaskViewModel.getTask(taskId);
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
        setTurnpointOrder(taskTurnpointlist);
    }

    private void setTurnpointOrder(List<TaskTurnpoint> taskTurnpointlist) {
        int size = taskTurnpointlist.size();
        if (size == 0){
        maxTurnpointOrderNumber = 0;}
        else {
            maxTurnpointOrderNumber = taskTurnpointlist.get(size - 1).getTaskOrder();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeleteTaskTurnpoint deleteTaskTurnpoint) {
        Completable completable = appRepository.deleteTaskTurnpoint(deleteTaskTurnpoint.getTaskTurnpoint());
        Disposable disposable = completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    //complete
                }, throwable -> {
                    // TODO Display some error
                });
        compositeDisposable.add(disposable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RenumberedTaskList renumberedTaskList) {
        Completable completable = appRepository.updateTaskListOrder(renumberedTaskList.getTaskList());
        Disposable disposable = completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    //complete
                }, throwable -> {
                    // TODO Display some error

                });
        compositeDisposable.add(disposable);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RenumberedTaskTurnpointList renumberedTaskTurnpointList) {
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
        EventBus.getDefault().post(new AddTurnpointsToTask(taskId, maxTurnpointOrderNumber));
    }

    private void doNotAddTaskTurnpoints() {
        EventBus.getDefault().post(new AddTurnpointsToTaskRefused());
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
