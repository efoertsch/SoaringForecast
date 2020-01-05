package org.soaringforecast.rasp.airport.list;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.AirportListView;
import org.soaringforecast.rasp.airport.messages.AddAirportEvent;
import org.soaringforecast.rasp.airport.messages.AirportOrderEvent;
import org.soaringforecast.rasp.repository.Airport;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.touchhelper.OnStartDragListener;
import org.soaringforecast.rasp.touchhelper.SimpleItemTouchHelperCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class AirportListFragment extends DaggerFragment implements OnStartDragListener, AirportListAdapter.NewAirportListListener {

    @Inject
    AppRepository appRepository;
    @Inject
    AppPreferences appPreferences;

    //TODO figure out injection for view model
    private AirportListViewModel airportListViewModel;
    private AirportListAdapter airportListAdapter;
    private ItemTouchHelper itemTouchHelper;
    private Observer<List<Airport>> airportListObserver;
    private boolean firstTime = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        airportListViewModel = ViewModelProviders.of(this).get(AirportListViewModel.class)
                .setRepositoryAndPreferences(appRepository, appPreferences);

        airportListObserver = airports -> {
            if (airportListAdapter != null) {
                airportListAdapter.setAirportList(airports);
            }
        };
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AirportListView airportListView = DataBindingUtil.inflate(inflater, R.layout.airport_list_layout, container, false);

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

        airportListViewModel.getSelectedAirports().observe(this, airportListObserver);

        return airportListView.getRoot();
    }



    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.metar_taf_airports);
        if (firstTime) {
            firstTime = false;
        } else {
            // force update as may be returning from search/add and have new airports
            airportListViewModel.refreshSelectedAirportsList();
        }
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
