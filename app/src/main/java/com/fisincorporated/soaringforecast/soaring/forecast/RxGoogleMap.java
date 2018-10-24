package com.fisincorporated.soaringforecast.soaring.forecast;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RxGoogleMap {

    public static Observable<GoogleMap> getGoogleMapObservable(SupportMapFragment supportMapFragment) {
        Observable observable = Observable.create(emitter -> {
            OnMapReadyCallback mapReadyCallback = googleMap -> emitter.onNext(googleMap);
            supportMapFragment.getMapAsync(mapReadyCallback);
        });
        observable.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
        return observable;
    }

}

