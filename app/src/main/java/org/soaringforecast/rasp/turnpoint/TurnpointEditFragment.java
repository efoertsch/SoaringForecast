package org.soaringforecast.rasp.turnpoint;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.databinding.TurnpointEditView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.turnpoint.cup.CupStyle;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class TurnpointEditFragment  extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    private long turnpointId;
    private TurnpointEditView turnpointEditView ;
    private TurnpointEditViewModel turnpointEditViewModel;
    private CupStyleAdapter cupStyleAdapter;
    private MutableLiveData<List<CupStyle>> cupStyles = new MutableLiveData<>();
    private int lastCupStylePosition = -1;

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

        return  turnpointEditView.getRoot();


    }
}
