package com.fisincorporated.aviationweather.repository;

import android.content.Context;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.soaring.json.Forecasts;
import com.fisincorporated.aviationweather.soaring.json.SoundingLocation;
import com.fisincorporated.aviationweather.soaring.json.Soundings;
import com.fisincorporated.aviationweather.utils.JSONResourceReader;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;


// TODO consolidate all data access to repository
/**
 * Use to access airport database
 * JSON soundings file
 *
 */

public class AppRepository {

    private static AppRepository appRepository;
    private AirportDao airportDao;
    private Context context;

    private AppRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        airportDao = db.getAirportDao();
        this.context = context;
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

    public Maybe<List<Airport>> selectIcaoIdAirports(List<String> iacoAirports) {
        return airportDao.selectIcaoIdAirports(iacoAirports);
    }

    public Maybe<Airport> getAirport(String ident) {
        return airportDao.getAirportByIdent(ident);
    }


    public Forecasts getForecasts() {
        return (new JSONResourceReader(context.getResources(), R.raw.forecast_options)).constructUsingGson(Forecasts.class);
    }


    public List<SoundingLocation> getLocationSoundings(){
        return (new JSONResourceReader(context.getResources(), R.raw.soundings)).constructUsingGson(Soundings.class).getSoundingLocations();
    }


}
