package org.soaringforecast.rasp.airport.airportweather;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.AirportMetarTafView;
import org.soaringforecast.rasp.messages.AddAirportEvent;
import org.soaringforecast.rasp.messages.CallFailure;
import org.soaringforecast.rasp.messages.DisplayAirportList;
import org.soaringforecast.rasp.messages.DisplaySettings;
import org.soaringforecast.rasp.messages.ResponseError;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.retrofit.AviationWeatherApi;
import org.soaringforecast.rasp.utils.ViewUtilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class AirportMetarTafFragment extends DaggerFragment {

    @Inject
    AppRepository appRepository;
    @Inject
    AppPreferences appPreferences;
    @Inject
    public AviationWeatherApi aviationWeatherApi;

    private AirportMetarTafViewModel airportMetarTafViewModel;
    private AirportMetarTafView airportMetarTafView;
    private boolean firstTime = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        airportMetarTafViewModel = ViewModelProviders.of(this).get(AirportMetarTafViewModel.class)
                .setAppPreferences(appPreferences)
                .setAviationWeaterApi(aviationWeatherApi)
                .setAppRepository(appRepository);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        airportMetarTafView = DataBindingUtil.inflate(inflater, R.layout.airport_metar_taf_fragment, container, false);
        RecyclerView recyclerView = airportMetarTafView.airportMetarTafRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        AirportMetarTafAdapter airportMetarTafAdapter = new AirportMetarTafAdapter();
        recyclerView.setAdapter(airportMetarTafAdapter);
        airportMetarTafAdapter.setWeatherMetarTafPreferences(airportMetarTafViewModel);
        airportMetarTafViewModel.getAirportMetarTafs().observe(this, airportWeatherList -> {
            airportMetarTafAdapter.setAirportMetarTafList(airportWeatherList);
        });

        airportMetarTafViewModel.getAirportList().observe(this, airports ->
        airportMetarTafAdapter.setAirportList(airports));

        airportMetarTafViewModel.getAirportMetars().observe(this, metars -> {
            airportMetarTafAdapter.updateMetarList(metars);
        });

        airportMetarTafViewModel.getAirportTaf().observe(this, tafs -> {
            airportMetarTafAdapter.updateTafList(tafs);
        });

        airportMetarTafViewModel.refresh();
        return airportMetarTafView.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.airport_activity_menu, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.nav_drawer_metar_taf_label);
        if (!firstTime) {
            airportMetarTafViewModel.refresh();
        }
        firstTime = false;
        checkForSomeAirports();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.airport_activity_menu_add:
                displayAirportSearchFragment();
                return true;
            case R.id.airport_activity_menu_list:
                airportListFragment();
                return true;
            case R.id.airport_activity_metar_options:
                displaySettingsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkForSomeAirports() {
        // if no airports go to search to add some
        String airportCode = appPreferences.getAirportList();
        if (airportCode == null || airportCode.isEmpty()) {
            displayAddAirportsDialog();
        }
    }

    private void displayAirportSearchFragment() {
        EventBus.getDefault().post(new AddAirportEvent());
    }

    private void airportListFragment() {
        EventBus.getDefault().post(new DisplayAirportList());
    }

    private void displaySettingsMenu() {
        EventBus.getDefault().post(new DisplaySettings());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    private void displayResponseError(ResponseError responseError) {
        ViewUtilities.displayErrorDialog(airportMetarTafView.getRoot(), getString(R.string.oops), responseError.getResponseError());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    private void displayCallFailure(CallFailure callFailure) {
        ViewUtilities.displayErrorDialog(airportMetarTafView.getRoot(), getString(R.string.oops), callFailure.getcallFailure());
    }

    private void displayAddAirportsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.metar_taf_airports)
                .setMessage(R.string.no_metar_taf_airports_selected)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    displayAirportSearchFragment();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    getActivity().finish();
                });
        builder.create().show();

    }

}
