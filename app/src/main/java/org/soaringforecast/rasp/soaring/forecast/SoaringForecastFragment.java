package org.soaringforecast.rasp.soaring.forecast;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.SoaringForecastBinding;
import org.soaringforecast.rasp.one800wxbrief.WxBriefRequestActivity;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.settings.SettingsActivity;
import org.soaringforecast.rasp.soaring.json.Forecast;
import org.soaringforecast.rasp.soaring.messages.DisplayLatLngForecast;
import org.soaringforecast.rasp.soaring.messages.DisplaySounding;
import org.soaringforecast.rasp.task.TaskActivity;
import org.soaringforecast.rasp.utils.StringUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
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
    StringUtils stringUtils;

    @Inject
    TurnpointBitmapUtils turnpointBitmapUtils;

    private SoaringForecastViewModel soaringForecastViewModel;
    private SoaringForecastBinding soaringForecastBinding;
    private ForecastTypeAdapter forecastTypeAdapter;
    private int lastForecastModelPosition = -1;
    private int lastForecastDatePosition = -1;
    private int lastForecastPosition = -1;
    private boolean showTaskSelectedMenuItems;
    private boolean displaySoundings = false;
    private boolean refreshForecastOrder = false;
    private boolean displaySUA = false;
    private AlphaAnimation opacitySliderFadeOut;
    private boolean displayTurnpoints;
    private Point soundingPoint;
    private SoundingZoomer soundingZoomer;
    private boolean firstSoundingToDisplay;

    public void onCreate(Bundle savedInstanceState) {
        //ElapsedTimeUtil.showElapsedTime(TAG, "startOnCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        soaringForecastViewModel = ViewModelProviders.of(this)
                .get(SoaringForecastViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
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
        forecastMapper.setContext(getContext())
                .displayMap((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.soaring_forecast_map))
                .setAppPreferences(appPreferences)
                .setTurnpointBitmapUtils(turnpointBitmapUtils);
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
            setShowTaskSelectedMenuItems(taskTurnpoints != null && taskTurnpoints.size() > 0);
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
            if (firstSoundingToDisplay && soundingPoint != null) {
                zoomOutSoundingView(soundingPoint);
                firstSoundingToDisplay = false;
            }
        });

        soaringForecastViewModel.getSoundingDisplay().observe(this, soundingDisplay -> {
            if (soundingPoint != null && !soundingDisplay) {
                    zoomInSoundingView();

            }
        });

        // Forecast region name for display of SUA (if any)
        soaringForecastViewModel.getSuaJSONObject().observe(this, suaJSONOject -> {
            forecastMapper.setSua(suaJSONOject);
        });

        // --- Turnpoints ------------------------------
        soaringForecastViewModel.getRegionTurnpoints().observe(this, turnpoints -> {
            displayTurnpoints = (turnpoints != null && turnpoints.size() > 0);
            forecastMapper.mapTurnpoints(turnpoints);
        });

        // Point forecast
        soaringForecastViewModel.getPointForecast().observe(this, pointForecast -> {
            forecastMapper.displayLatLngForecast(pointForecast);
        });

        //ElapsedTimeUtil.showElapsedTime(TAG, "end of startObservers()");

    }


    private void zoomOutSoundingView(Point soundingPoint) {
        if (soundingZoomer != null) {
            soundingZoomer.cancelZooming();
        }
        soundingZoomer = new SoundingZoomer(soundingPoint, soaringForecastBinding.soaringForecastSoundingLayout,
                soaringForecastBinding.soaringForecastSoundingImage
                , soaringForecastBinding.soaringForecastCloseSounding);
        soundingZoomer.zoomUpViewToDisplay();
    }

    private void zoomInSoundingView() {
        if (soundingZoomer != null) {
            soundingZoomer.cancelZooming();
            soundingZoomer.zoomDownToHide();
            soaringForecastBinding.soaringForecastImageLocalTime.setText("");
            soundingZoomer = null;
        }
        soundingPoint = null;
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
            clearTaskMenuItem.setEnabled(showTaskSelectedMenuItems);
        }
        MenuItem one800WxBriefMenuItem = menu.findItem(R.id.forecast_menu_1800wxbrief);
        if (one800WxBriefMenuItem != null) {
            one800WxBriefMenuItem.setEnabled(showTaskSelectedMenuItems);
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
                soaringForecastViewModel.setTaskId(-1);
                setShowTaskSelectedMenuItems(false);
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
            case R.id.wxbrief_task_notams:
                display1800WxNOTAMBrief();
                return true;
            case R.id.wxbrief_route_briefing:
                display1800WxRouteBrief();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void display1800WxNOTAMBrief(){
        WxBriefRequestActivity.Builder builder = WxBriefRequestActivity.Builder.getBuilder();
        builder.displayTaskNotams(soaringForecastViewModel.getTaskId());
        startActivity(builder.build(this.getContext()));
    }
    private void display1800WxRouteBrief(){
        WxBriefRequestActivity.Builder builder = WxBriefRequestActivity.Builder.getBuilder();
        builder.displayRouteBriefing(soaringForecastViewModel.getTaskId());
        startActivity(builder.build(this.getContext()));
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

    private void setShowTaskSelectedMenuItems(boolean visible) {
        showTaskSelectedMenuItems = visible;
        getActivity().invalidateOptionsMenu();
    }

    private void setMapLatLngBounds(LatLngBounds latLngBounds) {
        if (latLngBounds != null) {
            forecastMapper.setMapLatLngBounds(latLngBounds);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplaySounding displaySounding) {
        // The sounding location converted to  x,y coordinates later used to be starting point
        // for sounding display zoom in
        soundingPoint = displaySounding.getPoint();
        firstSoundingToDisplay = true;
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
    public void onMessageEvent(DisplayLatLngForecast latLngForecast) {
        soaringForecastViewModel.displayLatLngForecast(latLngForecast.getLatLng());
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
                                    soaringForecastViewModel.displaySua(displayOptionsChecked[i]);
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


