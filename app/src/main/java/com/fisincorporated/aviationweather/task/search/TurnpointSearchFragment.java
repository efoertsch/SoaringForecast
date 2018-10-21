package com.fisincorporated.aviationweather.task.search;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericListClickListener;
import com.fisincorporated.aviationweather.messages.ExitFromTurnpointSearch;
import com.fisincorporated.aviationweather.messages.GoToTurnpointImport;
import com.fisincorporated.aviationweather.messages.SnackbarMessage;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;
import com.fisincorporated.aviationweather.repository.Turnpoint;
import com.fisincorporated.aviationweather.task.edit.TaskAndTurnpointsViewModel;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;

public class TurnpointSearchFragment extends Fragment implements GenericListClickListener<Turnpoint> {
    private SearchView searchView;
    private AppRepository appRepository;
    private long taskId;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private int maxTurnpointOrderNumber = 0;
    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;

    private TurnpointSearchListAdapter turnpointSearchListAdapter;

    static public TurnpointSearchFragment newInstance() {
        TurnpointSearchFragment turnpointSearchFragment = new TurnpointSearchFragment();
        return turnpointSearchFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.turnpoint_search_layout, null);

        // Shared with EditTaskFragment and
        // should already be 'initialized' with AppRepository... before getting here
        taskAndTurnpointsViewModel = ViewModelProviders.of(getActivity()).get(TaskAndTurnpointsViewModel.class);

        turnpointSearchListAdapter = new TurnpointSearchListAdapter();
        turnpointSearchListAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = rootView.findViewById(R.id.turnpoint_search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(turnpointSearchListAdapter);
        setHasOptionsMenu(true);

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
        TaskTurnpoint taskTurnpoint = new TaskTurnpoint(taskId, turnpoint.getTitle(), turnpoint.getCode());
        taskAndTurnpointsViewModel.addTaskTurnpoint(taskTurnpoint);
        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.added_to_task, turnpoint.getTitle())));
        searchView.setQuery("", true);

    }

    private void checkForAtLeastOneTurnpoint() {
        taskAndTurnpointsViewModel.getNumberOfSearchableTurnpoints().observe(this, count -> {
            if (count == 0) {
                displayImportTurnpointsDialog();
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
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    returnToPreviousScreen();

                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void returnToPreviousScreen() {
        EventBus.getDefault().post(new ExitFromTurnpointSearch());
    }

    private void addTurnpoints() {
        EventBus.getDefault().post(new GoToTurnpointImport());
    }
}
