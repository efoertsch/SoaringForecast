package com.fisincorporated.soaringforecast.satellite.geos;

import android.databinding.BindingAdapter;
import android.support.v4.widget.CircularProgressDrawable;
import android.widget.ImageView;

import com.fisincorporated.soaringforecast.glide.GlideApp;
import com.fisincorporated.soaringforecast.utils.TimeUtils;

public class UrlImageBinding {

    @BindingAdapter("geosImageUrl")
    public static void loadImage(ImageView imageView, String imagePath) {
        if (imagePath != null) {
            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(imageView.getContext());
            circularProgressDrawable.setStyle(CircularProgressDrawable.DEFAULT);
            circularProgressDrawable.start();
            if (imagePath.endsWith(".gif")) {
                GlideApp.with(imageView)
                        .asGif()
                        .load(imagePath)
                        .signature(new GeosCacheKey(TimeUtils.getGeosCacheKey()))
                        .placeholder(circularProgressDrawable)
                        .into(imageView);
            } else {
                GlideApp.with(imageView)
                        .load(imagePath)
                        .signature(new GeosCacheKey(TimeUtils.getGeosCacheKey()))
                        .placeholder(circularProgressDrawable)
                        .into(imageView);
            }
        } else {
            imageView.setImageDrawable(null);
        }
    }

}
