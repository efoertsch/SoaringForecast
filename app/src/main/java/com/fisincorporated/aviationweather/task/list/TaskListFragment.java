package com.fisincorporated.aviationweather.task.list;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericListClickListener;
import com.fisincorporated.aviationweather.messages.AddNewTask;
import com.fisincorporated.aviationweather.messages.AddNewTaskRefused;
import com.fisincorporated.aviationweather.messages.RenumberedTaskList;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Task;
import com.fisincorporated.aviationweather.touchhelper.OnStartDragListener;
import com.fisincorporated.aviationweather.touchhelper.SimpleItemTouchHelperCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class TaskListFragment extends Fragment implements GenericListClickListener<Task>, OnStartDragListener {

    @Inject
    AppRepository appRepository;

    private List<Task> tasks = new ArrayList<>();

    private TaskListRecyclerViewAdapter recyclerViewAdapter;
    private ItemTouchHelper itemTouchHelper;

    private TaskListViewModel taskListViewModel;
    private ProgressBar progressBar;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public static TaskListFragment newInstance(AppRepository appRepository) {
        TaskListFragment taskListFragment = new TaskListFragment();
        taskListFragment.appRepository = appRepository;
        return taskListFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_list_layout, container, false);

        taskListViewModel = ViewModelProviders.of(this).get(TaskListViewModel.class).setAppRepository(appRepository);

        RecyclerView recyclerView = view.findViewById(R.id.task_list_recycler_view);
        recyclerViewAdapter = new TaskListRecyclerViewAdapter(tasks);

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

        FloatingActionButton fab = view.findViewById(R.id.task_list_add_button);
        fab.setOnClickListener(v -> createNewTask());

        progressBar = view.findViewById(R.id.task_list_progressBar);

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
        refreshTaskList();
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

//    private void displayAddTaskDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setMessage(R.string.would_you_like_to_add_a_task_now)
//                .setTitle(R.string.no_tasks_found)
//                .setPositiveButton(R.string.yes, (dialog, id) -> {
//                    dialog.dismiss();
//                    //createNewTask();
//                })
//                .setNegativeButton(R.string.no, (dialog, which) -> {
//                    dialog.dismiss();
//                   // doNotAddTask();
//                });
//        builder.create().show();
//    }


    @SuppressLint("CheckResult")
    private void createNewTask() {
        setProgressBarVisibility(true);
        Task task = new Task("New Task");
        appRepository.insertTask(task).subscribeWith(new DisposableSingleObserver<Long>() {
            @Override
            public void onSuccess(Long taskId) {
                setProgressBarVisibility(false);
                EventBus.getDefault().post(new AddNewTask(taskId));
            }

            @Override
            public void onError(Throwable e) {
                setProgressBarVisibility(false);
                displayAddTaskErrorDialog();

            }
        });
    }

    //TODO - bug report.
    private void displayAddTaskErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.error_adding_new_task)
                .setTitle(R.string.oops)
                .setPositiveButton(R.string.go_back, (dialog, id) -> {
                    dialog.dismiss();
                    doNotAddTask();
                });
        builder.create().show();
    }

    private void doNotAddTask() {
        EventBus.getDefault().post(new AddNewTaskRefused());
    }


    public void addItemClick() {
        //TODO start edit task fragment
        Toast.makeText(getActivity(), "Add task", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onItemClick(Task task, int position) {
        Toast.makeText(getActivity(), "Clicked on task:" + task.getTaskName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
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
