package com.fisincorporated.aviationweather.soaring.forecast;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.Constants;
import com.fisincorporated.aviationweather.messages.SnackbarMessage;
import com.fisincorporated.aviationweather.task.TaskActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class SoaringForecastFragment extends DaggerFragment {

    public static final String MASTER_FORECAST_FRAGMENT = "MASTER_FORECAST_FRAGMENT";

    @Inject
    SoaringForecastDisplay soaringForecastDisplay;

    public SoaringForecastFragment() {
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.soaring_forecast_rasp, container, false);
        soaringForecastDisplay.setView(this, view);
        checkForGooglePlayServices();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.rasp);
        soaringForecastDisplay.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        soaringForecastDisplay.onPause();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.forecast_menu_select_task:
                selectTask();
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
        if (requestCode == 999) {
            if ((bundle = data.getExtras()) != null) {
                long taskId = bundle.getLong(Constants.SELECTED_TASK);
                if (taskId != 0) {
                    EventBus.getDefault().post(new SnackbarMessage("Selected Task:" + taskId));
                    soaringForecastDisplay.displayTask(taskId);
                }
            }
        }
    }

    private void toggleSoundingPoints() {
        soaringForecastDisplay.toggleSoundingPoints();
    }

    private void displayOpacitySlider() {
        soaringForecastDisplay.displayOpacitySlider();
    }

    private void checkForGooglePlayServices() {
        int GooglePlayAvailableCode;
        GooglePlayAvailableCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        if (ConnectionResult.SUCCESS != GooglePlayAvailableCode) {
            Toast.makeText(getContext(), "GooglePlayServices not available",
                    Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }
}
