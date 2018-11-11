package com.fisincorporated.soaringforecast.satellite.noaa;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.databinding.NoaaSatelliteImageBinding;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.satellite.SatelliteImageDownloader;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImage;

import org.cache2k.Cache;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class NoaaSatelliteImageFragment extends DaggerFragment {

    private NoaaSatelliteImageBinding binding;

    private NoaaSatelliteViewModel satelliteViewModel;

    @Inject
    public SatelliteImageDownloader satelliteImageDownloader;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AppRepository appRepository;

    public static NoaaSatelliteImageFragment newInstance() {
        NoaaSatelliteImageFragment noaaSatelliteImageFragment = new NoaaSatelliteImageFragment();

        return noaaSatelliteImageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        satelliteViewModel = ViewModelProviders.of(this).get(NoaaSatelliteViewModel.class);
    }


    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.satellite_image_fragment_noaa, container, false);
        satelliteViewModel.setAppPreferences(appPreferences)
                .setAppRepository(appRepository)
                .setSatelliteImageCache(satelliteImageCache)
                .setSatelliteImageDownloader(satelliteImageDownloader);
        binding.setLifecycleOwner(this);
        binding.setViewModel(satelliteViewModel);
        setup();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.noaa_satellite);

    }

    private void setup(){
        satelliteViewModel.getSatelliteBitmap().observe(this, satelliteBitmap ->{
           binding.satelliteImageImageView.setImageBitmap(satelliteBitmap);
        });

        satelliteViewModel.setup();
    }



    //-------- Binding for regions (CONUS, Albany, ..
//    @BindingAdapter(value = {"selectedRegionValue", "selectedRegionValueAttrChanged"}, requireAll = false)
//    public static void bindSpinnerData(Spinner spinner, SatelliteRegion newSelectedValue, final InverseBindingListener newSatelliteAttrChanged) {
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                newSatelliteAttrChanged.onChange();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//        if (newSelectedValue != null) {
//            int pos = spinner.getSelectedItemPosition();
//            spinner.setSelection(pos, true);
//        }
//    }
//
//    @InverseBindingAdapter(attribute = "selectedRegionValue", event = "selectedRegionValueAttrChanged")
//    public static SatelliteRegion captureSelectedValue(Spinner spinner) {
//        return (SatelliteRegion) spinner.getSelectedItem();
//    }
//
//
//
//    // ---- Binding for satellite image type - Visible, Water Vapor,...
//    @BindingAdapter(value = {"selectedImageType", "selectedImageTypeAttrChanged"}, requireAll = false)
//    public static void bindImageTypeSpinnerData(Spinner spinner, SatelliteImageType newSelectedValue, final InverseBindingListener newSatelliteAttrChanged) {
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                newSatelliteAttrChanged.onChange();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//        if (newSelectedValue != null) {
//            int pos = spinner.getSelectedItemPosition();
//            spinner.setSelection(pos, true);
//        }
//    }
//
//    @InverseBindingAdapter(attribute = "selectedImageType", event = "selectedImageTypeAttrChanged")
//    public static SatelliteImageType captureSelectedImageTypeValue(Spinner spinner) {
//        return (SatelliteImageType) spinner.getSelectedItem();
//    }
}
