package org.soaringforecast.rasp.turnpoints.edit;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.databinding.TurnpointEditView;
import org.soaringforecast.rasp.repository.AppRepository;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class TurnpointEditFragment  extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    private long turnpointId;
    private TurnpointEditView turnpointEditView ;
    private TurnpointEditViewModel turnpointEditViewModel;
    private CupStyleAdapter cupStyleAdapter;

    private int lastCupStylePosition = -1;
    private Boolean saveIndicator = false;

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

        turnpointEditViewModel.getNeedToSave().observe(this, saveIndicator -> {
            updateSaveMenuOption(saveIndicator);
        });

        return  turnpointEditView.getRoot();
    }

    private void updateSaveMenuOption(Boolean saveIndicator) {
        this.saveIndicator = saveIndicator;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().setTitle(turnpointId <=0 ? R.string.add_new_turnpoint : R.string.edit_turnpoint);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.turnpoint_edit_options, menu);
        MenuItem saveMenuItem = menu.findItem(R.id.turnpoint_edit_menu_save);
        MenuItem resetMenuItem = menu.findItem(R.id.turnpoint_edit_menu_reset);
        saveMenuItem.setVisible(saveIndicator);

        if (saveIndicator) {
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
