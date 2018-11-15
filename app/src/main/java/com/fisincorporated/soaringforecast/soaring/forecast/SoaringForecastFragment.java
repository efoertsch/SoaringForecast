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

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.databinding.SoaringForecastImageBinding;
import com.fisincorporated.soaringforecast.messages.DisplaySoundingLocation;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.soaring.forecast.adapters.ForecastDateRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.soaring.forecast.adapters.ForecastModelRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.soaring.forecast.adapters.SoaringForecastRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;
import com.fisincorporated.soaringforecast.soaring.json.ModelForecastDate;
import com.fisincorporated.soaringforecast.task.TaskActivity;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class SoaringForecastFragment extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    @Inject
    ForecastMapper forecastMapper;

    @Inject
    SoaringForecastDownloader soaringForecastDownloader;

    private SoaringForecastViewModel soaringForecastViewModel;
    private SoaringForecastImageBinding soaringForecastImageBinding;
    private ForecastModelRecyclerViewAdapter forecastModelrecyclerViewAdapter;
    private ForecastDateRecyclerViewAdapter forecastDateRecyclerViewAdapter;
    private SoaringForecastRecyclerViewAdapter soaringForecastRecyclerViewAdapter;

    private MenuItem clearTaskMenuItem;

    // TODO replace with livedata in viewmodel
    private boolean showClearTaskMenuItem;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        soaringForecastViewModel = ViewModelProviders.of(this)
                .get(SoaringForecastViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setSoaringForecastDownloader(soaringForecastDownloader);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        soaringForecastImageBinding = DataBindingUtil.inflate(inflater
                , R.layout.soaring_forecast_rasp, container, false);
        soaringForecastImageBinding.setLifecycleOwner(this);
        soaringForecastImageBinding.setViewModel(soaringForecastViewModel);
        setupViews();
        setObservers();
        return soaringForecastImageBinding.getRoot();
    }

    private void setupViews() {
        // TODO how to get from binding
        forecastMapper.displayMap((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.soaring_forecast_map));
        setupSoaringForecastModelsRecyclerView(null);
        setupSoaringConditionRecyclerView(null);
        setupModelForecastDateRecyclerView(null);
        // opacity not worth having it bound in viewModel and forwarding changes.
        forecastMapper.setForecastOverlayOpacity(appPreferences.getForecastOverlayOpacity());
        soaringForecastImageBinding.soaringForecastSeekbarOpacity.setProgress(appPreferences.getForecastOverlayOpacity());
    }

    //TODO - way to many observers - consolidate/simplify?
    private void setObservers() {
        // RASP models - GFS, NAM, ...
        soaringForecastViewModel.getSoaringForecastModels().observe(this, soaringForecastModels -> {
            //TODO create preference for model to use for first display
            forecastModelrecyclerViewAdapter.setItems(soaringForecastModels);
        });

        // Selected RASP model - GFS, NAM, ... Fired first time selected assigned and subsequent user clicks
        soaringForecastViewModel.getSelectedSoaringForecastModel().observe(this, soaringForecastModel ->
                forecastModelrecyclerViewAdapter.setSelectedItem(soaringForecastModel));

        // List of dates for which rasp forecasts are available for the selected model
        soaringForecastViewModel.getModelForecastDates().observe(this, modelForecastDates ->
                forecastDateRecyclerViewAdapter.setItems(modelForecastDates));

        // First time selected date for the RASP model or subsequent user clicked date
        // Data also has map bounds
        soaringForecastViewModel.getSelectedModelForecastDate().observe(this, selectedForecastDate -> {
            forecastDateRecyclerViewAdapter.setSelectedItem(selectedForecastDate);
            setMapLatLngBounds(selectedForecastDate);
        });

        // Types of Rasp forecasts available - thermal updraft, bouncy/shear, cloud cover, ..
        soaringForecastViewModel.getForecasts().observe(this, forecasts -> {
                    soaringForecastRecyclerViewAdapter.setItems(forecasts);
                }
        );

        // First time assigned or user clicked forecast (wstar,...) to be displayed
        soaringForecastViewModel.getSelectedSoaringForecast().observe(this, forecast -> {
            soaringForecastRecyclerViewAdapter.setSelectedItem(forecast);
        });


        // List of turnpoints for a selected task
        soaringForecastViewModel.getTaskTurnpoints().observe(this, taskTurnpoints ->
                forecastMapper.setTaskTurnpoints(taskTurnpoints));

        // List of sounding locations available
        soaringForecastViewModel.getSoundingLocations().observe(this, soundingLocations ->
                forecastMapper.setSoundingLocations(soundingLocations));

        // Get Rasp bitmap for the date/time selected and pass to mapper
        soaringForecastViewModel.getSelectedSoaringForecastImageSet().observe(this, soaringForecastImageSet -> {
            if (soaringForecastImageSet != null) {
                soaringForecastImageBinding.soaringForecastImageLocalTime.setText(soaringForecastImageSet.getLocalTime());
                forecastMapper.setGroundOverlay(soaringForecastImageSet.getBodyImage().getBitmap());
                soaringForecastImageBinding.soaringForecastScaleImage.setImageBitmap(soaringForecastImageSet.getSideImage().getBitmap());
            } else {
                // Happens when user selects another forecast. Minimize confusion as to what is displayed by removing old forecast from map
                soaringForecastImageBinding.soaringForecastImageLocalTime.setText("");
                forecastMapper.setGroundOverlay(null);
                soaringForecastImageBinding.soaringForecastScaleImage.setImageBitmap(null);
            }
        });

        // Display sounding bitmap for the date/time selected and pass to mapper
        soaringForecastViewModel.getSoundingForecastImageSet().observe(this, soundingImageSet -> {
            soaringForecastImageBinding.soaringForecastImageLocalTime.setText(soundingImageSet.getLocalTime());
            soaringForecastImageBinding.soaringForecastSoundingImage.setImageBitmap(soundingImageSet.getBodyImage().getBitmap());
        });

        // Forecast bitmap opacity
        soaringForecastViewModel.getForecastOverlyOpacity().observe(this, forecastOverlyOpacity ->{
            forecastMapper.setForecastOverlayOpacity(forecastOverlyOpacity);
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
    }

    private void setUpHorizontalRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter recyclerViewAdapter) {
        recyclerView.setLayoutManager(
                new LinearLayoutManager(soaringForecastImageBinding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.rasp);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        soaringForecastViewModel.stopImageAnimation();
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
                toggleSoundingLocationsDisplay();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectTask() {
        TaskActivity.Builder builder = TaskActivity.Builder.getBuilder();
        builder.displayTaskList().enableClickTask(true);
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

    //------------ Bus messages (mainly from recycler view selections  ------------

    /**
     * Selected forecast type gfs, nam, ...
     *
     * @param soaringForecastModel
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SoaringForecastModel soaringForecastModel) {
        soaringForecastViewModel.setSelectedForecastModel(soaringForecastModel);
    }

    /**
     * Selected model date
     *
     * @param modelForecastDate
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ModelForecastDate modelForecastDate) {
        soaringForecastViewModel.setSelectedModelForecastDate(modelForecastDate);
    }

    /**
     * Select soaring forecast i.e wstar, bsratio (forecasts in raw/forecast_options)
     *
     * @param forecast
     */
    @Subscribe
    public void onMessageEvent(Forecast forecast) {
        soaringForecastViewModel.setSelectedSoaringForecast(forecast);

    }

    private void setMapLatLngBounds(ModelForecastDate modelForecastDate) {
        if (modelForecastDate != null) {
            forecastMapper.setMapLatLngBounds(
                    new LatLngBounds(modelForecastDate.getGpsLocationAndTimes().getSouthWestLatLng()
                            , modelForecastDate.getGpsLocationAndTimes().getNorthEastLatLng()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplaySoundingLocation displaySoundingLocation) {
        soaringForecastViewModel.setSelectedSoundingLocation(displaySoundingLocation.getSoundingLocation());
    }


    private void toggleSoundingLocationsDisplay() {
        soaringForecastViewModel.toggleSoundingLocationDisplay();
    }

    private void displayOpacitySlider() {
        soaringForecastImageBinding.soaringForecastSeekbarLayout.setVisibility(
                soaringForecastImageBinding.soaringForecastSeekbarLayout.getVisibility() == View.VISIBLE ?
                        View.GONE : View.VISIBLE);

    }

}
