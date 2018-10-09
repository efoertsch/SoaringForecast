package com.fisincorporated.aviationweather.task.search;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.support.v7.widget.SearchView;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericListClickListener;
import com.fisincorporated.aviationweather.messages.SnackbarMessage;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Turnpoint;

import org.greenrobot.eventbus.EventBus;

public class TurnpointSearchFragment extends Fragment implements GenericListClickListener<Turnpoint> {
    SearchView searchView;
    AppRepository appRepository;

    //TODO figure out injection for view model and then also inject adapter
    TurnpointSearchViewModel turnpointSearchViewModel;

    TurnpointSearchListAdapter turnpointSearchListAdapter;

    static public TurnpointSearchFragment newInstance(AppRepository appRepository) {
        TurnpointSearchFragment turnpointSearchFragment = new TurnpointSearchFragment();
        turnpointSearchFragment.appRepository = appRepository;
        return turnpointSearchFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.turnpoint_search_layout, null);

        turnpointSearchViewModel = ViewModelProviders.of(this).get(TurnpointSearchViewModel.class).setAppRepository(appRepository);
        turnpointSearchListAdapter = new TurnpointSearchListAdapter();
        turnpointSearchListAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = rootView.findViewById(R.id.turnpoint_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(turnpointSearchListAdapter);
        setHasOptionsMenu(true);
//        listAllTurnpoints();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.turnpoint_search);
        //displayKeyboard(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        // displayKeyboard(false);
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
//        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                return false;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                getActivity().getSupportFragmentManager().popBackStack();
//                return true;
//            }
//        });
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

    private void displayKeyboard(boolean show) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            View view = getView().getRootView();
            imm.hideSoftInputFromInputMethod(view.getWindowToken(), 0);
        }
    }

    private void runSearch(String search) {
        turnpointSearchViewModel.searchTurnpoints(search).observe(this, turnpoints -> turnpointSearchListAdapter.setTurnpointList(turnpoints));
    }

//    private void listAllTurnpoints() {
//        turnpointSearchViewModel.listAllTurnpoints().observe(this, turnpoints -> turnpointSearchListAdapter.setTurnpointList(turnpoints));
//    }

    @Override
    public void onItemClick(Turnpoint turnpoint, int position) {
        //TODO check if already added, store to task if not
        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.added_to_task, turnpoint.getTitle())));
    }
}
