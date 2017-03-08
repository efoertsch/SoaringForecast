package com.fisincorporated.aviationweather.airports;

import android.databinding.DataBindingUtil;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.databinding.AirportListBinding;

import javax.inject.Inject;

/**
 * For now a simple method to enter airport codes
 */
public class AirportListViewModel {

    private String airportList = "";

    private View bindingView;

    private AirportListBinding viewDataBinding;

    private EntryCompleteListener entryCompleteListener;

    @Inject
    public AppPreferences appPreferences;

    interface EntryCompleteListener {
        void onEntryComplete();
    }

    @Inject
    public AirportListViewModel() {
    }

    public AirportListViewModel setView(ViewGroup view) {
        bindingView = view.findViewById(R.id.activity_airport_list);
        viewDataBinding = DataBindingUtil.bind(bindingView);
        airportList = appPreferences.getAirportList();
        viewDataBinding.setAirports(this);
        // to set cursor at end of list of codes
        viewDataBinding.activityAirportCodes.setText(airportList);
        viewDataBinding.activityAirportCodes.setSelection(airportList.length()> 0 ? airportList.length() : 0);
        return this;

    }

    public AirportListViewModel setEntryCompleteListener(EntryCompleteListener entryCompleteListener) {
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
