package com.fisincorporated.soaringforecast.soaring.forecast;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.databinding.SoaringForecastImageBinding;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.retrofit.SoaringForecastApi;
import com.fisincorporated.soaringforecast.soaring.forecast.adapters.ForecastDateRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.soaring.forecast.adapters.ForecastModelRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.soaring.forecast.adapters.SoaringForecastRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;
import com.fisincorporated.soaringforecast.soaring.json.ModelForecastDate;
import com.fisincorporated.soaringforecast.task.TaskActivity;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class SoaringForecastFragment extends DaggerFragment {

    private AppRepository appRepository;
    private AppPreferences appPreferences;

    @Inject
    ForecastMapper forecastMapper;

    @Inject
    SoaringForecastApi soaringForecastApi;

    private SoaringForecastViewModel soaringForecastViewModel;
    private SoaringForecastImageBinding soaringForecastImageBinding;
    private ForecastModelRecyclerViewAdapter forecastModelrecyclerViewAdapter;
    private ForecastDateRecyclerViewAdapter forecastDateRecyclerViewAdapter;
    private SoaringForecastRecyclerViewAdapter soaringForecastRecyclerViewAdapter;

    private MenuItem clearTaskMenuItem;

    // TODO replace with livedata in viewmodel
    private boolean showClearTaskMenuItem;

    public static SoaringForecastFragment newInstance(AppRepository appRepository, AppPreferences appPreferences) {
        SoaringForecastFragment soaringForecastFragment = new SoaringForecastFragment();
        soaringForecastFragment.appRepository = appRepository;
        soaringForecastFragment.appPreferences = appPreferences;
        return soaringForecastFragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        soaringForecastViewModel = ViewModelProviders.of(this)
                .get(SoaringForecastViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setSoaringForecastApi(soaringForecastApi);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        soaringForecastImageBinding = DataBindingUtil.inflate(inflater
                , R.layout.soaring_forecast_rasp, container, false);
        soaringForecastImageBinding.setLifecycleOwner(this);
        setupViews();
        return soaringForecastImageBinding.getRoot();
    }

    private void setupViews() {
        // TODO how to get from binding
        forecastMapper.displayMap((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.soaring_forecast_map));
        setupSoaringForecastModelsRecyclerView(null);
        setupSoaringConditionRecyclerView(null);
        setupModelForecastDateRecyclerView(null);
        soaringForecastImageBinding.soaringForecastSeekbarOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                forecastMapper.setForecastOverlayOpacity(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // nada
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // nada
            }
        });
        setObservers();

    }

    private void setObservers(){
        soaringForecastViewModel.getSoaringForecastModels().observe(this, soaringForecastModels -> {
            //TODO create preference for model to use for first display
            forecastModelrecyclerViewAdapter.setItems(soaringForecastModels);
            if (soaringForecastModels != null && soaringForecastModels.size() > 0) {
                forecastModelrecyclerViewAdapter.setSelectedForecastModel(soaringForecastModels.get(0));
            }
        });

        soaringForecastViewModel.getForecasts().observe(this, forecasts -> {
                    soaringForecastRecyclerViewAdapter.setItems(forecasts);
                }
        );

        soaringForecastViewModel.getModelForecastDates().observe(this, modelForecastDates ->
                forecastDateRecyclerViewAdapter.setItems(modelForecastDates));

        soaringForecastViewModel.getTaskTurnpoints().observe(this, taskTurnpoints ->
                forecastMapper.setTaskTurnpoints(taskTurnpoints));

        soaringForecastViewModel.getSoundingLocations().observe(this, soundingLocations ->
                forecastMapper.setSoundingLocations(soundingLocations));

        soaringForecastViewModel.getSelectedSoaringForecastImageSet().observe(this, soaringForecastImageSet -> {
            soaringForecastImageBinding.soaringForecastImageLocalTime.setText(soaringForecastImageSet.getLocalTime());
            forecastMapper.setGroundOverlay(soaringForecastImageSet.getBodyImage().getBitmap());
        });
    }

    /**
     * Set up recycler view with forecast models - gfs, rap, ...
     *
     * @param soaringForecastModels
     */
    private void setupSoaringForecastModelsRecyclerView(List<SoaringForecastModel> soaringForecastModels) {
        forecastModelrecyclerViewAdapter = new ForecastModelRecyclerViewAdapter(soaringForecastModels);
        setUpHorizontalRecyclerView(soaringForecastImageBinding.soaringForecastModelRecyclerView, forecastModelrecyclerViewAdapter);
    }

    private void setupModelForecastDateRecyclerView(List<ModelForecastDate> modelForecastDateList) {
        forecastDateRecyclerViewAdapter = new ForecastDateRecyclerViewAdapter(modelForecastDateList);
        setUpHorizontalRecyclerView(soaringForecastImageBinding.regionForecastDateRecyclerView, forecastDateRecyclerViewAdapter);
    }

    private void setupSoaringConditionRecyclerView(List<Forecast> forecasts) {
        soaringForecastRecyclerViewAdapter = new SoaringForecastRecyclerViewAdapter(forecasts);
        setUpHorizontalRecyclerView(soaringForecastImageBinding.soaringForecastRecyclerView, soaringForecastRecyclerViewAdapter);
        // TODO do better way to set selected
        soaringForecastRecyclerViewAdapter.setSelectedForecast(forecasts.get(1));
    }

    private void setUpHorizontalRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter recyclerViewAdapter) {
        recyclerView.setLayoutManager(
                new LinearLayoutManager(soaringForecastImageBinding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.rasp);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_menu, menu);
        clearTaskMenuItem = menu.findItem(R.id.forecast_menu_clear_task);
        if (clearTaskMenuItem != null) {
            clearTaskMenuItem.setVisible(showClearTaskMenuItem);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.forecast_menu_select_task:
                selectTask();
                return true;
            case R.id.forecast_menu_clear_task:
                forecastMapper.removeTaskTurnpoints();
                showClearTaskMenuItem = false;
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.forecast_menu_opacity_slider:
                displayOpacitySlider();
                return true;
            case R.id.forecast_menu_toggle_sounding_points:
                toggleSoundingPoints();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectTask() {
        TaskActivity.Builder builder = TaskActivity.Builder.getBuilder();
        builder.displayTaskList();
        startActivityForResult(builder.build(this.getContext()), 999);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle;
        if (requestCode == 999 && data != null) {
            if ((bundle = data.getExtras()) != null) {
                long taskId = bundle.getLong(Constants.SELECTED_TASK);
                if (taskId != 0) {
                    soaringForecastViewModel.getTask(taskId);
                    showClearTaskMenuItem = true;
                    getActivity().invalidateOptionsMenu();
                }
            }
        }
    }


    //@BindingAdapter(value={"android:onProgressChanged"})
    public void onOpacityValueChanged(SeekBar seekBar, int newOpacity, boolean fromUser) {
        forecastMapper.setForecastOverlayOpacity(newOpacity);
        appPreferences.setForecastOverlayOpacity(newOpacity);

    }

    private void toggleSoundingPoints() {
        soaringForecastViewModel.toggleSoundingPoints();
    }

    private void displayOpacitySlider() {
        soaringForecastImageBinding.soaringForecastSeekbarOpacity.setVisibility(
                soaringForecastImageBinding.soaringForecastSeekbarOpacity.getVisibility() == View.VISIBLE ?
                        View.GONE : View.VISIBLE);
    }

}
