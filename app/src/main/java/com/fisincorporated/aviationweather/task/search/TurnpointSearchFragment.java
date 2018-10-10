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

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import timber.log.Timber;

public class TurnpointSearchFragment extends Fragment implements GenericListClickListener<Turnpoint> {
    private SearchView searchView;
    private AppRepository appRepository;
    private long taskId;
    private int taskOrder;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    //TODO figure out injection for view model and then also inject adapter
    TurnpointSearchViewModel turnpointSearchViewModel;
    TurnpointSearchListAdapter turnpointSearchListAdapter;

    static public TurnpointSearchFragment newInstance(AppRepository appRepository, long taskId) {
        TurnpointSearchFragment turnpointSearchFragment = new TurnpointSearchFragment();
        turnpointSearchFragment.appRepository = appRepository;
        turnpointSearchFragment.taskId = taskId;
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

        return rootView;
    }

    @Override
    public void onResume(){
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
//        searchView.requestFocus();
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

    private void runSearch(String search) {
        turnpointSearchViewModel.searchTurnpoints(search).observe(this, turnpoints -> turnpointSearchListAdapter.setTurnpointList(turnpoints));
    }

//    private void listAllTurnpoints() {
//        turnpointSearchViewModel.listAllTurnpoints().observe(this, turnpoints -> turnpointSearchListAdapter.setTurnpointList(turnpoints));
//    }

    @SuppressLint("CheckResult")
    @Override
    public void onItemClick(Turnpoint turnpoint, int position) {
        TaskTurnpoint taskTurnpoint = new TaskTurnpoint(taskId, turnpoint.getTitle(), turnpoint.getCode(), taskOrder);
        appRepository.addTurnpointToTask(taskTurnpoint).subscribeWith(new DisposableSingleObserver<Long>() {
            @Override
            public void onSuccess(Long taskId) {
                EventBus.getDefault().post(new SnackbarMessage(getString(R.string.added_to_task, turnpoint.getTitle())));
                searchView.setQuery("",true);
            }

            @Override
            public void onError(Throwable e) {
               EventBus.getDefault().post(new SnackbarMessage((getString(R.string.error_adding_turnpoint_to_task))));

            }
        });
    }

    private void checkForAtLeastOneTurnpoint() {
        Disposable disposable = appRepository.getCountOfTurnpoints()
                .subscribe(count -> {
                            if (count == 0) {
                                displayImportTurnpointsDialog();
                            }
                        }
                        , throwable -> {
                            //TODO
                            Timber.e(throwable);
                        });
        compositeDisposable.add(disposable);
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
