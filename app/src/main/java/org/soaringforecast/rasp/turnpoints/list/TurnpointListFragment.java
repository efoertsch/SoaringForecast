package org.soaringforecast.rasp.turnpoints.list;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpoint;
import org.soaringforecast.rasp.turnpoints.messages.EditTurnpoint;
import org.soaringforecast.rasp.turnpoints.messages.GoToTurnpointImport;
import org.soaringforecast.rasp.turnpoints.messages.TurnpointSearchForEdit;
import org.soaringforecast.rasp.utils.ViewUtilities;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class TurnpointListFragment extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    @Inject
    TurnpointBitmapUtils turnpointBitmapUtils;

    protected TurnpointListAdapter turnpointListAdapter;
    protected TurnpointListViewModel turnpointListViewModel;

    private AlertDialog noTurnpointsDialog;
    private View rootView;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean showSearchIcon = true;


    private GenericListClickListener<Turnpoint> turnpointTextClickListener = (turnpoint, position) -> {
        EventBus.getDefault().post(new EditTurnpoint(turnpoint.getId()));
    };

    private GenericListClickListener<Turnpoint> satelliteOnItemClickListener = (turnpoint, position) -> {
        EventBus.getDefault().post(new DisplayTurnpoint(turnpoint));
    };


    public static TurnpointListFragment newInstance() {
        return new TurnpointListFragment();
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnpointListViewModel = ViewModelProviders.of(getActivity()).get(TurnpointListViewModel.class);
        turnpointListViewModel.setAppRepository(appRepository);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.turnpoint_list_layout, null);

        turnpointListAdapter = TurnpointListAdapter.getInstance()
                .setSateliteOnItemClickListener(satelliteOnItemClickListener)
                .setTurnpointBitmapUtils(turnpointBitmapUtils);

        turnpointListAdapter.setOnItemClickListener(turnpointTextClickListener);

        RecyclerView recyclerView = rootView.findViewById(R.id.turnpoint_list_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ViewUtilities.addRecyclerViewDivider(getContext(), linearLayoutManager.getOrientation(), recyclerView);
        recyclerView.setAdapter(turnpointListAdapter);

        checkForAtLeastOneTurnpoint();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.turnpoint_list);
        // subclass my want altered menu items
        getActivity().invalidateOptionsMenu();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchIconMenuItem = menu.findItem(R.id.turnpoint_search);
        if (searchIconMenuItem != null) {
            searchIconMenuItem.setVisible(showSearchIcon);
        } else {
            inflater.inflate(R.menu.turnpoint_options_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.turnpoint_search:
                displaySearch();
                return true;
            case R.id.turnpoint_menu_add_new_turnpoint:
                addNewTurnpoint();
                return true;
            case R.id.turnpoint_menu_import:
                addTurnpoints();
                return true;
            case R.id.turnpoint_menu_clear_turnpoints:
                showClearTurnpointsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewTurnpoint() {
        EventBus.getDefault().post((new EditTurnpoint(-1l) ));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void displaySearch() {
        EventBus.getDefault().post(new TurnpointSearchForEdit());
    }

    protected void checkForAtLeastOneTurnpoint() {
        turnpointListViewModel.getNumberOfTurnpoints().observe(this, count -> {
            if (count == 0) {
                displayImportTurnpointsDialog();
            } else {
                if (noTurnpointsDialog != null) {
                    noTurnpointsDialog.dismiss();
                    noTurnpointsDialog = null;
                }
                loadTurnpoints();
            }
        });
    }

    private void loadTurnpoints() {
        turnpointListViewModel.searchTurnpoints("").
                observe(this, turnpoints -> turnpointListAdapter.setTurnpointList(turnpoints));
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

    // TODO move this and similar logic in CommonTurnpointsImportFragment to Activity?
    private void showClearTurnpointsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.turnpoints_no_hyphen)
                .setMessage(R.string.clearing_turnpoints_information)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    clearTurnpointDatabase();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {

                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @SuppressLint("CheckResult")
    private void clearTurnpointDatabase() {
        showProgressBar(true);
        Disposable disposable = appRepository.deleteAllTurnpoints()
                .subscribe(numberDeleted -> {
                    postNumberDeleted(numberDeleted);
                    showProgressBar(false);
                    loadTurnpoints();
                });
        compositeDisposable.add(disposable);

    }

    private void postNumberDeleted(Integer numberDeleted) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.turnpoints_no_hyphen)
                .setMessage(R.string.turnpoints_have_been_cleared)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    // continue
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    protected void showProgressBar(boolean setVisible) {
        rootView.findViewById(R.id.turnpoint_list_recycler_view);
        rootView.findViewById(R.id.turnpoint_list_progress_bar).setVisibility(setVisible ? View.VISIBLE : View.GONE);
    }

    private void returnToPreviousScreen() {
        //getActivity().finish();
        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
    }

    private void addTurnpoints() {
        EventBus.getDefault().post(new GoToTurnpointImport());
    }

    public void showSearchIconInMenu(boolean showSearchIcon){
        this.showSearchIcon = showSearchIcon;
    }

}