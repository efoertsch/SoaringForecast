package org.soaringforecast.rasp.turnpoints.search;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.view.inputmethod.InputMethodManager;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpoint;
import org.soaringforecast.rasp.task.messages.GoToTurnpointImport;
import org.soaringforecast.rasp.utils.ViewUtilities;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class TurnpointSearchFragment extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    protected SearchView searchView;
    protected TurnpointSearchListAdapter turnpointSearchListAdapter;
    protected TurnpointSearchViewModel turnpointSearchViewModel;

    private AlertDialog noTurnpointsDialog;

    @Inject
    TurnpointBitmapUtils turnpointBitmapUtils;

    private GenericListClickListener<Turnpoint> satelliteOnItemClickListener = (turnpoint, position) -> {
        if (searchView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }
        EventBus.getDefault().post(new DisplayTurnpoint(turnpoint));
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnpointSearchViewModel = ViewModelProviders.of(getActivity()).get(TurnpointSearchViewModel.class);
        turnpointSearchViewModel.setAppRepository(appRepository);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.turnpoint_search_layout, null);

        turnpointSearchListAdapter = TurnpointSearchListAdapter.getInstance()
                .setSateliteOnItemClickListener(satelliteOnItemClickListener)
                .setTurnpointBitmapUtils(turnpointBitmapUtils);

        RecyclerView recyclerView = rootView.findViewById(R.id.turnpoint_search_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ViewUtilities.addRecyclerViewDivider(getContext(), linearLayoutManager.getOrientation(), recyclerView);
        recyclerView.setAdapter(turnpointSearchListAdapter);
        return rootView;
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
    private SearchView.OnQueryTextListener getListener() {
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
        turnpointSearchViewModel.searchTurnpoints(search).observe(this, turnpoints -> turnpointSearchListAdapter.setTurnpointList(turnpoints));
    }

    protected void checkForAtLeastOneTurnpoint() {
        turnpointSearchViewModel.getNumberOfSearchableTurnpoints().observe(this, count -> {
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
