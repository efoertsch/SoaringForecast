package org.soaringforecast.rasp.task.search;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.messages.GoToTurnpointImport;
import org.soaringforecast.rasp.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.task.edit.TaskAndTurnpointsViewModel;

public class TurnpointSearchFragment extends Fragment implements GenericListClickListener<Turnpoint> {

    private SearchView searchView;
    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;
    private TurnpointSearchListAdapter turnpointSearchListAdapter;
    private AlertDialog noTurnpointsDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Shared with EditTaskFragment and
        // should already be 'initialized' with AppRepository, taskId, ... before getting here
        taskAndTurnpointsViewModel = ViewModelProviders.of(getActivity()).get(TaskAndTurnpointsViewModel.class);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.turnpoint_search_layout, null);

        turnpointSearchListAdapter = new TurnpointSearchListAdapter();
        turnpointSearchListAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = rootView.findViewById(R.id.turnpoint_search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(turnpointSearchListAdapter);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.add_turnpoints);
        checkForAtLeastOneTurnpoint();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.turnpoint_search_menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(getListener());
        searchView.setQueryHint(getString(R.string.search_for_turnpoints_hint));
        searchView.setIconifiedByDefault(false);
        item.expandActionView();
        searchView.requestFocus();
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            }
        });

    }

    @NonNull
    public SearchView.OnQueryTextListener getListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String txt) {
                // do nothing
                return false;
            }

            @Override
            public boolean onQueryTextChange(String search) {
                if (search.length() == 0) {
                    runSearch(search);
                } else if (search.length() <= 2) {
                    clearTurnpointList();
                } else {
                    runSearch(search);
                }

                return false;
            }
        };
    }

    private void clearTurnpointList() {
        turnpointSearchListAdapter.setTurnpointList(null);
    }

    private void runSearch(String search) {
        taskAndTurnpointsViewModel.searchTurnpoints(search).observe(this, turnpoints -> turnpointSearchListAdapter.setTurnpointList(turnpoints));
    }

    @SuppressLint("CheckResult")
    @Override
    public void onItemClick(Turnpoint turnpoint, int position) {
        TaskTurnpoint taskTurnpoint = new TaskTurnpoint(taskAndTurnpointsViewModel.getTaskId(), turnpoint.getTitle(), turnpoint.getCode(), turnpoint.getLatitudeDeg(), turnpoint.getLongitudeDeg());
        taskAndTurnpointsViewModel.addTaskTurnpoint(taskTurnpoint);
        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.added_to_task, turnpoint.getTitle()), Snackbar.LENGTH_SHORT));
        searchView.setQuery("", true);

    }

    private void checkForAtLeastOneTurnpoint() {
        taskAndTurnpointsViewModel.getNumberOfSearchableTurnpoints().observe(this, count -> {
            if (count == 0) {
                displayImportTurnpointsDialog();
            } else {
                if (noTurnpointsDialog != null) {
                    noTurnpointsDialog.dismiss();
                    noTurnpointsDialog = null;
                }
            }
        });
    }

    private void displayImportTurnpointsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.no_turnpoints_found_add_some)
                .setTitle(R.string.no_turnpoints_found)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    addTurnpoints();
                    //TODO go to Turnpoint import
                    noTurnpointsDialog = null;
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    noTurnpointsDialog = null;
                    returnToPreviousScreen();

                });
        noTurnpointsDialog = builder.create();
        noTurnpointsDialog.setCanceledOnTouchOutside(false);
        noTurnpointsDialog.show();
    }

    private void returnToPreviousScreen() {
        getActivity().finish();
        //EventBus.getDefault().post(new PopThisFragmentFromBackStack());
    }

    private void addTurnpoints() {
        EventBus.getDefault().post(new GoToTurnpointImport());
    }
}
