package org.soaringforecast.rasp.task.edit;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.CheckBeforeGoingBack;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.databinding.EditTaskView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpoint;
import org.soaringforecast.rasp.task.messages.AddTurnpointsToTask;
import org.soaringforecast.rasp.touchhelper.OnStartDragListener;
import org.soaringforecast.rasp.touchhelper.SimpleItemTouchHelperCallback;
import org.soaringforecast.rasp.utils.ViewUtilities;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class EditTaskFragment extends DaggerFragment implements OnStartDragListener, CheckBeforeGoingBack {

    @Inject
    AppRepository appRepository;

    private long taskId;

    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;
    private TaskTurnpointsRecyclerViewAdapter recyclerViewAdapter;
    private FloatingActionButton saveFab;
    private ItemTouchHelper itemTouchHelper;
    private EditTaskView editTaskView;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public EditTaskFragment setTaskId(long taskId) {
        this.taskId = taskId;
        return this;
    }

    private View.OnClickListener onItemClickListener = view -> {
       TaskTurnpoint taskTurnpoint = (TaskTurnpoint) view.getTag();
       // find corresponding turnpoint
        getTurnpointFromTaskTurnpoint(taskTurnpoint);


    };

    private void getTurnpointFromTaskTurnpoint(TaskTurnpoint taskTurnpoint) {
        Disposable disposable = appRepository.getTurnpoint(taskTurnpoint.getTitle(), taskTurnpoint.getCode())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpoint-> {
                            EventBus.getDefault().post(new DisplayTurnpoint(turnpoint));
                        },
                        t -> {
                            EventBus.getDefault().post(new DataBaseError(getContext().getString(R.string.error_reading_turnpoint, taskTurnpoint.getTitle(),
                                    taskTurnpoint.getCode()), t));
                        });
        compositeDisposable.add(disposable);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note viewmodel is shared by activity
        taskAndTurnpointsViewModel = ViewModelProviders.of(getActivity())
                .get(TaskAndTurnpointsViewModel.class)
                .setAppRepository(appRepository)
                .setTaskId(taskId);

        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_task_menu, menu);
        MenuItem item = menu.findItem(R.id.edit_task_menu_clone);
        item.setVisible(taskId != 0);
        item.setEnabled(taskId != 0);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_task_menu_clone:
                taskAndTurnpointsViewModel.cloneTask();
                return true;
            default:
                break;
        }

        return false;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        editTaskView = DataBindingUtil.inflate(inflater, R.layout.task_edit_layout, container, false);
        editTaskView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.

        editTaskView.setViewModel(taskAndTurnpointsViewModel);

        RecyclerView recyclerView = editTaskView.editTaskRecyclerView;
        recyclerViewAdapter = new TaskTurnpointsRecyclerViewAdapter(taskAndTurnpointsViewModel);
        recyclerViewAdapter.setOnItemClickListener(onItemClickListener);

        //TODO DRY
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        ViewUtilities.addRecyclerViewDivider(getContext(), linearLayoutManager.getOrientation(), recyclerView);
        recyclerView.setAdapter(recyclerViewAdapter);

        taskAndTurnpointsViewModel.getTaskTurnpoints().observe(this, taskTurnpoints -> {
            recyclerViewAdapter.setItems(taskTurnpoints);
            taskId = (taskTurnpoints != null && taskTurnpoints.size() > 0) ?  taskTurnpoints.get(0).getTaskId() : 0;
            getActivity().invalidateOptionsMenu();
        });

        //TODO DRY
        //DragListener
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(recyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        editTaskView.editTaskAddTurnpoints.setOnClickListener(v -> goToAddTaskTurnpoints());

        saveFab = editTaskView.editTaskSaveTask;
        saveFab.setOnClickListener(v -> {
                    taskAndTurnpointsViewModel.saveTask();
                    getActivity().invalidateOptionsMenu();
                }
        );

        return editTaskView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayTitle();
        // if just added all turnpoints via search, the task distance will not be updated by observer in createView
        // ( as this fragment paused while adding turnpoints, so make sure ui update occurs.
        editTaskView.editTaskDistance.setText(getString(R.string.distance_km, taskAndTurnpointsViewModel.getTaskDistance().getValue()));
    }

    @Override
    public void onStop(){
        compositeDisposable.dispose();
        super.onStop();
    }

    private void displayTitle() {
        if (taskId == 0) {
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

    @Override
    public boolean okToGoBack() {
        // why am I not using observer?
        if (taskAndTurnpointsViewModel.getNeedToSaveUpdates().getValue() == null
                || !taskAndTurnpointsViewModel.getNeedToSaveUpdates().getValue()) {
            return true;
        } else {
            displaySaveFirstDialog();
            return false;
        }
    }

    private void displaySaveFirstDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.save_task)
                .setMessage(R.string.save_task_before_exit)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    taskAndTurnpointsViewModel.saveTask();
                    EventBus.getDefault().post(new PopThisFragmentFromBackStack());

                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    taskAndTurnpointsViewModel.getTaskTurnpoints().removeObservers(this);
                    taskAndTurnpointsViewModel.reset();
                    EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

    }
}
