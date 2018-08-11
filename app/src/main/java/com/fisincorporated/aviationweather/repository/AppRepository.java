package com.fisincorporated.aviationweather.repository;

import android.content.Context;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;

public class AppRepository {

    private static AppRepository appRepository;
    private AirportDao airportDao;

    private AppRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        airportDao = db.getAirportDao();
    }

    public static AppRepository getAppRepository(Context context) {
        if (appRepository == null) {
            synchronized (AppRepository.class) {
                if (appRepository == null) {
                    appRepository = new AppRepository(context);
                }
            }
        }
        return appRepository;
    }

    public void insertAirport(Airport airport) {
        airportDao.insertAirport(airport);
//        Completable completable = new Completable() {
//            @Override
//            protected void subscribeActual(CompletableObserver s) {
//                airportDao.insertAirport(airport);
//                s.onComplete();
//            }
//        };
    }

    public Single<Integer> getCountOfAirports() {
        return airportDao.getCountOfAirports();
    }

    public Maybe<List<Airport>> findAirports(String searchTerm ) {
        return airportDao.findAirports(searchTerm);
    }

    public Maybe<List<Airport>> listAllAirports() {
        return airportDao.listAllAirports();
    }


    public Maybe<Airport> getAirport(String ident) {
        return airportDao.getAirportByIdent(ident);
    }
}
