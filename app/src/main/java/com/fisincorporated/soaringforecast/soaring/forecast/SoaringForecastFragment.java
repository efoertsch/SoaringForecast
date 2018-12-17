package com.fisincorporated.soaringforecast.soaring.forecast;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.fisincorporated.soaringforecast.messages.DisplaySounding;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.settings.SettingsActivity;
import com.fisincorporated.soaringforecast.task.TaskActivity;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private SoaringForecastBinding soaringForecastBinding;

    private int lastForecastModelPosition = -1;
    private int lastForecastDatePosition = -1;
    private int lastForecastPosition = -1;

    private boolean showClearTaskMenuItem;

    private MenuItem soundingsMenuItem;
    private boolean checkSoundsMenuItem = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        soaringForecastViewModel = ViewModelProviders.of(this)
                .get(SoaringForecastViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setSoaringForecastDownloader(soaringForecastDownloader);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        soaringForecastBinding = DataBindingUtil.inflate(inflater
                , R.layout.soaring_forecast_rasp_spinners, container, false);
        soaringForecastBinding.setLifecycleOwner(getActivity());
        soaringForecastBinding.setViewModel(soaringForecastViewModel);
        setupViews();
        setObservers();
        return soaringForecastBinding.getRoot();
    }

    private void setupViews() {
        // TODO how to get from binding - soaringForecastBinding.soaringForecastMap - need to get as supportMapFragment
        forecastMapper.displayMap((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.soaring_forecast_map));
        // opacity not worth having it bound in viewModel and forwarding changes.
        forecastMapper.setForecastOverlayOpacity(appPreferences.getForecastOverlayOpacity());
        soaringForecastBinding.soaringForecastSeekbarOpacity.setProgress(appPreferences.getForecastOverlayOpacity());
    }

    //TODO - lots of observers - consolidate/simplify?
    private void setObservers() {
        // RASP models - GFS, NAM, ...
        soaringForecastViewModel.getModelPosition().observe(this, newForecastModelPosition -> {
            if (newForecastModelPosition != null) {
                if (lastForecastModelPosition != -1 && lastForecastModelPosition != newForecastModelPosition) {
                    soaringForecastViewModel.setModelPosition(newForecastModelPosition);
                }
                lastForecastModelPosition = newForecastModelPosition;
            }
        });

        // Dates for the selected model
        soaringForecastViewModel.getModelForecastDatePosition().observe(this, newForecastDatePosition -> {
            if (newForecastDatePosition != null) {
                if (lastForecastDatePosition != -1 && lastForecastDatePosition != newForecastDatePosition) {
                    soaringForecastViewModel.setModelForecastDatePosition(newForecastDatePosition);
                }
                lastForecastDatePosition = newForecastDatePosition;
            }
        });

        soaringForecastViewModel.getForecastPosition().observe(this, newForecastPosition -> {
            if (newForecastPosition != null) {
                if (lastForecastPosition != -1 && lastForecastPosition != newForecastPosition) {
                    soaringForecastViewModel.setForecastPosition(newForecastPosition);
                }
                lastForecastPosition = newForecastPosition;
            }
        });

        // Set the corners of the google map
        soaringForecastViewModel.getRegionLatLngBounds().observe(this, regionLatngBounds -> {
            setMapLatLngBounds(regionLatngBounds);
        });

        // List of turnpoints for a selected task
        soaringForecastViewModel.getTaskTurnpoints().observe(this, taskTurnpoints -> {
            if (taskTurnpoints != null && taskTurnpoints.size() > 0) {
                displayTaskClearMenuItem(true);
            } else {
                displayTaskClearMenuItem(false);
            }
            forecastMapper.setTaskTurnpoints(taskTurnpoints);
        });

        // List of soundings available
        soaringForecastViewModel.getSoundings().observe(this, soundingList -> {
            checkSoundsMenuItem = (soundingList != null && soundingList.size() > 0);
            forecastMapper.setSoundings(soundingList);
            if (soundingsMenuItem != null) {
                soundingsMenuItem.setChecked(checkSoundsMenuItem);
            }
        });

        // Get Rasp bitmap for the date/time selected and pass to mapper
        soaringForecastViewModel.getSelectedSoaringForecastImageSet().observe(this, soaringForecastImageSet -> {
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
        soaringForecastViewModel.getSoundingForecastImageSet().observe(this, soundingImageSet -> {
            soaringForecastBinding.soaringForecastImageLocalTime.setText(soundingImageSet.getLocalTime());
            soaringForecastBinding.soaringForecastSoundingImage.setImageBitmap(soundingImageSet.getBodyImage().getBitmap());
        });

        // Forecast bitmap opacity
        soaringForecastViewModel.getForecastOverlyOpacity().observe(this, forecastOverlyOpacity -> {
            forecastMapper.setForecastOverlayOpacity(forecastOverlyOpacity);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.rasp);
        EventBus.getDefault().register(this);
        soaringForecastViewModel.checkForRegionChange();
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
        MenuItem clearTaskMenuItem = menu.findItem(R.id.forecast_menu_clear_task);
        if (clearTaskMenuItem != null) {
            clearTaskMenuItem.setVisible(showClearTaskMenuItem);
        }

        soundingsMenuItem = menu.findItem(R.id.forecast_menu_toggle_sounding_points);
        soundingsMenuItem.setChecked(checkSoundsMenuItem);
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
                soaringForecastViewModel.setTaskId(-1);
                displayTaskClearMenuItem(false);
                return true;
            case R.id.forecast_menu_opacity_slider:
                displayOpacitySlider();
                return true;
            case R.id.forecast_menu_toggle_sounding_points:
                displaySoundings(!soundingsMenuItem.isChecked());
                return true;
            case R.id.forecast_menu_select_regions:
                displayRegionSelections();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayRegionSelections() {
        SettingsActivity.Builder builder = SettingsActivity.Builder.getBuilder();
        builder.displaySelectRegion();
        startActivity(builder.build(this.getContext()));
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
                }
            }
        }
    }

    private void displayTaskClearMenuItem(boolean visible) {
        showClearTaskMenuItem = visible;
        getActivity().invalidateOptionsMenu();
    }


    private void setMapLatLngBounds(LatLngBounds latLngBounds) {
        if (latLngBounds != null) {
            forecastMapper.setMapLatLngBounds(latLngBounds);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplaySounding displaySounding) {
        soaringForecastViewModel.setSelectedSounding(displaySounding.getSounding());
    }


    private void displaySoundings(boolean displaySoundings) {
        soaringForecastViewModel.displaySoundings(displaySoundings);
    }

    private void displayOpacitySlider() {
        soaringForecastBinding.soaringForecastSeekbarLayout.setVisibility(
                soaringForecastBinding.soaringForecastSeekbarLayout.getVisibility() == View.VISIBLE ?
                        View.GONE : View.VISIBLE);

    }

}
