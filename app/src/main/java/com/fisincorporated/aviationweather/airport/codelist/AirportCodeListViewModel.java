package com.fisincorporated.aviationweather.airport.codelist;

import android.databinding.DataBindingUtil;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.databinding.AirportWeatherListBinding;

import javax.inject.Inject;

/**
 * For now a simple method to enter airport codes
 */
public class AirportCodeListViewModel {

    private String airportList = "";

    private View bindingView;

    private AirportWeatherListBinding viewDataBinding;

    private EntryCompleteListener entryCompleteListener;

    @Inject
    public AppPreferences appPreferences;

    interface EntryCompleteListener {
        void onEntryComplete();
    }

    @Inject
    public AirportCodeListViewModel() {
    }

    public AirportCodeListViewModel setView(ViewGroup view) {
        bindingView = view.findViewById(R.id.activity_airport_list);
        viewDataBinding = DataBindingUtil.bind(bindingView);
        airportList = appPreferences.getAirportList();
        viewDataBinding.setAirportList(this);
        // to set cursor at end of list of codes
        viewDataBinding.activityAirportCodes.setText(airportList);
        viewDataBinding.activityAirportCodes.setSelection(airportList.length()> 0 ? airportList.length() : 0);
        return this;

    }

    public AirportCodeListViewModel setEntryCompleteListener(EntryCompleteListener entryCompleteListener) {
        this.entryCompleteListener = entryCompleteListener;
        return this;
    }

    public void onSaveAirports(View view) {
        airportList = viewDataBinding.activityAirportCodes.getText().toString().toUpperCase();
        appPreferences.saveAirportList(airportList);
        if (entryCompleteListener != null) {
            entryCompleteListener.onEntryComplete();
        }
    }

    public String getAirportList() {
        return airportList;
    }

}
