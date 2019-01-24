package com.fisincorporated.soaringforecast.task.list;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericEditClickListener;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericListClickListener;
import com.fisincorporated.soaringforecast.databinding.TaskListView;
import com.fisincorporated.soaringforecast.messages.DeleteTask;
import com.fisincorporated.soaringforecast.messages.EditTask;
import com.fisincorporated.soaringforecast.messages.RenumberedTaskList;
import com.fisincorporated.soaringforecast.messages.SelectedTask;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.repository.Task;
import com.fisincorporated.soaringforecast.touchhelper.OnStartDragListener;
import com.fisincorporated.soaringforecast.touchhelper.SimpleItemTouchHelperCallback;
import com.fisincorporated.soaringforecast.utils.ViewUtilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TaskListFragment extends DaggerFragment implements GenericListClickListener<Task>, GenericEditClickListener<Task>, OnStartDragListener {

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    private List<Task> tasks = new ArrayList<>();
    private TaskListRecyclerViewAdapter recyclerViewAdapter;
    private ItemTouchHelper itemTouchHelper;
    private TaskListViewModel taskListViewModel;
    private ProgressBar progressBar;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskListViewModel = ViewModelProviders.of(this).get(TaskListViewModel.class).setAppRepository(appRepository);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TaskListView taskListView = DataBindingUtil.inflate(inflater,R.layout.task_list_layout, container,false);

        RecyclerView recyclerView = taskListView.taskListRecyclerView;
        recyclerViewAdapter = new TaskListRecyclerViewAdapter(tasks)
                .setItemClickListener(this)
                .setEditItemClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        ViewUtilities.addRecyclerViewDivider(getContext(), linearLayoutManager.getOrientation(), recyclerView);
        recyclerView.setAdapter(recyclerViewAdapter);

        //DragListener
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(recyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        FloatingActionButton fab = taskListView.taskListAddButton;
        fab.setOnClickListener(v ->
                EventBus.getDefault().post(new EditTask(0)));

        progressBar = taskListView.taskListProgressBar;

        return taskListView.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.task_list);
        refreshTaskList();
    }

    @Override
    public void onPause() {
        super.onPause();
       setProgressBarVisibility(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        compositeDisposable.dispose();
    }

    private void refreshTaskList() {
        setProgressBarVisibility(true);
        taskListViewModel.listTasks().observe(this, taskList -> {
            recyclerViewAdapter.updateTaskList(taskList);
            setProgressBarVisibility(false);
        });
    }

    private void setProgressBarVisibility(boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onItemClick(Task task, int position) {
        //TODO best way to do this?
        appPreferences.setSelectedTaskId(task.getId());
        EventBus.getDefault().post(new SelectedTask(task.getId()));
    }

    @Override
    public void onEditItemClick(Task task, int position) {
        EventBus.getDefault().post(new EditTask(task.getId()));
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeleteTask deleteTask){
       Completable completable = appRepository.deleteTask(deleteTask.getTask());
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

}
