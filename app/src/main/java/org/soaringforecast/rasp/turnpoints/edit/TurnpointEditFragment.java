package org.soaringforecast.rasp.turnpoints.edit;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.databinding.TurnpointEditView;
import org.soaringforecast.rasp.repository.AppRepository;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class TurnpointEditFragment  extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    private long turnpointId;
    private TurnpointEditView turnpointEditView ;
    private TurnpointEditViewModel turnpointEditViewModel;
    private CupStyleAdapter cupStyleAdapter;

    private int lastCupStylePosition = -1;
    private Boolean okToSave = false;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static TurnpointEditFragment  newInstance(long turnpointId) {
        TurnpointEditFragment turnpointEditFragment = new TurnpointEditFragment();
        turnpointEditFragment.setTurnpointId(turnpointId);
        return turnpointEditFragment;
    }

    private void setTurnpointId(long turnpointId) {
        this.turnpointId = turnpointId;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note viewmodel is shared by activity
        turnpointEditViewModel = ViewModelProviders.of(getActivity())
                .get(TurnpointEditViewModel.class)
                .setAppRepository(appRepository)
                .setTurnpointId(turnpointId);
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
            if (newCupStylePosition != null){
                if (lastCupStylePosition != -1 && lastCupStylePosition != newCupStylePosition){
                    turnpointEditViewModel.setInitialCupStylePosition(newCupStylePosition);
                }
            }
            lastCupStylePosition = newCupStylePosition;
        });

        turnpointEditViewModel.getOKToSaveFlag().observe(this, okToSave -> {
            this.okToSave = okToSave;
            getActivity().invalidateOptionsMenu();
        });

        return  turnpointEditView.getRoot();
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().setTitle(turnpointId <=0 ? R.string.add_new_turnpoint : R.string.edit_turnpoint);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.turnpoint_edit_options, menu);
        MenuItem saveMenuItem = menu.findItem(R.id.turnpoint_edit_menu_save);
        MenuItem resetMenuItem = menu.findItem(R.id.turnpoint_edit_menu_reset);
        saveMenuItem.setVisible(okToSave);

        if (okToSave) {
            resetMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        } else {
            resetMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.turnpoint_edit_menu_save:
                turnpointEditViewModel.saveTurnpoint();
                return true;
            case R.id.turnpoint_edit_menu_reset:
                turnpointEditViewModel.resetTurnpoint();
                return true;
            case R.id.turnpoint_edit_menu_delete:
                checkIfOkToDeleteTurnpoint();
            default:
                return super.onOptionsItemSelected(item);
        }
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
                    turnpointEditViewModel.deleteTurnpoint();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                        /// Whew! Saved by the bell.
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void  deleteTurnpoint() {
        Disposable disposable = turnpointEditViewModel.deleteTurnpoint()
                .subscribe(numberDeleted -> {
                    if (numberDeleted == 1) {
                        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.turnpoint_deleted)));
                        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                    }
                    else {
                        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.turnpoint_delete_error)));
                    }
                });
        compositeDisposable.add(disposable);
    }

}
