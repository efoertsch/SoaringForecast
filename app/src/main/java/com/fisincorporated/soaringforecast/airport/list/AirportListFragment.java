package com.fisincorporated.soaringforecast.airport.list;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.databinding.AirportListView;
import com.fisincorporated.soaringforecast.messages.AddAirportEvent;
import com.fisincorporated.soaringforecast.messages.AirportOrderEvent;
import com.fisincorporated.soaringforecast.repository.Airport;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.touchhelper.OnStartDragListener;
import com.fisincorporated.soaringforecast.touchhelper.SimpleItemTouchHelperCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import dagger.android.support.DaggerFragment;

public class AirportListFragment extends DaggerFragment implements OnStartDragListener, AirportListAdapter.NewAirportListListener {

    private AppRepository appRepository;
    private AppPreferences appPreferences;

    //TODO figure out injection for view model
    private AirportListViewModel airportListViewModel;
    private AirportListAdapter airportListAdapter;
    private ItemTouchHelper itemTouchHelper;

    public static AirportListFragment newInstance(AppRepository appRepository, AppPreferences appPreferences) {
        AirportListFragment airportListFragment = new AirportListFragment();
        airportListFragment.appRepository = appRepository;
        airportListFragment.appPreferences = appPreferences;
        return airportListFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AirportListView airportListView = DataBindingUtil.inflate(inflater, R.layout.airport_list_layout, container,false);
        airportListViewModel = ViewModelProviders.of(this).get(AirportListViewModel.class)
                .setRepositoryAndPreferences(appRepository, appPreferences);

        airportListAdapter = new AirportListAdapter();
        RecyclerView recyclerView = airportListView.airportListRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(airportListAdapter);

        FloatingActionButton button = airportListView.airportListAddButton;
        button.setOnClickListener(v -> EventBus.getDefault().post(new AddAirportEvent()));

        airportListAdapter.setOnStartDragListener(this).setNewAirportListListener(this);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(airportListAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return airportListView.getRoot();
    }

    @Override
    public void onResume(){
        super.onResume();
        //set title
        getActivity().setTitle(R.string.metar_taf_airports);
        refreshAirports();
    }

    private void refreshAirports() {
        airportListViewModel.getSelectedAirports().observe(this, airports -> {
            airportListAdapter.setAirportList(airports);
        });
    }

    public void onPause(){
        super.onPause();

    }

   @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AirportOrderEvent event) {
        appPreferences.storeNewAirportOrder(event.getAirports());
    }

    @Override
    public void newAirportOrder(List<Airport> airports) {
        appPreferences.storeNewAirportOrder(airports);
    }
}
