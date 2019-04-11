package org.soaringforecast.rasp.soaring.forecast;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.SeekBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.SoaringForecastBinding;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.settings.SettingsActivity;
import org.soaringforecast.rasp.soaring.json.Forecast;
import org.soaringforecast.rasp.soaring.messages.DisplayPointForecast;
import org.soaringforecast.rasp.soaring.messages.DisplaySounding;
import org.soaringforecast.rasp.task.TaskActivity;
import org.soaringforecast.rasp.utils.StringUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import timber.log.Timber;

public class SoaringForecastFragment extends DaggerFragment {

    private static final String TAG = SoaringForecastFragment.class.getSimpleName();
    private static final int SELECT_TASK = 999;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    @Inject
    ForecastMapper forecastMapper;

    @Inject
    SoaringForecastDownloader soaringForecastDownloader;

    @Inject
    StringUtils stringUtils;

    private SoaringForecastViewModel soaringForecastViewModel;
    private SoaringForecastBinding soaringForecastBinding;
    private ForecastTypeAdapter forecastTypeAdapter;
    private int lastForecastModelPosition = -1;
    private int lastForecastDatePosition = -1;
    private int lastForecastPosition = -1;
    private boolean showClearTaskMenuItem;
    private boolean displaySoundings = false;
    private boolean refreshForecastOrder = false;
    private boolean displaySUA = false;
    private AlphaAnimation opacitySliderFadeOut;
    private String lastRegionName;
    private boolean displayTurnpoints;

    public void onCreate(Bundle savedInstanceState) {
        //ElapsedTimeUtil.showElapsedTime(TAG, "startOnCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        soaringForecastViewModel = ViewModelProviders.of(this)
                .get(SoaringForecastViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setSoaringForecastDownloader(soaringForecastDownloader)
                .setStringUtils(stringUtils);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //ElapsedTimeUtil.showElapsedTime(TAG, "startOnCreateView()");
        soaringForecastBinding = DataBindingUtil.inflate(inflater
                , R.layout.fragment_forecast_rasp_spinners, container, false);
        soaringForecastBinding.setLifecycleOwner(getActivity());
        soaringForecastBinding.setViewModel(soaringForecastViewModel);

        forecastTypeAdapter = new ForecastTypeAdapter(new ArrayList<>(), getContext());
        soaringForecastBinding.setSpinAdapterForecast(forecastTypeAdapter);

        setupViews();
        // attempt to speed up initial screen display
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setObservers();
            }
        });
        return soaringForecastBinding.getRoot();
    }

    private void setupViews() {
        // TODO how to get from binding - soaringForecastBinding.soaringForecastMap - need to get as supportMapFragment
        forecastMapper.setContext(getContext()).displayMap((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.soaring_forecast_map));
        // opacity not worth having it bound in viewModel and forwarding changes.
        forecastMapper.setForecastOverlayOpacity(appPreferences.getForecastOverlayOpacity());
        soaringForecastBinding.soaringForecastSeekbarOpacity.setProgress(soaringForecastViewModel.getForecastOverlyOpacity());
        soaringForecastBinding.soaringForecastSeekbarOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                stopOpacitySliderFadeOut();
                forecastMapper.setForecastOverlayOpacity(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopOpacitySliderFadeOut();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                soaringForecastViewModel.setForecastOverlayOpacity(seekBar.getProgress());
                startOpacitySliderFadeOut();
            }
        });

        displaySUA = appPreferences.getDisplaySua();
    }

    //TODO - lots of observers - consolidate/simplify?
    private void setObservers() {
        //ElapsedTimeUtil.showElapsedTime(TAG, "startObservers()");
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

        // Forecasts
        soaringForecastViewModel.getForecasts().observe(this, forecasts -> {
            forecastTypeAdapter.clear();
            forecastTypeAdapter.addAll(forecasts);
        });

        // Databinding doesn't call set... directly so need this observer to kick off forecast change
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
            displaySoundings = (soundingList != null && soundingList.size() > 0);
            forecastMapper.setSoundings(soundingList);

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

        // Forecast region name for display of SUA (if any)
        soaringForecastViewModel.getSuaRegionName().observe(this, regionName -> {
            lastRegionName = regionName;
            displaySUA = appPreferences.getDisplaySua();
            if (displaySUA) {
                displaySuaOnMap(lastRegionName);
            }

            //getActivity().invalidateOptionsMenu();
        });

        // --- Turnpoints ------------------------------
        soaringForecastViewModel.getRegionTurnpoints().observe(this, turnpoints -> {
            displayTurnpoints = (turnpoints != null && turnpoints.size() > 0);
            forecastMapper.mapTurnpoints(turnpoints);
        });

        // Point forecast
        soaringForecastViewModel.getPointForecast().observe(this, pointForecast -> {
            forecastMapper.displayPointForecast(pointForecast);
        });

        //ElapsedTimeUtil.showElapsedTime(TAG, "end of startObservers()");

    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        getActivity().setTitle(R.string.rasp);
        EventBus.getDefault().register(this);
        soaringForecastViewModel.checkForChanges();
        if (refreshForecastOrder) {
            refreshForecastOrder = false;
            soaringForecastViewModel.reloadForecasts();
        }
        //ElapsedTimeUtil.showElapsedTime(TAG, "end of onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        soaringForecastViewModel.stopImageAnimation();
    }


    // Note this occurs *after* onResume
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Timber.d("onCreateOptionsMenu");
        inflater.inflate(R.menu.forecast_menu, menu);
        MenuItem clearTaskMenuItem = menu.findItem(R.id.forecast_menu_clear_task);
        if (clearTaskMenuItem != null) {
            clearTaskMenuItem.setVisible(showClearTaskMenuItem);
        }

//        soundingsMenuItem = menu.findItem(R.id.forecast_menu_toggle_sounding_points);
//        soundingsMenuItem.setChecked(displaySoundings);
//
//        suaMenuItem = menu.findItem(R.id.forecast_menu_display_sua);
//        if (suaMenuItem != null) {
//            suaMenuItem.setChecked(displaySUA);
//        }
//
//        turnpointsMenuItem = menu.findItem(R.id.forecast_menu_display_turnpoints);
//        if (turnpointsMenuItem != null) {
//            turnpointsMenuItem.setChecked(displayTurnpoints);
//        }

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
            case R.id.forecast_menu_display_options:
                showDisplayOptionsDialog();
                return true;
            case R.id.forecast_menu_select_regions:
                displayRegionSelections();
                return true;
            case R.id.forecast_menu_order_forecasts:
                displayForecastOrderFragment();
                return true;
            case R.id.forecast_menu_map_topo:
                forecastMapper.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            case R.id.forecast_menu_map_satellite:
                forecastMapper.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.forecast_menu_map_roadmap:
                forecastMapper.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.forecast_menu_map_hybrid:
                forecastMapper.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void displaySua(boolean displaySua) {
        Timber.d("displaySua() suaMenuItem.isChecked(): %1$s", displaySoundings);
        // When menu clicked to be checked it, menuItem still unchecked when you get here
        // Likewise when clicked and is checked, it is still checked when you get here.
        if (!displaySua) {
            if (forecastMapper != null) {
                forecastMapper.removeSuaFromMap();
            }
        } else {
            if (lastRegionName == null) {
                // sua not displayed by settings so need to get region from viewmodel first
                if (soaringForecastViewModel.getSuaRegionName() != null) {
                    lastRegionName = soaringForecastViewModel.getSuaRegionName().getValue();
                }
            }
            displaySuaOnMap(lastRegionName);

        }
    }

    // Drawing SUA cpu intensive so doing this way to make app more responsive
    private void displaySuaOnMap(final String regionName) {
        new Thread(() -> getActivity().runOnUiThread(((Runnable) () -> forecastMapper.setSuaRegionName(regionName)))).start();
    }


    private void displayForecastOrderFragment() {
        refreshForecastOrder = true;
        SettingsActivity.Builder builder = SettingsActivity.Builder.getBuilder();
        builder.displayOrderedForecasts();
        startActivity(builder.build(this.getContext()));
    }

    private void displayRegionSelections() {
        SettingsActivity.Builder builder = SettingsActivity.Builder.getBuilder();
        builder.displaySelectRegion();
        startActivity(builder.build(this.getContext()));
    }

    private void selectTask() {
        TaskActivity.Builder builder = TaskActivity.Builder.getBuilder();
        builder.displayTaskList().enableClickTask(true);
        startActivityForResult(builder.build(this.getContext()), SELECT_TASK);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_TASK) {
            // Always check for task, user might have reordered turnpoints under same task id
            // so always check/redisplay
            soaringForecastViewModel.checkIfToDisplayTask();
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Forecast forecast) {
        BottomSheetBehavior bsb = BottomSheetBehavior.from(soaringForecastBinding.soaringForecastBottomSheet);
        if (bsb.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplayPointForecast pointForecast) {
        soaringForecastViewModel.displayPointForecast(pointForecast.getLatLng());
    }

    // ---- Opacity for forecast overlay -----------------------
    private void displayOpacitySlider() {
        soaringForecastBinding.soaringForecastSeekbarLayout.setVisibility(View.VISIBLE);
        startOpacitySliderFadeOut();
    }

    private void startOpacitySliderFadeOut() {
        stopOpacitySliderFadeOut();
        animateOpacitySliderFadeOut();

    }

    private void stopOpacitySliderFadeOut() {
        if (opacitySliderFadeOut != null) {
            opacitySliderFadeOut.cancel();
        }
    }

    public void animateOpacitySliderFadeOut() {
        opacitySliderFadeOut = new AlphaAnimation(1.0f, 0.0f);
        opacitySliderFadeOut.setDuration(1000);
        opacitySliderFadeOut.setStartOffset(5000);
        opacitySliderFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                soaringForecastBinding.soaringForecastSeekbarLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        soaringForecastBinding.soaringForecastSeekbarLayout.startAnimation(opacitySliderFadeOut);
    }

    //--- Display Soundings/SUA/Turnpoints dialog --------------------------------------------------
    private void showDisplayOptionsDialog() {
        final String[] displayOptions = {getString(R.string.forecast_menu_toggle_sounds),
                getString(R.string.sua), getString(R.string.turnpoints_no_hyphen)};
        final boolean[] displayOptionsChecked = {displaySoundings, displaySUA, displayTurnpoints};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Display");
        builder.setMultiChoiceItems(displayOptions, displayOptionsChecked,
                (dialog, item, isChecked) -> displayOptionsChecked[item] = isChecked);
        builder.setPositiveButton(R.string.ok,
                (dialog, which) -> {
                    for (int i = 0; i < displayOptionsChecked.length; ++i) {
                        switch (i) {
                            case 0:
                                displaySoundings = soaringForecastViewModel.displaySoundings(displayOptionsChecked[i]);
                                break;
                            case 1:
                                if (displaySUA != displayOptionsChecked[i]) {
                                    displaySua(displayOptionsChecked[i]);
                                    displaySUA = displayOptionsChecked[i];
                                }
                                break;
                            case 2:
                                if (displayTurnpoints != displayOptionsChecked[i]) {
                                    soaringForecastViewModel.displayTurnpoints(displayOptionsChecked[i]);
                                    displayTurnpoints = displayOptionsChecked[i];
                                }
                                break;
                        }
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                (dialog, which) -> {
                    //dismiss - no changes
                });
        AlertDialog displayOptionsMenu = builder.create();
        displayOptionsMenu.show();

    }
}


