package com.fisincorporated.aviationweather.satellite;

import android.graphics.Bitmap;


public class SatelliteImage  {

    private final String imageName;
    private boolean errorOnLoad = false;
    private Bitmap bitmap = null;

    public SatelliteImage(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public boolean isImageLoaded() {
        return bitmap != null;
    }

    public void setErrorOnLoad(boolean errorOnLoad) {
        this.errorOnLoad = errorOnLoad;
    }

    public boolean isErrorOnLoad() {
        return errorOnLoad;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
