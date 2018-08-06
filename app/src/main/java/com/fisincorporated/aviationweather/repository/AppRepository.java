package com.fisincorporated.aviationweather.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;

public class AppRepository {

    private static AppRepository appRepository;
    private AirportDao airportDao;

    private AppRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        airportDao = db.getAirportDao();
    }

    public static AppRepository getAppRepository(Application application) {
        if (appRepository == null) {
            synchronized (AppRepository.class) {
                if (appRepository == null) {
                    appRepository = new AppRepository(application);
                }
            }
        }
        return appRepository;
    }

    public void insertAirport(Airport airport) {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                airportDao.insertAirport(airport);
                s.onComplete();
            }
        };
    }

    public int getCountOfAirports() {
        return airportDao.getCountOfAirports();
    }

    public LiveData<List<Airport>> findAirports(String ident, String name, String municipality) {
        return airportDao.findAirports(ident, name, municipality);
    }


    public LiveData<Airport> getAirport(String ident) {
        return airportDao.getAirportByIdent(ident);
    }
}
