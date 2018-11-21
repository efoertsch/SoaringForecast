package com.fisincorporated.soaringforecast.soaring.forecast;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.databinding.SoaringForecastBinding;
import com.fisincorporated.soaringforecast.messages.DisplaySoundingLocation;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;
import com.fisincorporated.soaringforecast.soaring.json.ModelForecastDate;
import com.fisincorporated.soaringforecast.task.TaskActivity;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class SoaringForecastFragmentSpinner extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    @Inject
    ForecastMapper forecastMapper;

    @Inject
    SoaringForecastDownloader soaringForecastDownloader;

    private SoaringForecastViewModelSpinner soaringForecastViewModelSpinner;
    private SoaringForecastBinding soaringForecastBinding;

    private int lastForecastModelPosition = -1;
    private int lastForecastDatePosition = -1;
    private int lastForecastPosition = -1;

    private MenuItem clearTaskMenuItem;

    // TODO replace with livedata in viewmodel
    private boolean showClearTaskMenuItem;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        soaringForecastViewModelSpinner = ViewModelProviders.of(this)
                .get(SoaringForecastViewModelSpinner.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setSoaringForecastDownloader(soaringForecastDownloader);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        soaringForecastBinding = DataBindingUtil.inflate(inflater
                , R.layout.soaring_forecast_rasp_spinners, container, false);
        soaringForecastBinding.setLifecycleOwner(this);
        soaringForecastBinding.setViewModel(soaringForecastViewModelSpinner);
        setupViews();
        setObservers();
        return soaringForecastBinding.getRoot();
    }

    private void setupViews() {
        // TODO how to get from binding
        forecastMapper.displayMap((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.soaring_forecast_map));
        // opacity not worth having it bound in viewModel and forwarding changes.
        forecastMapper.setForecastOverlayOpacity(appPreferences.getForecastOverlayOpacity());
        soaringForecastBinding.soaringForecastSeekbarOpacity.setProgress(appPreferences.getForecastOverlayOpacity());
    }

    //TODO - way to many observers - consolidate/simplify?
    private void setObservers() {
        // RASP models - GFS, NAM, ...
        soaringForecastViewModelSpinner.getSoaringForecastModelPosition().observe(this, newForecastModelPosition -> {
            if (lastForecastModelPosition != -1 && lastForecastModelPosition != newForecastModelPosition) {
                soaringForecastViewModelSpinner.setSoaringForecastModelPosition(newForecastModelPosition);
            }
            lastForecastModelPosition = newForecastModelPosition;
        });

        soaringForecastViewModelSpinner.getModelForecastDatesPosition().observe(this, newForecastDatePosition -> {
            if (lastForecastDatePosition != -1 && lastForecastDatePosition != newForecastDatePosition) {
                soaringForecastViewModelSpinner.setModelForecastDatesPosition(newForecastDatePosition);
                setMapLatLngBounds(soaringForecastViewModelSpinner.getSelectedModelForecastDate());
            }
            lastForecastDatePosition = newForecastDatePosition;
        });

        soaringForecastViewModelSpinner.getForecastPosition().observe(this, newForecastPosition -> {
            if (lastForecastPosition != -1 && lastForecastPosition != newForecastPosition) {
                soaringForecastViewModelSpinner.setForecastPosition(newForecastPosition);
            }
            lastForecastPosition = newForecastPosition;
        });

        // List of turnpoints for a selected task
        soaringForecastViewModelSpinner.getTaskTurnpoints().observe(this, taskTurnpoints -> {
            if (taskTurnpoints != null && taskTurnpoints.size() > 0) {
                displayTaskClearMenuItem(true);
            }
            forecastMapper.setTaskTurnpoints(taskTurnpoints);
        });

        // List of sounding locations available
        soaringForecastViewModelSpinner.getSoundingLocations().observe(this, soundingLocations ->
                forecastMapper.setSoundingLocations(soundingLocations));

        // Get Rasp bitmap for the date/time selected and pass to mapper
        soaringForecastViewModelSpinner.getSelectedSoaringForecastImageSet().observe(this, soaringForecastImageSet -> {
            if (soaringForecastImageSet != null) {
                soaringForecastBinding.soaringForecastImageLocalTime.setText(soaringForecastImageSet.getLocalTime());
                forecastMapper.setGroundOverlay(soaringForecastImageSet.getBodyImage().getBitmap());
                soaringForecastBinding.soaringForecastScaleImage.setImageBitmap(soaringForecastImageSet.getSideImage().getBitmap());
            } else {
                // Happens when user selects another forecast. Minimize confusion as to what is displayed by removing old forecast from map
                soaringForecastBinding.soaringForecastImageLocalTime.setText("");
                forecastMapper.setGroundOverlay(null);
                soaringForecastBinding.soaringForecastScaleImage.setImageBitmap(null);
            }
        });

        // Display sounding bitmap for the date/time selected and pass to mapper
        soaringForecastViewModelSpinner.getSoundingForecastImageSet().observe(this, soundingImageSet -> {
            soaringForecastBinding.soaringForecastImageLocalTime.setText(soundingImageSet.getLocalTime());
            soaringForecastBinding.soaringForecastSoundingImage.setImageBitmap(soundingImageSet.getBodyImage().getBitmap());
        });

        // Forecast bitmap opacity
        soaringForecastViewModelSpinner.getForecastOverlyOpacity().observe(this, forecastOverlyOpacity -> {
            forecastMapper.setForecastOverlayOpacity(forecastOverlyOpacity);
        });
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
        soaringForecastViewModelSpinner.stopImageAnimation();
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
                soaringForecastViewModelSpinner.setTaskId(-1);
                displayTaskClearMenuItem(false);
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
                    soaringForecastViewModelSpinner.getTask(taskId);
                }
            }
        }
    }

    private void displayTaskClearMenuItem(boolean visible) {
        showClearTaskMenuItem = visible;
        getActivity().invalidateOptionsMenu();
    }

    //------------ Bus messages (mainly from recycler view selections  ------------

    /**
     * Selected forecast type gfs, nam, ...
     *
     * @param soaringForecastModel
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SoaringForecastModel soaringForecastModel) {
        soaringForecastViewModelSpinner.setSelectedForecastModel(soaringForecastModel);
    }

    /**
     * Selected model date
     *
     * @param modelForecastDate
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ModelForecastDate modelForecastDate) {
        soaringForecastViewModelSpinner.setSelectedModelForecastDate(modelForecastDate);
    }

    /**
     * Select soaring forecast i.e wstar, bsratio (forecasts in raw/forecast_options)
     *
     * @param forecast
     */
    @Subscribe
    public void onMessageEvent(Forecast forecast) {
        soaringForecastViewModelSpinner.setSelectedSoaringForecast(forecast);

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
        soaringForecastViewModelSpinner.setSelectedSoundingLocation(displaySoundingLocation.getSoundingLocation());
    }


    private void toggleSoundingLocationsDisplay() {
        soaringForecastViewModelSpinner.toggleSoundingLocationDisplay();
    }

    private void displayOpacitySlider() {
        soaringForecastBinding.soaringForecastSeekbarLayout.setVisibility(
                soaringForecastBinding.soaringForecastSeekbarLayout.getVisibility() == View.VISIBLE ?
                        View.GONE : View.VISIBLE);

    }

}