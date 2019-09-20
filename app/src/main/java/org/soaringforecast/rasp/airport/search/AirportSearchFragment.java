package org.soaringforecast.rasp.airport.search;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.airport.list.AirportListAdapter;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.AirportSearchView;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.Airport;
import org.soaringforecast.rasp.repository.AppRepository;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

// started with http://camposha.info/source/android-dialog-fragment-listview-searchfilter-source and
// adapted as needed
public class AirportSearchFragment extends DaggerFragment implements AirportListAdapter.OnItemClickListener {

    @Inject
    AppRepository appRepository;
    @Inject
    AppPreferences appPreferences;

    private SearchView searchView;
    //TODO figure out injection for view model and then also inject adapter
    private AirportSearchViewModel airportSearchViewModel;
    private AirportListAdapter airportListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        airportSearchViewModel = ViewModelProviders.of(this).get(AirportSearchViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AirportSearchView airportSearchView = DataBindingUtil.inflate(inflater, R.layout.airport_search_layout, container, false);

        airportListAdapter = new AirportListAdapter();
        airportListAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = airportSearchView.airportSearchRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(airportListAdapter);

        airportSearchViewModel.getSearchAirports("").observe(this, airports -> airportListAdapter.setAirportList(airports));

        return airportSearchView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.airport_search);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.airport_search_menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(getListener());
        searchView.setQueryHint(getString(R.string.search_for_airports_hint));
        searchView.setIconifiedByDefault(false);
        item.expandActionView();
        searchView.requestFocus();
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            }
        });
    }

    @NonNull
    private SearchView.OnQueryTextListener getListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String txt) {
                // do nothing
                return false;
            }

            @Override
            public boolean onQueryTextChange(String search) {
                if (search == null || search.length() <= 2) {
                    runSearch("");
                } if (search.length() > 2) {
                    runSearch(search);
                }
                return false;
            }
        };
    }

    private void displayKeyboard(boolean show) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            View view = getView().getRootView();
            imm.hideSoftInputFromInputMethod(view.getWindowToken(), 0);
        }
    }

    private void runSearch(String search) {
        // run search and trigger observer to update list of airports
        airportSearchViewModel.searchAirports(search);
    }

    @Override
    public void onItemClick(Airport airport) {
        airportSearchViewModel.addAirportIcaoCodeToSelectedAirports(airport.getIdent());
        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.icao_added_for_metar_taf, airport.getIdent()), Snackbar.LENGTH_SHORT));
        searchView.setQuery("", true);
    }
}
