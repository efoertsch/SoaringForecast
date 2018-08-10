package com.fisincorporated.aviationweather.airport.list;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.repository.AppRepository;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class AirportListFragment extends DaggerFragment {


    @Inject
    AppRepository appRepository;

    //TODO figure out injection for view model and then also inject adapter
    AirportListViewModel airportListViewModel;

    AirportListAdapter airportListAdapter;


    @Inject
    public AirportListFragment(){}

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.airport_list, container, false);

        airportListViewModel = ViewModelProviders.of(this).get(AirportListViewModel.class).setAppRepository(appRepository);
        airportListAdapter = new AirportListAdapter();

        RecyclerView recyclerView = view.findViewById(R.id.airport_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(airportListAdapter);


        airportListViewModel.listAirports().observe(this, airports -> {
            airportListAdapter.setAirportList(airports);
        });

        return view;
    }


}
