package org.soaringforecast.rasp.turnpoints.list;

import android.Manifest;
import android.os.Bundle;
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
import org.soaringforecast.rasp.databinding.TurnpointListBinding;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpoint;
import org.soaringforecast.rasp.turnpoints.messages.EditTurnpoint;
import org.soaringforecast.rasp.turnpoints.messages.GoToTurnpointImport;
import org.soaringforecast.rasp.turnpoints.messages.TurnpointSearchForEdit;
import org.soaringforecast.rasp.utils.ViewUtilities;

import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class TurnpointListFragment extends DaggerFragment implements EasyPermissions.PermissionCallbacks {

    @Inject
    AppRepository appRepository;

    @Inject
    TurnpointBitmapUtils turnpointBitmapUtils;

    private static final int WRITE_DOWNLOADS_ACCESS = 54321;

    protected TurnpointListAdapter turnpointListAdapter;
    protected TurnpointListViewModel turnpointListViewModel;

    private AlertDialog noTurnpointsDialog;
    private View rootView;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean showSearchIcon = true;
    private boolean refreshOnResume = false;


    private GenericListClickListener<Turnpoint> turnpointTextClickListener = (turnpoint, position) -> {
       post(new EditTurnpoint(turnpoint));
    };

    private GenericListClickListener<Turnpoint> satelliteOnItemClickListener = (turnpoint, position) -> {
        post(new DisplayTurnpoint(turnpoint));
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

        TurnpointListBinding turnpointListBinding = DataBindingUtil.inflate(inflater
                , R.layout.turnpoint_list_layout, container, false);
        turnpointListBinding.setLifecycleOwner(getActivity());
        turnpointListBinding.setViewModel(turnpointListViewModel);

        turnpointListAdapter = TurnpointListAdapter.getInstance()
                .setSateliteOnItemClickListener(satelliteOnItemClickListener)
                .setTurnpointBitmapUtils(turnpointBitmapUtils);

        turnpointListAdapter.setOnItemClickListener(turnpointTextClickListener);

        RecyclerView recyclerView = turnpointListBinding.turnpointListRecyclerView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ViewUtilities.addRecyclerViewDivider(getContext(), linearLayoutManager.getOrientation(), recyclerView);
        recyclerView.setAdapter(turnpointListAdapter);
        checkForAtLeastOneTurnpoint();
        return turnpointListBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.turnpoint_list);
        // subclass my want altered menu items
        getActivity().invalidateOptionsMenu();
        if (refreshOnResume){
            turnpointListViewModel.searchTurnpoints("");
            refreshOnResume = false;
        }
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
            case R.id.turnpoint_menu_add_export_turnpoints:
                exportTurnpoints();
                return true;
            case R.id.turnpoint_menu_clear_turnpoints:
                showClearTurnpointsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewTurnpoint() {
       post(new EditTurnpoint(new Turnpoint()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void displaySearch() {
        post(new TurnpointSearchForEdit());
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
                setUpObservables();
            }
        });
    }

    protected void setUpObservables() {

        turnpointListViewModel.getTurnpoints().observe(this, turnpoints ->{
            turnpointListAdapter.setTurnpointList(turnpoints);
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

    // TODO move this and similar logic in CommonTurnpointsImportFragment to Activity?
    private void showClearTurnpointsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.turnpoints_no_hyphen)
                .setMessage(R.string.turnpoint_delete_all_really_sure)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    turnpointListViewModel.clearTurnpointDatabase();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {

                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    private void returnToPreviousScreen() {
        //getActivity().finish();
        post(new PopThisFragmentFromBackStack());
    }

    private void addTurnpoints() {
        post(new GoToTurnpointImport());
        refreshOnResume = true;
    }

    public void showSearchIconInMenu(boolean showSearchIcon){
        this.showSearchIcon = showSearchIcon;
    }


    //TODO export turnpoints to Downloads directory (name include date/time)
    private void exportTurnpoints() {
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            turnpointListViewModel.writeTurnpointsToDownloadsFile();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_write_downloads_dir), WRITE_DOWNLOADS_ACCESS, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Timber.d("Permission has been granted");
        if (requestCode == WRITE_DOWNLOADS_ACCESS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            turnpointListViewModel.writeTurnpointsToDownloadsFile();
        }
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == WRITE_DOWNLOADS_ACCESS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Timber.d("Permission has been denied");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.no_permission_to_write_downloads_dir)
                    .setTitle(R.string.permission_denied)
                    .setPositiveButton(R.string.ok, (dialog, id) -> {
                        post(new PopThisFragmentFromBackStack());
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }
    }

    //TODO subclass DaggerFragment and move to there - then update other fragments...
    private void post(Object  post){
        EventBus.getDefault().post(post);
    }

}