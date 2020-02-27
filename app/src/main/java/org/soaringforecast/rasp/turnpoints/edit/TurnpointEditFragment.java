package org.soaringforecast.rasp.turnpoints.edit;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.CheckBeforeGoingBack;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.databinding.TurnpointEditView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpointSatelliteView;
import org.soaringforecast.rasp.turnpoints.messages.DisplayAirNav;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class TurnpointEditFragment extends DaggerFragment implements CheckBeforeGoingBack, EasyPermissions.PermissionCallbacks {

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    private static final String TURNPOINT = "TURNPOINT";
    private static final int WRITE_DOWNLOADS_ACCESS = 4343;

    private Turnpoint turnpoint;
    private TurnpointEditViewModel turnpointEditViewModel;
    private CupStyleAdapter cupStyleAdapter;
    private int lastCupStylePosition = -1;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean okToSave = false;
    private boolean inEditMode = false;
    private boolean needToSave;
    private TurnpointEditView turnpointEditView;
    private boolean ignoreFirstOnItemSelected = true;

    public static TurnpointEditFragment newInstance(Turnpoint turnpoint) {
        TurnpointEditFragment turnpointEditFragment = new TurnpointEditFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TURNPOINT, turnpoint);
        turnpointEditFragment.setArguments(bundle);
        return turnpointEditFragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnpoint = getArguments().getParcelable(TURNPOINT);
        // Note viewmodel is shared by activity so this viewmodel will be shared by
        // TurnpointSatelliteViewFragment (if called)
        turnpointEditViewModel = ViewModelProviders.of(getActivity())
                .get(TurnpointEditViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setTurnpoint(turnpoint);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        turnpointEditView = DataBindingUtil.inflate(inflater, R.layout.turnpoint_edit_fragment, container, false);
        turnpointEditView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.
        turnpointEditView.setViewModel(turnpointEditViewModel);
        cupStyleAdapter = new CupStyleAdapter(new ArrayList<>(), getContext());
        turnpointEditView.setSpinAdapterCupStyle(cupStyleAdapter);
        turnpointEditView.turnpointEditGpsImageview.setOnClickListener(v -> {
            displaySatelliteTurnpointView();
        });

        // CupStyles
        turnpointEditViewModel.getCupStyles().observe(this, cupStyles -> {
            cupStyleAdapter.clear();
            cupStyleAdapter.addAll(cupStyles);
        });

        // Placing setting spinner position and onItemSelected here as couldn't get it to work in
        // viewmodel and xml databinding
        turnpointEditViewModel.getCupStylePosition().observe(this, cupStylePosition -> {
            turnpointEditView.turnpointEditStyleSpinner.setSelection(cupStylePosition, false);
        });

        turnpointEditView.turnpointEditStyleSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        turnpointEditViewModel.setCupStylePosition(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        turnpointEditViewModel.getOKToSaveFlag().observe(this, okToSave -> {
            this.okToSave = okToSave;
            getActivity().invalidateOptionsMenu();
        });

        turnpointEditViewModel.getNeedToSaveUpdates().observe(this, needToSave -> {
            this.needToSave = needToSave;
        });

        turnpointEditViewModel.getEditMode().observe(this, inEditMode -> {
            this.inEditMode = inEditMode;
            enableTurnpointEditting();

        });

        // If adding new turnpoint go into edit mode automatically
        turnpointEditViewModel.setInEditMode(turnpoint.getId() <= 0);

        return turnpointEditView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(turnpoint.getId() <= 0 ? R.string.add_new_turnpoint :
                inEditMode ? R.string.edit_turnpoint : R.string.turnpoint);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.turnpoint_edit_options, menu);
        MenuItem editMenuItem = menu.findItem(R.id.turnpoint_edit_menu_edit);
        MenuItem saveMenuItem = menu.findItem(R.id.turnpoint_edit_menu_save);
        MenuItem resetMenuItem = menu.findItem(R.id.turnpoint_edit_menu_reset);
        MenuItem deleteMenuItem = menu.findItem(R.id.turnpoint_edit_menu_delete);
        MenuItem airnavMenuItem = menu.findItem(R.id.turnpoint_edit_menu_airnav);
        if (inEditMode) {
            editMenuItem.setVisible(false);
            saveMenuItem.setVisible(okToSave);
            resetMenuItem.setVisible(true);
            deleteMenuItem.setVisible(true);
        } else {
            editMenuItem.setVisible(true);
            saveMenuItem.setVisible(false);
            resetMenuItem.setVisible(false);
            deleteMenuItem.setVisible(false);
        }
        airnavMenuItem.setVisible(turnpoint != null && (turnpoint.isAirport()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.turnpoint_edit_menu_edit:
                turnpointEditViewModel.setInEditMode(true);
                return true;
            case R.id.turnpoint_edit_menu_toggle_latlng_format:
                turnpointEditViewModel.toggleLatLongFormat();
                return true;
            case R.id.turnpoint_edit_menu_save:
                confirmSave();
                return true;
            case R.id.turnpoint_edit_menu_export:
                exportTurnpoint();
                return true;
            case R.id.turnpoint_edit_menu_email:
                email();
                return true;
            case R.id.turnpoint_edit_menu_reset:
                turnpointEditViewModel.resetTurnpoint();
                return true;
            case R.id.turnpoint_edit_menu_delete:
                checkIfOkToDeleteTurnpoint();
                return true;
            case R.id.turnpoint_edit_menu_airnav:
                displayAirNav();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displaySatelliteTurnpointView() {
        post(new DisplayTurnpointSatelliteView(turnpoint));
    }

    private void displayAirNav() {
        post(new DisplayAirNav(turnpoint));
    }

    private void enableTurnpointEditting() {
        turnpointEditView.turnpointEditTitle.setEnabled(inEditMode);
        // Can't edit code if existing record
        turnpointEditView.turnpointEditCode.setEnabled(turnpointEditViewModel.isTurnpointCodeEditEnabled());
        turnpointEditView.turnpointEditCountry.setEnabled(inEditMode);
        turnpointEditView.turnpointEditCupLatitude.setEnabled(inEditMode);
        turnpointEditView.turnpointEditCupLongitude.setEnabled(inEditMode);
        turnpointEditView.turnpointEditGoogleLatitude.setEnabled(inEditMode);
        turnpointEditView.turnpointEditGoogleLongitude.setEnabled(inEditMode);
        turnpointEditView.turnpointEditElevation.setEnabled(inEditMode);
        turnpointEditView.turnpointEditStyleSpinner.setEnabled(inEditMode);
        turnpointEditView.turnpointEditRunwayDirection.setEnabled(inEditMode);
        turnpointEditView.turnpointEditRunwayLength.setEnabled(inEditMode);
        turnpointEditView.turnpointEditAirportFrequency.setEnabled(inEditMode);
        turnpointEditView.turnpointEditDescription.setEnabled(inEditMode);
        getActivity().invalidateOptionsMenu();
    }

    private void checkIfOkToDeleteTurnpoint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.turnpoint_delete)
                .setMessage(R.string.turnpoint_delete_really_sure)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    okToDeleteOneMoreTime();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {

                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    private void okToDeleteOneMoreTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.turnpoint_delete)
                .setMessage(R.string.turnpoint_delete_really_sure_times_two)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    deleteTurnpoint();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    /// Whew! Saved by the bell.
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void deleteTurnpoint() {
        Disposable disposable = turnpointEditViewModel.deleteTurnpoint()
                .subscribe(numberDeleted -> {
                    if (numberDeleted == 1) {
                        post(new SnackbarMessage(getString(R.string.turnpoint_deleted)));
                        post(new PopThisFragmentFromBackStack());
                    } else {
                        post(new SnackbarMessage(getString(R.string.turnpoint_delete_error)));
                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public boolean okToGoBack() {
        if (needToSave == false) {
            return true;
        } else {
            displaySaveFirstDialog();
            return false;
        }
    }

    // Run when save menuitem pressed
    private void confirmSave() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.save_turnpoint)
                .setMessage(R.string.save_turnpoint_changes)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    turnpointEditViewModel.saveTurnpoint();
                    post(new PopThisFragmentFromBackStack());
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    // No action
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    // Run when user made changes but then hit back without saving changes
    private void displaySaveFirstDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.save_turnpoint)
                .setMessage(R.string.save_turnpoint_before_exit)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    turnpointEditViewModel.saveTurnpoint();
                    post(new PopThisFragmentFromBackStack());
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    turnpointEditViewModel.resetTurnpoint();
                    post(new PopThisFragmentFromBackStack());
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    private void email() {
        turnpointEditViewModel.setEmailTurnpoint();
        exportTurnpoint();
    }

    private void exportTurnpoint() {
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            turnpointEditViewModel.writeTurnpointToDownloadsFile();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_write_downloads_dir), WRITE_DOWNLOADS_ACCESS, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    //TODO subclass and set up with TurnpointListFragment
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
            turnpointEditViewModel.writeTurnpointToDownloadsFile();
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
    private void post(Object post) {
        EventBus.getDefault().post(post);
    }

}

