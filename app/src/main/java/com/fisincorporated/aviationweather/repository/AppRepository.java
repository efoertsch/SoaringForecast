package com.fisincorporated.aviationweather.repository;

import android.content.Context;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.soaring.json.Forecasts;
import com.fisincorporated.aviationweather.soaring.json.SoundingLocation;
import com.fisincorporated.aviationweather.soaring.json.Soundings;
import com.fisincorporated.aviationweather.utils.JSONResourceReader;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;

// TODO consolidate all data access to repository

/**
 * Use to access airport database
 * JSON soundings file
 */

public class AppRepository {

    private static AppRepository appRepository;
    private AirportDao airportDao;
    private TurnpointDao turnpointDao;
    private TaskDao taskDao;
    private TaskTurnpointDao taskTurnpointDao;
    private Context context;

    private AppRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        airportDao = db.getAirportDao();
        turnpointDao = db.getTurnpointDao();
        taskDao = db.getTaskDao();
        taskTurnpointDao = db.getTaskTurnpointDao();
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

    public Maybe<List<Airport>> findAirports(String searchTerm) {
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
    public List<SoundingLocation> getLocationSoundings() {
        return (new JSONResourceReader(context.getResources(), R.raw.soundings)).constructUsingGson(Soundings.class).getSoundingLocations();
    }

    // --------- Turnpoints -----------------
    public long insertTurnpoint(Turnpoint turnpoint) {
        return turnpointDao.insert(turnpoint);
    }

    public void updateTurnpoint(Turnpoint turnpoint) {
        turnpointDao.update(turnpoint);
    }

    public Maybe<List<Turnpoint>> findTurnpoints(String searchTerm) {
        return turnpointDao.findTurnpoints(searchTerm);
    }

    public Maybe<List<Turnpoint>> listAllTurnpoints() {
        return turnpointDao.listAllTurnpoints();
    }

    public Maybe<Turnpoint> getTurnpoint(String title, String code) {
        return turnpointDao.getTurnpoint(title, code);
    }

    public int deleteAllTurnpoints() {
        return turnpointDao.deleteAllTurnpoints();
    }


    // ---------- Task ------------------
    public Maybe<List<Task>> listAllTasks() {
        return taskDao.listAllTasks();
    }

    public Maybe<Task> getTask(long taskId) {
        return taskDao.getTask(taskId);
    }

    public Single<Long> insertTask(Task task) {
        return Single.create((SingleOnSubscribe<Long>) emitter -> {
            try {
                Long id = taskDao.insert(task);
                emitter.onSuccess(id);
            } catch (Throwable t) {
                emitter.onError(t);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public Completable updateTaskListOrder(List<Task> taskList) {
        Completable completable = Completable.fromAction(() -> {
            try {
                for (Task task: taskList) {
                    taskDao.update(task);
                }
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });
        return completable;
    }

    // -----------Task Turnpoints -----------
    public Maybe<List<TaskTurnpoint>> listTaskTurnpionts(long taskId) {
        return taskTurnpointDao.getTaskTurnpoints(taskId);
    }


}
