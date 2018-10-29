package com.fisincorporated.soaringforecast.satellite.geos;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.databinding.GeosSatelliteImageBinding;

public class GeosSatelliteFragment  extends Fragment {

    private GeosSatelliteImageBinding binding;

    public static GeosSatelliteFragment newInstance() {
        GeosSatelliteFragment geosSatelliteFragment = new GeosSatelliteFragment();
        return geosSatelliteFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.satellite_image_fragment_geos, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.geos_satellite);
        showGeosGif();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void showGeosGif(){
        Glide.with(this)
                .asGif()
                .load("https://cdn.star.nesdis.noaa.gov/GOES16/ABI/GIFS/GOES16-NE-GEOCOLOR-600x600.gif")
                .into(binding.geosSatelliteImageImageView);
    }


}

