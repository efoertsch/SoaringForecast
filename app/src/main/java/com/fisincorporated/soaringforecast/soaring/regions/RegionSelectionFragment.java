package com.fisincorporated.soaringforecast.soaring.regions;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.databinding.RegionSelectionBinding;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastDownloader;
import com.fisincorporated.soaringforecast.soaring.json.Region;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class RegionSelectionFragment extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    @Inject
    SoaringForecastDownloader soaringForecastDownloader;

    private String selectedRegion;

    private RegionSelectionViewModel regionSelectionViewModel;
    private RegionSelectionBinding regionSelectionBinding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        regionSelectionViewModel = ViewModelProviders.of(this)
                .get(RegionSelectionViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setSoaringForecastDownloader(soaringForecastDownloader);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        regionSelectionBinding = DataBindingUtil.inflate(inflater
                , R.layout.soaring_region_selection_fragment, container, false);
        regionSelectionBinding.setLifecycleOwner(this);
        regionSelectionBinding.setViewModel(regionSelectionViewModel);
        selectedRegion = regionSelectionViewModel.getSelectedRegion();
        setObservers();
        return regionSelectionBinding.getRoot();
    }

    private void setObservers() {
        regionSelectionViewModel.getRegions().observe(this, regionList -> {
            listRegions(regionList);
        });
    }

    private void listRegions(List<Region> regionList) {
        RadioButton radioButton;
        RadioGroup regionGroup = regionSelectionBinding.soaringRegionsButtonGroup;
        regionGroup.setOnCheckedChangeListener(getRegionButtonGroupListener());
        regionGroup.clearCheck();
        regionGroup.removeAllViews();
        regionGroup.setOrientation(RadioGroup.HORIZONTAL);
        synchronized (regionList) {
            for (Region region : regionList) {
                radioButton = new RadioButton(getContext());
                radioButton.setText(region.getName());
                regionGroup.addView(radioButton);
                if (region.getName().equals(selectedRegion)) {
                    radioButton.setChecked(true);
                }
            }
        }
    }

    /**
     * Seems easiest to have listener here rather than do databinding
     * @return
     */
    private RadioGroup.OnCheckedChangeListener getRegionButtonGroupListener() {
        return (group, checkedId) -> {
            RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
            // If the radiobutton that has changed in check state is now checked...
            if (checkedRadioButton.isChecked()) {
                regionSelectionViewModel.setSoaringForecastRegion(checkedRadioButton.getText().toString());
            }
        };
    }
}
