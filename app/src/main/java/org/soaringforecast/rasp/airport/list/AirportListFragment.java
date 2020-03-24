package org.soaringforecast.rasp.airport.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.airport.messages.AddAirportEvent;
import org.soaringforecast.rasp.airport.messages.AirportOrderEvent;
import org.soaringforecast.rasp.airport.messages.DeletedAirport;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.AirportListView;
import org.soaringforecast.rasp.repository.Airport;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.touchhelper.OnStartDragListener;
import org.soaringforecast.rasp.touchhelper.SimpleItemTouchHelperCallback;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.DaggerFragment;

public class AirportListFragment extends DaggerFragment implements OnStartDragListener
        ,View.OnClickListener, AirportListAdapter.NewAirportListListener {

    @Inject
    AppRepository appRepository;
    @Inject
    AppPreferences appPreferences;

    //TODO figure out injection for view model
    private AirportListViewModel airportListViewModel;
    private AirportListAdapter airportListAdapter;
    private ItemTouchHelper itemTouchHelper;
    private boolean firstTime = true;
    private DeletedAirport deletedAirport;
    private AirportListView airportListView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        airportListViewModel = ViewModelProviders.of(this).get(AirportListViewModel.class)
                .setRepositoryAndPreferences(appRepository, appPreferences);

    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
       airportListView = DataBindingUtil.inflate(inflater, R.layout.airport_list_layout, container, false);

        airportListAdapter = new AirportListAdapter();
        RecyclerView recyclerView = airportListView.airportListRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(airportListAdapter);

        FloatingActionButton button = airportListView.airportListAddButton;
        button.setOnClickListener(v -> post(new AddAirportEvent()));

        airportListAdapter.setOnStartDragListener(this).setNewAirportListListener(this);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(airportListAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        airportListViewModel.getSelectedAirports().observe(this, airports -> {
            airportListAdapter.setAirportList(airports);
        });

        return airportListView.getRoot();
    }

    // TODO Put into superclass
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
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

    // TODO Put into superclass
    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AirportOrderEvent event) {
        airportListViewModel.storeNewAirportOrder(event.getAirports());
    }

    @Override
    public void newAirportOrder(List<Airport> airports) {
        airportListViewModel.storeNewAirportOrder(airports);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeletedAirport event){
        this.deletedAirport = event;
        Snackbar snackbar = Snackbar.make(airportListView.airportListCoordinatorLayout,
                R.string.airport_removed, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.undo, this);
        snackbar.show();
    }

    @Override
    public void onClick(View v) {
        if (deletedAirport != null){
            airportListViewModel.unRemoveAirport(deletedAirport);
        }
    }

    // TODO Put into superclass
    private void post(Object object){
        EventBus.getDefault().post(object);
    }

}
