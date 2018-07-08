package com.fisincorporated.aviationweather.common;

import android.graphics.Bitmap;

public class BitmapImage {

    private final String imageName;
    private boolean errorOnLoad = false;
    private Bitmap bitmap = null;

    public BitmapImage(String imageName) {
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
        bitmap = null;
    }

    public boolean isErrorOnLoad() {
        return errorOnLoad;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        errorOnLoad = false;
    }
}
