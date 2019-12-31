package org.soaringforecast.rasp.satellite.geos;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CircularProgressDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.TouchImageView;
import org.soaringforecast.rasp.databinding.GeosSatelliteImageBinding;
import org.soaringforecast.rasp.glide.GlideApp;
import org.soaringforecast.rasp.utils.TimeUtils;

public class GeosSatelliteFragment extends Fragment {

    // TODO move to URL file
    private static final String geosGif = "https://cdn.star.nesdis.noaa.gov/GOES16/ABI/GIFS/GOES16-NE-GEOCOLOR-600x600.gif";
    private static final String currentGeos = "https://cdn.star.nesdis.noaa.gov/GOES16/ABI/SECTOR/ne/GEOCOLOR/600x600.jpg";

    private GeosSatelliteImageBinding binding;
    private TouchImageView touchImageView;
    private Button loopButton;

    public static GeosSatelliteFragment newInstance() {
        return new GeosSatelliteFragment();
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.satellite_image_fragment_geos, container, false);
        touchImageView = binding.geosSatelliteImageImageView;
        loopButton = binding.satelliteImageFragmentGeosLoopBtn;
        loopButton.setOnClickListener(view -> toggleGeosDisplay());
        displayGeosImage(currentGeos);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.geos_northeast);
    }

    private void toggleGeosDisplay() {
        if (loopButton.getText().equals(getString(R.string.loop))) {
            loopButton.setText(R.string.latestImage);
            displayGeosImage(geosGif);
        } else {
            loopButton.setText(R.string.loop);
            displayGeosImage(currentGeos);
        }
    }

    private void displayGeosImage(String url) {
        if (url != null) {
            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(touchImageView.getContext());
            circularProgressDrawable.setStyle(CircularProgressDrawable.DEFAULT);
            circularProgressDrawable.start();
            if (url.endsWith(".gif")) {
                GlideApp.with(touchImageView)
                        .asGif()
                        .load(url)
                        .signature(new GeosCacheKey(TimeUtils.getGeosLoopCacheKey(5)))
                        .placeholder(circularProgressDrawable)
                        .into(touchImageView);
            } else {
                GlideApp.with(touchImageView)
                        .load(url)
                        .signature(new GeosCacheKey(TimeUtils.getGeosLoopCacheKey(5)))
                        .placeholder(circularProgressDrawable)
                        .into(touchImageView);
            }
        } else {
            touchImageView.setImageDrawable(null);
        }
    }

}

