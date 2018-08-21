package com.fisincorporated.aviationweather.airport.list;

import android.arch.lifecycle.ViewModelProviders;
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

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.messages.AddAirportEvent;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.touchhelper.OnStartDragListener;
import com.fisincorporated.aviationweather.touchhelper.SimpleItemTouchHelperCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class AirportListFragment extends DaggerFragment implements OnStartDragListener {

    @Inject
    public AppPreferences appPreferences;

    @Inject
    AppRepository appRepository;

    //TODO figure out injection for view model and then also inject adapter
    private AirportListViewModel airportListViewModel;

    private AirportListAdapter airportListAdapter;

    private ItemTouchHelper mItemTouchHelper;


    @Inject
    public AirportListFragment() {
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airport_list, container, false);

        airportListViewModel = ViewModelProviders.of(this).get(AirportListViewModel.class).setAppRepository(appRepository);
        airportListAdapter = new AirportListAdapter();

        RecyclerView recyclerView = view.findViewById(R.id.airport_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(airportListAdapter);

        FloatingActionButton button = view.findViewById(R.id.airport_list_add_button);
        button.setOnClickListener(v -> EventBus.getDefault().post(new AddAirportEvent()));

        airportListAdapter.setOnStartDragListener(this);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(airportListAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        //set title
        getActivity().setTitle(R.string.metar_taf_airports);
        refreshAirports();
    }

    private void refreshAirports() {
        List<String> airportList = appPreferences.getSelectedAirportCodesList();
        airportListViewModel.listSelectedAirports(airportList).observe(this, airports -> {
            airportListAdapter.setAirportList(airports);
        });
    }

   @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

}
