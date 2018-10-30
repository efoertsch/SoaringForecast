package com.fisincorporated.soaringforecast.satellite.geos;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;

public class GeosViewModel extends BaseObservable {

    public ObservableField<String> geosImageUrl = new ObservableField<>();

    @Bindable
    public ObservableField<String> getGeosImageUrl() {
        return geosImageUrl;
    }

    public void setGeosImageUrl(String geosImageUrlRequested) {
        geosImageUrl.set(geosImageUrlRequested);
    }

}
