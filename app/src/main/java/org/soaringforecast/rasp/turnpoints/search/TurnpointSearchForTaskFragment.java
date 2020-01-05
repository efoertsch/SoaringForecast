package org.soaringforecast.rasp.turnpoints.search;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.task.edit.TaskAndTurnpointsViewModel;

public class TurnpointSearchForTaskFragment extends TurnpointSearchFragment {

    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;

    private GenericListClickListener<Turnpoint> turnpointTextClickListener = (turnpoint, position) -> {
        TaskTurnpoint taskTurnpoint = new TaskTurnpoint(taskAndTurnpointsViewModel.getTaskId(), turnpoint.getTitle(), turnpoint.getCode(), turnpoint.getLatitudeDeg(), turnpoint.getLongitudeDeg());
        taskAndTurnpointsViewModel.addTaskTurnpoint(taskTurnpoint);
        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.added_to_task, turnpoint.getTitle()), Snackbar.LENGTH_SHORT));
        searchView.setQuery("", true);
    };

    public static TurnpointSearchForTaskFragment newInstance() {
        return new TurnpointSearchForTaskFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Shared with EditTaskFragment and
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

}
