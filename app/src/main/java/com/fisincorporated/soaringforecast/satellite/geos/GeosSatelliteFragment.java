package com.fisincorporated.soaringforecast.satellite.geos;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.databinding.GeosSatelliteImageBinding;

public class GeosSatelliteFragment extends Fragment {

    private static final String geosGif = "https://cdn.star.nesdis.noaa.gov/GOES16/ABI/GIFS/GOES16-NE-GEOCOLOR-600x600.gif";
    private static final String currentGeos = "https://cdn.star.nesdis.noaa.gov/GOES16/ABI/SECTOR/ne/GEOCOLOR/600x600.jpg";

    private GeosSatelliteImageBinding binding;
    private GeosViewModel geosViewModel;

    public static GeosSatelliteFragment newInstance() {
        GeosSatelliteFragment geosSatelliteFragment = new GeosSatelliteFragment();
        return geosSatelliteFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.satellite_image_fragment_geos, container, false);
        geosViewModel = new GeosViewModel();
        binding.setViewModel(geosViewModel);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.geos_northeast);
        geosViewModel.setGeosImageUrl(currentGeos);
        binding.executePendingBindings();

    }

    @Override
    public void onPause() {
        super.onPause();

    }


}

