package com.fisincorporated.soaringforecast.settings.forecastorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.databinding.ForecastOrderListView;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;
import com.fisincorporated.soaringforecast.touchhelper.OnStartDragListener;
import com.fisincorporated.soaringforecast.touchhelper.SimpleItemTouchHelperCallback;
import com.fisincorporated.soaringforecast.utils.ViewUtilities;

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
