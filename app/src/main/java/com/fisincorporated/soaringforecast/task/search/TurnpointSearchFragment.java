package com.fisincorporated.soaringforecast.task.search;

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

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericListClickListener;
import com.fisincorporated.soaringforecast.messages.GoToTurnpointImport;
import com.fisincorporated.soaringforecast.messages.PopThisFragmentFromBackStack;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.repository.Turnpoint;
import com.fisincorporated.soaringforecast.task.edit.TaskAndTurnpointsViewModel;

import org.greenrobot.eventbus.EventBus;

public class TurnpointSearchFragment extends Fragment implements GenericListClickListener<Turnpoint> {
    private SearchView searchView;
    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;
    private TurnpointSearchListAdapter turnpointSearchListAdapter;
    private AlertDialog noTurnpointsDialog;

    static public TurnpointSearchFragment newInstance() {
        TurnpointSearchFragment turnpointSearchFragment = new TurnpointSearchFragment();
        return turnpointSearchFragment;
    }

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
                    returnToPreviousScreen();

                });
        noTurnpointsDialog = builder.create();
        noTurnpointsDialog.setCanceledOnTouchOutside(false);
        noTurnpointsDialog.show();
    }

    private void returnToPreviousScreen() {
        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
    }

    private void addTurnpoints() {
        EventBus.getDefault().post(new GoToTurnpointImport());
    }
}
