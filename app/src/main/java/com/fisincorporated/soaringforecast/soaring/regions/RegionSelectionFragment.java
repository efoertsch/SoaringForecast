package com.fisincorporated.soaringforecast.soaring.regions;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

    private LinearLayout.LayoutParams radioButtonParams;

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
        radioButtonParams = getRadioButtonParams();
        synchronized (regionList) {
            for (Region region : regionList) {
                radioButton = new RadioButton(getContext());
                radioButton.setText(region.getName());
                radioButton.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                radioButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                radioButton.setTextSize(getResources().getDimension(R.dimen.text_size_small));
                //radioButton.setTypeface(null, Typeface.BOLD);
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                    radioButton.setTextAppearance(getResources()
//                            .getIdentifier("textAppearanceMedium", "style", getActivity().getPackageName()));
//                } else {
//                    radioButton.setTextAppearance(getContext(),getResources()
//                            .getIdentifier("textAppearanceMedium", "style", getActivity().getPackageName()));
//                }
                radioButton.setLayoutParams(radioButtonParams);
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
            RadioButton checkedRadioButton =  group.findViewById(checkedId);
            // If the radiobutton that has changed in check state is now checked...
            if (checkedRadioButton != null && checkedRadioButton.isChecked()) {
                regionSelectionViewModel.setSoaringForecastRegion(checkedRadioButton.getText().toString());
            }
        };
    }

    private LinearLayout.LayoutParams getRadioButtonParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.START;
        int verticalMargin = (int)getResources().getDimension(R.dimen.margin_double);
        int horizonatlMargin = (int)getResources().getDimension(R.dimen.margin_standard);
        layoutParams.setMargins(horizonatlMargin, verticalMargin,horizonatlMargin, verticalMargin);
        return layoutParams;
    }
}
