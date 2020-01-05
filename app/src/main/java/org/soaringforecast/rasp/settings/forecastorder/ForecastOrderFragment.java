package org.soaringforecast.rasp.settings.forecastorder;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.ForecastOrderListView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.soaring.json.Forecast;
import org.soaringforecast.rasp.touchhelper.OnStartDragListener;
import org.soaringforecast.rasp.touchhelper.SimpleItemTouchHelperCallback;
import org.soaringforecast.rasp.utils.ViewUtilities;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class ForecastOrderFragment extends DaggerFragment implements OnStartDragListener, ForecastOrderAdapter.OnItemClickListener {

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    //TODO figure out injection for view model
    private ForecastOrderListView forecastOrderListView;
    private ForecastOrderViewModel forecastOrderViewModel;
    private ForecastOrderAdapter forecastOrderAdapter;
    private ItemTouchHelper itemTouchHelper;
    private Observer<List<Forecast>> forecastListObserver;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        forecastOrderViewModel = ViewModelProviders.of(this).get(ForecastOrderViewModel.class)
                .setRepositoryAndPreferences(appRepository, appPreferences);

        forecastListObserver = forecasts -> {
            if (forecastOrderAdapter != null) {
                forecastOrderAdapter.setForecastList(forecasts);
            }
        };
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        forecastOrderListView = DataBindingUtil.inflate(inflater, R.layout.forecast_order_list, container, false);
        forecastOrderListView.setLifecycleOwner(this);

        forecastOrderAdapter = new ForecastOrderAdapter();
        forecastOrderAdapter.setOnItemClickListener(this);
        forecastOrderAdapter.setNewForecastOrderListener(forecastOrderViewModel);
        RecyclerView recyclerView = forecastOrderListView.forecastOrderRecyclerView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ViewUtilities.addRecyclerViewDivider(getContext(), linearLayoutManager.getOrientation(), recyclerView);
        recyclerView.setAdapter(forecastOrderAdapter);

        //forecastOrderAdapter.setOnStartDragListener(this).setNewForecastListListener(forecastOrderViewModel);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(forecastOrderAdapter){
            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        };
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        forecastOrderViewModel.getOrderedForecasts().observe(this, forecastListObserver);

        return forecastOrderListView.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.order_forecasts);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.forecast_order_menu_reset:
                forecastOrderViewModel.deleteCustomForecastOrder();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_order_menu, menu);
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemClick(Forecast forecast) {
          ForecastOrderBottomSheetFragment forecastOrderBottomSheetFragment =
                  ForecastOrderBottomSheetFragment.newInstance(forecast);
          forecastOrderBottomSheetFragment.show(getActivity().getSupportFragmentManager(),"forecastDescription");
    }
}
