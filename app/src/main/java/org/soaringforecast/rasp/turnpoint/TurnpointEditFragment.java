package org.soaringforecast.rasp.turnpoint;

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

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class TurnpointEditFragment  extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    private long turnpointId;
    private TurnpointEditView turnpointEditView ;
    private TurnpointEditViewModel turnpointEditViewModel;

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
        return  turnpointEditView.getRoot();


    }
}
