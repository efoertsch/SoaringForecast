package org.soaringforecast.rasp.task.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.CheckBeforeGoingBack;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.databinding.EditTaskView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;
import org.soaringforecast.rasp.touchhelper.OnStartDragListener;
import org.soaringforecast.rasp.touchhelper.SimpleItemTouchHelperCallback;
import org.soaringforecast.rasp.turnpoints.messages.AddTurnpointsToTask;
import org.soaringforecast.rasp.utils.ViewUtilities;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.DaggerFragment;

public class TaskEditFragment extends DaggerFragment implements OnStartDragListener, CheckBeforeGoingBack {

    private static final String TASKID = "TASKID";
    @Inject
    AppRepository appRepository;

    @Inject
    public TurnpointBitmapUtils turnpointBitmapUtils;

    private long taskId;
    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;
    private TaskTurnpointsRecyclerViewAdapter recyclerViewAdapter;
    private ItemTouchHelper itemTouchHelper;
    private EditTaskView editTaskView;

    public static TaskEditFragment newInstance(long taskId){
        TaskEditFragment taskEditFragment= new TaskEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(TASKID, taskId);
        taskEditFragment.setArguments(bundle);
        return taskEditFragment;
    }


    private GenericListClickListener<TaskTurnpoint> onItemClickListener = (taskTurnpoint, position) -> {
        taskAndTurnpointsViewModel.getTurnpointFromTaskTurnpoint(taskTurnpoint);
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskId = getArguments().getLong(TASKID);
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
        recyclerViewAdapter = TaskTurnpointsRecyclerViewAdapter.getInstance()
                .setTaskAndTurnpointViewModel(taskAndTurnpointsViewModel)
                .setItemClickListener(onItemClickListener)
                .setTurnpointBitmapUtils(turnpointBitmapUtils)
                .setAppRepository(appRepository);

        //TODO DRY
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        ViewUtilities.addRecyclerViewDivider(getContext(), linearLayoutManager.getOrientation(), recyclerView);
        recyclerView.setAdapter(recyclerViewAdapter);

        taskAndTurnpointsViewModel.getTaskTurnpoints().observe(this, taskTurnpoints -> {
            recyclerViewAdapter.setItems(taskTurnpoints);
            taskId = (taskTurnpoints != null && taskTurnpoints.size() > 0) ? taskTurnpoints.get(0).getTaskId() : 0;
            getActivity().invalidateOptionsMenu();
        });

        //TODO DRY
        //DragListener
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(recyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        editTaskView.editTaskAddTurnpoints.setOnClickListener(v -> goToAddTaskTurnpoints());

        FloatingActionButton saveFab = editTaskView.editTaskSaveTask;
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


    private void displayTitle() {
        if (taskId == 0) {
            getActivity().setTitle(R.string.add_task);
        } else {
            getActivity().setTitle(R.string.edit_task);
        }
    }

    private void goToAddTaskTurnpoints() {
        post(new AddTurnpointsToTask());
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
                    post(new PopThisFragmentFromBackStack());

                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    taskAndTurnpointsViewModel.getTaskTurnpoints().removeObservers(this);
                    taskAndTurnpointsViewModel.reset();
                    post(new PopThisFragmentFromBackStack());
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

    }

    public void post(Object object){
        EventBus.getDefault().post(object);
    }
}
