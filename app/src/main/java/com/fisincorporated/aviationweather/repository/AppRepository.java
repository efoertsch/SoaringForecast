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
    private TurnpointDao turnpointDao;
    private Context context;


    private AppRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        airportDao = db.getAirportDao();
        turnpointDao = db.getTurnpointDao();
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

    // --------- Airports -----------------
    public void insertAirport(Airport airport) {
        airportDao.insert(airport);
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


    // --------- Forecasts -----------------
    public Forecasts getForecasts() {
        return (new JSONResourceReader(context.getResources(), R.raw.forecast_options)).constructUsingGson(Forecasts.class);
    }

    // --------- Soundings ------------------
    public List<SoundingLocation> getLocationSoundings(){
        return (new JSONResourceReader(context.getResources(), R.raw.soundings)).constructUsingGson(Soundings.class).getSoundingLocations();
    }

    // --------- Turnpoints -----------------
    public long insertTurnpoint(Turnpoint turnpoint){
        return turnpointDao.insert(turnpoint);
    }

    public long updateTurnpoint(Turnpoint turnpoint){
        return turnpointDao.update(turnpoint);
    }

    public Maybe<List<Turnpoint>> findTurnpoints(String searchTerm){
        return turnpointDao.findTurnpoints(searchTerm);
    }

    public Maybe<List<Turnpoint>> listAllTurnpoints(){
        return turnpointDao.listAllTurnpoints();
    }

    public Maybe<Turnpoint>  getTurnpoint(String title, String code){
        return turnpointDao.getTurnpoint(title, code);
    }

    public int deleteAllTurnpoints(){
        return turnpointDao.deleteAllTurnpoints();
    }



}
