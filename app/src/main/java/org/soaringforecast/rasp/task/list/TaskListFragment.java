package org.soaringforecast.rasp.task.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.recycleradapter.GenericEditClickListener;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.databinding.TaskListView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Task;
import org.soaringforecast.rasp.task.messages.DeleteTask;
import org.soaringforecast.rasp.task.messages.EditTask;
import org.soaringforecast.rasp.task.messages.RenumberedTaskList;
import org.soaringforecast.rasp.task.messages.SelectedTask;
import org.soaringforecast.rasp.touchhelper.OnStartDragListener;
import org.soaringforecast.rasp.touchhelper.SimpleItemTouchHelperCallback;
import org.soaringforecast.rasp.utils.ViewUtilities;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
        // shared with TaskEditFragment
        taskListViewModel = ViewModelProviders.of(this).get(TaskListViewModel.class).setAppRepository(appRepository);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TaskListView taskListView = DataBindingUtil.inflate(inflater, R.layout.task_list_layout, container, false);

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
                post(new EditTask(0)));

        progressBar = taskListView.taskListProgressBar;

        setProgressBarVisibility(true);
        // Should also update when task added on TaskEditFragment
        taskListViewModel.getTaskList().observe(this, taskList -> {
            recyclerViewAdapter.updateTaskList(taskList);
            setProgressBarVisibility(false);
        });
        return taskListView.getRoot();
    }

    // TODO Put into superclass
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.task_list);
        setProgressBarVisibility(true);
        taskListViewModel.listTasks();
    }

    // TODO Put into superclass
    @Override
    public void onPause() {
        super.onPause();
        setProgressBarVisibility(false);
    }

    // TODO Put into superclass
    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        compositeDisposable.dispose();
    }

    private void setProgressBarVisibility(boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(Task task, int position) {
        //TODO best way to do this?
        appPreferences.setSelectedTaskId(task.getId());
        post(new SelectedTask(task.getId()));
    }

    @Override
    public void onEditItemClick(Task task, int position) {
        post(new EditTask(task.getId()));
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeleteTask deleteTask) {
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

    // TODO Put into superclass
    private void post(Object object){
        EventBus.getDefault().post(object);
    }

}
