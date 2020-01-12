package org.soaringforecast.rasp.turnpoints.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.CheckBeforeGoingBack;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.databinding.TurnpointEditView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class TurnpointEditFragment extends DaggerFragment implements CheckBeforeGoingBack {

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;


    private static final String TURNPOINT = "TURNPOINT";

    private Turnpoint turnpoint;
    private TurnpointEditViewModel turnpointEditViewModel;
    private CupStyleAdapter cupStyleAdapter;

    private int lastCupStylePosition = -1;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean okToSave = false;
    private boolean inEditMode = false;
    private boolean needToSave;
    private TurnpointEditView turnpointEditView;

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
                .reset()
                .setTurnpoint(getArguments().getParcelable(TURNPOINT));

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

        // Forecasts
        turnpointEditViewModel.getCupStyles().observe(this, forecasts -> {
            cupStyleAdapter.clear();
            cupStyleAdapter.addAll(forecasts);
        });

        turnpointEditViewModel.getCupStylePosition().observe(this, newCupStylePosition -> {
            if (newCupStylePosition != null) {
                if (lastCupStylePosition != -1 && lastCupStylePosition != newCupStylePosition) {
                    turnpointEditViewModel.setCupStylePosition(newCupStylePosition);
                }
            }
            lastCupStylePosition = newCupStylePosition;
        });

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
        turnpointEditViewModel.setInEditMode(turnpoint.getId() < 0);

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
        if (inEditMode) {
            editMenuItem.setVisible(false);
            saveMenuItem.setVisible(okToSave);
            resetMenuItem.setVisible(true);
        } else {
            editMenuItem.setVisible(true);
            saveMenuItem.setVisible(false);
            resetMenuItem.setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.turnpoint_edit_menu_edit:
                turnpointEditViewModel.setInEditMode(true);
                return true;
            case R.id.turnpoint_edit_menu_save:
                confirmSave();
                return true;
            case R.id.turnpoint_edit_menu_reset:
                turnpointEditViewModel.resetTurnpoint();
                return true;
            case R.id.turnpoint_edit_menu_delete:
                checkIfOkToDeleteTurnpoint();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void enableTurnpointEditting() {
        turnpointEditView.turnpointEditTitle.setEnabled(inEditMode);
        turnpointEditView.turnpointEditCode.setEnabled(inEditMode);
        turnpointEditView.turnpointEditCountry.setEnabled(inEditMode);
        turnpointEditView.turnpointEditLatitude.setEnabled(inEditMode);
        turnpointEditView.turnpointEditLongitude.setEnabled(inEditMode);
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
                        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.turnpoint_deleted)));
                        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                    } else {
                        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.turnpoint_delete_error)));
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
                    EventBus.getDefault().post(new PopThisFragmentFromBackStack());
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
                    EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    turnpointEditViewModel.resetTurnpoint();
                    EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

    }

}

