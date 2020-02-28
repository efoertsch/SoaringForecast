package org.soaringforecast.rasp.turnpoints.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.task.edit.TaskAndTurnpointsViewModel;

import androidx.lifecycle.ViewModelProviders;

/**
 * This version of search is to be called by Task fragment as it shares the viewmodel with the task
 * As such it is to run under the TaskActivity
 */
public class TurnpointSearchForTaskFragment extends TurnpointSearchFragment {

    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;

    private GenericListClickListener<Turnpoint> turnpointTextClickListener = (turnpoint, position) -> {
        TaskTurnpoint taskTurnpoint = new TaskTurnpoint(taskAndTurnpointsViewModel.getTaskId(), turnpoint.getTitle(), turnpoint.getCode(), turnpoint.getLatitudeDeg(), turnpoint.getLongitudeDeg());
        taskAndTurnpointsViewModel.addTaskTurnpoint(taskTurnpoint);
        post(new SnackbarMessage(getString(R.string.added_to_task, turnpoint.getTitle()), Snackbar.LENGTH_SHORT));
        searchView.setQuery("", true);
    };

    public static TurnpointSearchForTaskFragment newInstance() {
        return new TurnpointSearchForTaskFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Shared with TaskEditFragment and
        // should already be 'initialized' with AppRepository, taskId, ... before getting here
        taskAndTurnpointsViewModel = ViewModelProviders.of(getActivity()).get(TaskAndTurnpointsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater,container, savedInstanceState);

        // replace default click listener
        turnpointListAdapter.setOnItemClickListener(turnpointTextClickListener);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.add_turnpoints);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.findItem(R.id.turnpoint_search);
        if (menuItem != null) {
            menuItem.setVisible(false);
        }
        menuItem = menu.findItem(R.id.turnpoint_menu_add_export_turnpoints);
        if (menuItem != null) {
            menuItem.setVisible(false);
        }
        menuItem = menu.findItem(R.id.turnpoint_menu_add_email_turnpoints);
        if (menuItem != null) {
            menuItem.setVisible(false);
        }
        menuItem = menu.findItem(R.id.turnpoint_menu_clear_turnpoints);
        if (menuItem != null) {
            menuItem.setVisible(false);
        }

    }

}
