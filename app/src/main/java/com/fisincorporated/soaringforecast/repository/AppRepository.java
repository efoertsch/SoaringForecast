package com.fisincorporated.soaringforecast.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImageType;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteRegion;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastModel;
import com.fisincorporated.soaringforecast.soaring.json.Forecasts;
import com.fisincorporated.soaringforecast.soaring.json.SoundingLocation;
import com.fisincorporated.soaringforecast.soaring.json.Soundings;
import com.fisincorporated.soaringforecast.task.json.TurnpointFile;
import com.fisincorporated.soaringforecast.task.json.TurnpointFiles;
import com.fisincorporated.soaringforecast.task.json.TurnpointRegion;
import com.fisincorporated.soaringforecast.utils.JSONResourceReader;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;

// TODO consolidate all data access to repository(ies)

/**
 * Use to access Room database
 * JSON soundings file
 */

public class AppRepository {

    private static AppRepository appRepository;
    private static AppDatabase db;
    private static ArrayList<SatelliteRegion> satelliteRegions;
    private static ArrayList<SatelliteImageType> satelliteImageTypes;

    private Context context;
    private AirportDao airportDao;
    private TurnpointDao turnpointDao;
    private TaskDao taskDao;
    private TaskTurnpointDao taskTurnpointDao;

    private AppRepository(Context context) {
        db = AppDatabase.getDatabase(context);
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

    public int getDatabaseVersion() {
        return db.getDatabaseVersion();
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

    public Maybe<List<Airport>> selectIcaoIdAirports(List<String> icaoAirports) {
        return airportDao.selectIcaoIdAirports(icaoAirports);
    }

    public Maybe<List<Airport>> getAirportsByIcaoIdAirports(List<String> icaoIds) {
        return selectIcaoIdAirports(icaoIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Maybe<Airport> getAirport(String ident) {
        return airportDao.getAirportByIdent(ident);
    }


    // --------- Forecasts -----------------
    public Single<Forecasts> getForecasts() {
        return Single.create(emitter -> {
            try {
                Forecasts forecasts = (new JSONResourceReader(context.getResources(), R.raw.forecast_options)).constructUsingGson(Forecasts.class);
                emitter.onSuccess(forecasts);
            } catch (JsonSyntaxException jse) {
                emitter.onError(jse);
            }
        });
    }

    public Single<List<SoaringForecastModel>> getSoaringForecastModels() {
        return Single.create(emitter -> {
            String[] types;
            Resources res = context.getResources();
            List<SoaringForecastModel> soaringForecastModelList = new ArrayList<>();
            try {
                types = res.getStringArray(R.array.soaring_forecast_models);
                for (int i = 0; i < types.length; ++i) {
                    SoaringForecastModel soaringForecastModel = new SoaringForecastModel(types[i]);
                    soaringForecastModelList.add(soaringForecastModel);
                }
                emitter.onSuccess(soaringForecastModelList);
            } catch (Resources.NotFoundException nfe) {
                emitter.onError(nfe);
            }
        });
    }

    // --------- Soundings ------------------
    public List<SoundingLocation> getLocationSoundings() {
        return (new JSONResourceReader(context.getResources(), R.raw.soundings)).constructUsingGson(Soundings.class).getSoundingLocations();
    }

    // ----------- Turnpoints in Download directory

    public Maybe<List<File>> getDownloadedCupFileList() {
       return Maybe.create(emitter -> {
                    try {
                        ArrayList<File> cupFileList = new ArrayList<>();
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File filesInDirectory[] = path.listFiles(new AppRepository.ImageFileFilter());
                        cupFileList.addAll(new ArrayList<>(Arrays.asList(filesInDirectory)));
                        emitter.onSuccess(cupFileList);
                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                });
    }

    public class ImageFileFilter implements FileFilter {
        private final String[] cupFileExtensions = new String[]{"cup"};

        public boolean accept(File file) {
            for (String extension : cupFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    // make sure not version with control number placed before name.
                    return !file.getName().substring(0, file.getName().indexOf(".cup")).endsWith("_nm");
                }
            }
            return false;
        }
    }

    // ----------- Turnpoint download (SeeYou cup files) ------------------
    public Maybe<List<TurnpointFile>> getTurnpointFiles(String regionName) {
        return Maybe.create(emitter -> {
                    try {
                        List<TurnpointRegion> turnpointRegions = (new JSONResourceReader(context.getResources(), R.raw.turnpoint_download_list))
                                .constructUsingGson((TurnpointFiles.class)).getTurnpointRegions();
                        if (turnpointRegions != null && !turnpointRegions.isEmpty()) {
                            emitter.onSuccess(getRegionFiles(turnpointRegions, regionName));
                        } else {
                            emitter.onComplete();
                        }
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                });
    }

    private List<TurnpointFile> getRegionFiles
            (List<TurnpointRegion> turnpointRegions, String regionName) {
        for (TurnpointRegion turnpointRegion : turnpointRegions) {
            if (turnpointRegion.getRegion().equals(regionName)) {
                return turnpointRegion.getTurnpointFiles();
            }
        }
        return new ArrayList<>();

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

    public Single<Integer> deleteAllTurnpoints() {
        return Single.create((SingleOnSubscribe<Integer>) emitter -> {
            try {
                int numberDeleted = turnpointDao.deleteAllTurnpoints();
                emitter.onSuccess(numberDeleted);
            } catch (Throwable t) {
                emitter.onError(t);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public Single<Integer> getCountOfTurnpoints() {
        return turnpointDao.getTurnpointCount().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Maybe<Turnpoint> checkForAtLeastOneTurnpoint() {
        return turnpointDao.checkForAtLeastOneTurnpoint();
    }

    // ---------- Task ------------------
    public Maybe<List<Task>> listAllTasks() {
        return taskDao.listAllTasks();
    }

    public Maybe<Task> getTask(long taskId) {
        return taskDao.getTask(taskId);
    }

    public Completable updateTask(Task task) {
        return Completable.fromAction(() -> {
            try {
                taskDao.update(task);
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });


    }

    public Completable updateTaskListOrder(List<Task> taskList) {
        return  Completable.fromAction(() -> {
            try {
                for (Task task : taskList) {
                    taskDao.update(task);
                }
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });
    }

    public Completable deleteTask(Task task) {
        return Completable.fromAction(() -> {
            try {
                taskTurnpointDao.deleteTaskTurnpoints(task.getId());
                taskDao.deleteTask(task.getId());
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });
    }

    // -----------Task Turnpoints -----------
    public Maybe<List<TaskTurnpoint>> getTaskTurnpionts(long taskId) {
        return taskTurnpointDao.getTaskTurnpoints(taskId);
    }

    public Completable updateTaskTurnpoints(List<TaskTurnpoint> taskTurnpoints) {
        return Completable.fromAction(() -> {
            try {
                for (TaskTurnpoint taskTurnpoint : taskTurnpoints) {
                    if (taskTurnpoint.getId() == 0) {
                        taskTurnpointDao.insert(taskTurnpoint);
                    } else {
                        taskTurnpointDao.update(taskTurnpoint);
                    }
                }
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });
    }

    public Completable deleteTaskTurnpoints(List<TaskTurnpoint> taskTurnpoints) {
        return Completable.fromAction(() -> {
            try {
                for (TaskTurnpoint taskTurnpoint : taskTurnpoints) {
                    taskTurnpointDao.deleteTaskTurnpoint(taskTurnpoint.getTaskId(), taskTurnpoint.getTitle(), taskTurnpoint.getCode());
                }
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });
    }

    // ---------- Update Task and Turnpoints --------------------------------

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

    @SuppressLint("CheckResult")
    public Single<Long> addNewTaskAndTurnpoints(Task
                                                        task, List<TaskTurnpoint> taskTurnpoints) {
        return Single.create((SingleOnSubscribe<Long>) emitter -> {
            try {
                long taskId = taskDao.insert(task);
                for (TaskTurnpoint taskTurnpoint : taskTurnpoints) {
                    taskTurnpoint.setTaskId(taskId);
                }
                long[] keys = taskTurnpointDao.insertAll(taskTurnpoints);
                emitter.onSuccess(taskId);
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());

    }

    @SuppressLint("CheckResult")
    public Completable updateTaskAndTurnpoints(Task task, List<TaskTurnpoint> taskTurnpoints
            , List<TaskTurnpoint> deleteTurnpoints) {
        return updateTask(task)
                .andThen(updateTaskTurnpoints(taskTurnpoints))
                .andThen(deleteTaskTurnpoints(deleteTurnpoints))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    // ----- Satellite options ----------

    public List<SatelliteRegion> getSatelliteRegions() {
        if (satelliteRegions == null ) {
           satelliteRegions = new ArrayList<>();
            Resources res = context.getResources();
            try {
                String[] regions = res.getStringArray(R.array.satellite_regions);
                for (int i = 0; i < regions.length; ++i) {
                    SatelliteRegion satelliteRegion = new SatelliteRegion(regions[i]);
                    satelliteRegions.add(satelliteRegion);
                }
            } catch (Resources.NotFoundException ignored) {
            }
        }
        return satelliteRegions;
    }

    public List<SatelliteImageType> getSatelliteImageTypes() {
        if (satelliteImageTypes == null) {
            satelliteImageTypes = new ArrayList<>();
            Resources res = context.getResources();
            try {
                String[] imageTypes = res.getStringArray(R.array.satellite_image_types);
                for (int i = 0; i < imageTypes.length; ++i) {
                    SatelliteImageType satelliteImageType = new SatelliteImageType(imageTypes[i]);
                    satelliteImageTypes.add(satelliteImageType);
                }
            } catch (Resources.NotFoundException ignored) {
            }
        }
        return satelliteImageTypes;
    }

}