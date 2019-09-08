package org.soaringforecast.rasp.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.app.CacheTimeListener;
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.common.Constants.FORECAST_SOUNDING;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.retrofit.JSONServerApi;
import org.soaringforecast.rasp.retrofit.SoaringForecastApi;
import org.soaringforecast.rasp.satellite.data.SatelliteImageType;
import org.soaringforecast.rasp.satellite.data.SatelliteRegion;
import org.soaringforecast.rasp.soaring.forecast.SoaringForecastImage;
import org.soaringforecast.rasp.soaring.json.ForecastModels;
import org.soaringforecast.rasp.soaring.json.Forecasts;
import org.soaringforecast.rasp.soaring.json.Regions;
import org.soaringforecast.rasp.soaring.json.SUARegion;
import org.soaringforecast.rasp.soaring.json.SUARegionFiles;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.JSONResourceReader;
import org.soaringforecast.rasp.utils.StringUtils;
import org.soaringforecast.rasp.windy.WindyAltitude;
import org.soaringforecast.rasp.windy.WindyLayer;
import org.soaringforecast.rasp.windy.WindyModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

// TODO consolidate all data access to repository(ies)

/**
 * Use to access Room database
 * JSON soundings file
 */

public class AppRepository implements CacheTimeListener {

    private static AppRepository appRepository;
    private static AppDatabase db;
    private static ArrayList<SatelliteRegion> satelliteRegions;
    private static ArrayList<SatelliteImageType> satelliteImageTypes;
    private static ArrayList<WindyModel> windyModels;
    private static ArrayList<WindyLayer> windyLayers;
    private static ArrayList<WindyAltitude> windyAltitudes;

    private Context context;
    private AirportDao airportDao;
    private TurnpointDao turnpointDao;
    private TaskDao taskDao;
    private TaskTurnpointDao taskTurnpointDao;

    private AppPreferences appPreferences;

    // Forecast related
    private String raspUrl;
    private StringUtils stringUtils;
    private SoaringForecastApi soaringForecastApi;
    private long cacheTimeCheckpoint = 0;
    private long cacheClearTime = 0;
    private BitmapImageUtils bitmapImageUtils;
    private JSONServerApi jsonServerApi;


    private AppRepository(Context context) {
        db = AppDatabase.getDatabase(context);
        airportDao = db.getAirportDao();
        turnpointDao = db.getTurnpointDao();
        taskDao = db.getTaskDao();
        taskTurnpointDao = db.getTaskTurnpointDao();
        this.context = context;

    }

    public static AppRepository getAppRepository(Context context
            , SoaringForecastApi soaringForecastApi
            , JSONServerApi jsonServerApi
            , BitmapImageUtils bitmapImageUtils
            , String raspUrl
            , StringUtils stringUtils
            , AppPreferences appPreferences) {
        if (appRepository == null) {
            synchronized (AppRepository.class) {
                if (appRepository == null) {
                    appRepository = new AppRepository(context);
                    appRepository.soaringForecastApi = soaringForecastApi;
                    appRepository.jsonServerApi = jsonServerApi;
                    appRepository.bitmapImageUtils = bitmapImageUtils;
                    appRepository.raspUrl = raspUrl;
                    appRepository.stringUtils = stringUtils;
                    appRepository.appPreferences = appPreferences;
                    // Since downloader should exist for lifetime of app, not unregistering anywhere
                    appRepository.appPreferences.registerCacheTimeChangeListener(appRepository);
                    appRepository.cacheTimeLimit(appPreferences.getClearCacheTime());
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

    public Single<Integer> deleteAllAirports() {
        return Single.create(emitter -> {
            try {
                emitter.onSuccess(airportDao.deleteAll());
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    // --------- Forecasts -----------------
    public Single<Forecasts> getForecasts() {
        return Single.create(emitter -> {
            try {
                Forecasts forecasts = (new JSONResourceReader(context.getResources(), R.raw.forecast_options_json)).constructUsingGson(Forecasts.class);
                emitter.onSuccess(forecasts);
            } catch (JsonSyntaxException jse) {
                emitter.onError(jse);
            }
        });
    }

    /**
     * Call to find what regions and days forecasts are available
     *
     * @return
     * @throws IOException
     */
    public Single<Regions> getRegionForecastDates() {
        return soaringForecastApi.getForecastDates("current.json");
    }

    /**
     * For the given region (e.g. NewEngland) and day find the available forecast models
     *
     * @param region
     * @param regionForecastDate
     * @throws IOException
     */
    public Single<ForecastModels> getForecastModels(String region, String regionForecastDate) {
        //Timber.d("Url:  %1$s/%2$s/status.json", region, regionForecastDate);
        return soaringForecastApi.getForecastModels(region, regionForecastDate);
    }

    /**
     * Get forecast
     *
     * @param region
     * @param yyyymmddDate
     * @param soaringForecastType
     * @param forecastParameter
     * @param times
     * @return
     */

    public Observable<SoaringForecastImage> getSoaringForecastForTypeAndDay(String region, String yyyymmddDate, String soaringForecastType,
                                                                            String forecastParameter, List<String> times) {
        return checkCacheTimeCompletable().andThen(Observable.fromIterable(times)
                .flatMap((Function<String, Observable<SoaringForecastImage>>) time ->
                        Observable.merge(
                                getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.BODY, FORECAST_SOUNDING.FORECAST).toObservable()
                                , getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.SIDE, FORECAST_SOUNDING.FORECAST).toObservable()

                        )
                ));
    }

    /**
     * Get sounding
     *
     * @param region
     * @param yyyymmddDate
     * @param soaringForecastType
     * @param forecastParameter
     * @param times
     * @return
     */
    public Observable<SoaringForecastImage> getSoaringSoundingForTypeAndDay(String region, String yyyymmddDate, String soaringForecastType,
                                                                            String forecastParameter, List<String> times) {
        return checkCacheTimeCompletable().andThen(Observable.fromIterable(times)
                .flatMap((Function<String, Observable<SoaringForecastImage>>) time ->
                        getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter
                                , time, Constants.BODY, FORECAST_SOUNDING.SOUNDING).toObservable()
                ));
    }

    /**
     * @param region            - "NewEngland"
     * @param yyyymmddDate      - 2018-30-31
     * @param forecastParameter - wstar_bsratio  OR it is sounding index (1 based) with index based on location from sounding.json
     * @param forecastTime      - 1500
     * @param bitmapType        - body
     *                          <p>
     *                          Construct something like http://soargbsc.com/rasp/NewEngland/2018-03-31/gfs/wstar_bsratio.curr.1500lst.d2.body.png”
     *                          for soaring forecast or
     * @param forecastSounding  - gfs/nam/rap
     */
    public Single<SoaringForecastImage> getSoaringForecastImageObservable(String region, String yyyymmddDate, String forecastType,
                                                                          String forecastParameter, String forecastTime, String bitmapType, FORECAST_SOUNDING forecastSounding) {
        return Single.create(emitter -> {
            try {
                final String parmUrl;
                switch (forecastSounding) {
                    case FORECAST:
                        parmUrl = getSoaringForecastUrlParm(region, yyyymmddDate, forecastType, forecastParameter
                                , stringUtils.stripOldIfNeeded(forecastTime), bitmapType);
                        Timber.d("calling parmUrl %1$s", parmUrl);
                        break;
                    case SOUNDING:
                        parmUrl = getSoaringForecastSoundingUrlParm(region, yyyymmddDate, forecastType, forecastParameter
                                , stringUtils.stripOldIfNeeded(forecastTime), bitmapType);
                        break;
                    default:
                        parmUrl = "xxxx";
                }

                SoaringForecastImage soaringForecastImage = getSoaringForcastImage(region, yyyymmddDate, forecastType
                        , forecastParameter, stringUtils.stripOldIfNeeded(forecastTime), bitmapType, parmUrl);
                emitter.onSuccess((SoaringForecastImage) bitmapImageUtils.getBitmapImage(soaringForecastImage, raspUrl, parmUrl));

            } catch (Exception e) {
                emitter.onError(e);
                Timber.e(e);
            }
        });
    }


    @NonNull
    public SoaringForecastImage getSoaringForcastImage(String region, String
            yyyymmddDate, String forecastType, String forecastParameter, String forecastTime, String
                                                               bitmapType, String parmUrl) {
        SoaringForecastImage soaringForecastImage = new SoaringForecastImage(parmUrl);
        soaringForecastImage.setRegion(region)
                .setForecastTime(forecastTime)
                .setYyyymmdd(yyyymmddDate)
                .setForecastType(forecastType)
                .setForecastParameter(forecastParameter)
                .setForecastTime(forecastTime)
                .setBitmapType(bitmapType);
        return soaringForecastImage;
    }


    /**
     * @param region
     * @param yyyymmddDate
     * @param forecastType
     * @param forecastParameter
     * @param forecastTime
     * @param bitmapType
     * @return something like NewEngland/2018-03-31/gfs/wstar_bsratio.1500local.d2.body.png
     */
    //TODO cleanup how url created - use @PATH parms? (But what about creating cache name?)
    public String getSoaringForecastUrlParm(String region, String yyyymmddDate, String
            forecastType, String forecastParameter, String forecastTime, String bitmapType) {
        return String.format("/rasp/%s/%s/%s/%s.%slocal.d2.%s.png", region, yyyymmddDate
                , forecastType.toLowerCase(), forecastParameter, forecastTime, bitmapType);
    }

    /**
     * @param region
     * @param yyyymmddDate
     * @param forecastType
     * @param soundingIndex
     * @param forecastTime
     * @param bitmapType
     * @return something like NewEngland/2018-08-31/nam/sounding3.1200local.d2.png
     */
    //TODO cleanup how url created - use @PATH parms? (But what about creating cache name?)
    public String getSoaringForecastSoundingUrlParm(String region, String yyyymmddDate, String
            forecastType, String soundingIndex, String forecastTime, String bitmapType) {
        return String.format("/rasp/%s/%s/%s/sounding%s.%slocal.d2.png", region, yyyymmddDate
                , forecastType.toLowerCase(), soundingIndex, forecastTime, bitmapType);
    }


    /**
     * See if need to clear cache
     *
     * @return
     */
    public Completable checkCacheTimeCompletable() {
        return Completable.fromAction(() -> {
            try {
                long currentMillis = new Date().getTime();
                /** if cacheTimeCheckpoint (first time here)
                 *      set current millisec time
                 *      don't need to clear cache
                 *   else if time since last call >= cacheClearTime
                 *       clear cache
                 *       set current millisec time
                 */
                if (cacheTimeCheckpoint == 0) {
                    cacheTimeCheckpoint = currentMillis;
                } else if ((currentMillis - cacheTimeCheckpoint) >= cacheClearTime) {
                    Timber.d("Clearing bitmap cache");
                    bitmapImageUtils.clearAllImages();
                    cacheTimeCheckpoint = currentMillis;
                }
            } catch (Throwable throwable) {
                //TODO report error
                throw Exceptions.propagate(throwable);
            }
        });

    }

    @Override
    public void cacheTimeLimit(int minutes) {
        cacheClearTime = minutes * 60 * 1000;
    }


    public Single<Response<ResponseBody>> getLatLngForecast(String region, String date, String model
            , String time, String lat, String lon, String forecastType) {
        return soaringForecastApi.getLatLongPointForecast(region, date, model, time, lat, lon, forecastType);
    }

    // ----------- Turnpoints in Download directory -------------------

    public Maybe<List<File>> getDownloadedCupFileList() {
        return Maybe.create(emitter -> {
            try {
                ArrayList<File> cupFileList = new ArrayList<>();
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File[] filesInDirectory = path.listFiles(new AppRepository.ImageFileFilter());
                // May not have any files downloaded to Downloads directory
                if (filesInDirectory != null) {
                    cupFileList.addAll(new ArrayList<>(Arrays.asList(filesInDirectory)));
                }
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

    public Maybe<List<Turnpoint>> getTurnpointsInRegion(LatLng swLatLong, LatLng neLatLng) {
        return turnpointDao.getTurnpointsInRegion((float) swLatLong.latitude, (float) swLatLong.longitude
                , (float) neLatLng.latitude, (float) neLatLng.longitude);
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
        return Completable.fromAction(() -> {
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
                        long id = taskTurnpointDao.insert(taskTurnpoint);
                        taskTurnpoint.setId(id);
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
                    // if taskturnpoint id = 0 then it was added to task, then deleted(swiped) so not in database to delete
                    if (taskTurnpoint.getId() != 0) {
                        taskTurnpointDao.deleteTaskTurnpoint(taskTurnpoint.getId());
                    }
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
    public Single<Long> addNewTaskAndTurnpoints(Task task, List<TaskTurnpoint> taskTurnpoints) {
        return Single.create((SingleOnSubscribe<Long>) emitter -> {
            try {
                long taskId = taskDao.insert(task);
                for (TaskTurnpoint taskTurnpoint : taskTurnpoints) {
                    taskTurnpoint.setTaskId(taskId);
                }
                long[] keys = taskTurnpointDao.insertAll(taskTurnpoints);
                for (int i = 0; i < keys.length; ++i) {
                    taskTurnpoints.get(i).setId(keys[i]);
                }
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
        if (satelliteRegions == null) {
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

    // ---------- Windy arrays ------------------------
    public List<WindyModel> getWindyModels() {
        if (windyModels == null) {
            windyModels = new ArrayList<WindyModel>();
            Resources res = context.getResources();
            try {
                String[] imageTypes = res.getStringArray(R.array.windy_models);
                for (int i = 0; i < imageTypes.length; ++i) {
                    WindyModel windyModel = new WindyModel(imageTypes[i]);
                    windyModels.add(windyModel);
                }
            } catch (Resources.NotFoundException ignored) {
            }

        }
        return windyModels;
    }

    public List<WindyLayer> getWindyLayers() {
        if (windyLayers == null) {
            windyLayers = new ArrayList<WindyLayer>();
            Resources res = context.getResources();
            try {
                String[] imageTypes = res.getStringArray(R.array.windy_layers);
                for (int i = 0; i < imageTypes.length; ++i) {
                    WindyLayer WindyLayer = new WindyLayer(imageTypes[i]);
                    windyLayers.add(WindyLayer);
                }
            } catch (Resources.NotFoundException ignored) {
            }

        }
        return windyLayers;
    }

    public List<WindyAltitude> getWindyAltitudes() {
        if (windyAltitudes == null) {
            windyAltitudes = new ArrayList<WindyAltitude>();
            Resources res = context.getResources();
            try {
                String[] imageTypes = res.getStringArray(R.array.windy_altitude);
                for (int i = 0; i < imageTypes.length; ++i) {
                    WindyAltitude windyAltitude = new WindyAltitude(imageTypes[i]);
                    windyAltitudes.add(windyAltitude);
                }
            } catch (Resources.NotFoundException ignored) {
            }

        }
        return windyAltitudes;
    }


    // --------- SUA --------------------------------
    /**
     * The process to retrieve/display an SUA for the region is
     * 1. See if SUA file already downloaded for region
     * 2. If so, pass on the GeoJson object
     * 3. In any case, get the lastest SUA info from the server
     * 4. If SUA file was available and the file name matches that of the
     * server, it means the file is still most current so stop here
     * 5. If SUA file not available OR the SUA file is no longer current (an updated file is on server)
     * a. download the new file
     * b. if successful download delete the old (if it existed)
     * c. Emit updated GeoJson object
     *
     * @param region
     */
    public Observable<JSONObject> displaySuaForRegion(String region) {
        return Observable.create(emitter -> {
            // return current stored file if there is one
            String oldSuaFilename = seeIfRegionSUAFileExists(region);
            if (oldSuaFilename != null) {
                Timber.d("Found existing SUA file for region: %1$s  filename: %2$s", region, oldSuaFilename);
                emitter.onNext(getSuaJSONObject(region, oldSuaFilename));
            } else {
                Timber.d("No SUA file found on device for region: %1$s ", region);
            }
            // get list of sua files for regions
            SUARegionFiles suaRegionFiles = jsonServerApi.getSUARegions().blockingGet();
            // get file name for region you are on
            if (suaRegionFiles != null) {
                String newSuaFilename = getSuaFilenameForRegion(suaRegionFiles, region);
                // if no new file found (some oops here) or no updated file just end
                if (newSuaFilename == null || newSuaFilename.equalsIgnoreCase(oldSuaFilename)) {
                    Timber.d("No server SUA file found OR current device file same as server version for region: %1$s ", region);
                } else {
                    // else download new file, delete old and emit new sua geojson object
                    Timber.d("Updated SUA file available for region: %1$s  updated file name: %2$s", region, newSuaFilename);
                    getDownloadSUACompleteable(region, newSuaFilename).blockingGet();
                    if (oldSuaFilename != null) {
                        deleteSUAFile(region, oldSuaFilename);
                    }
                    EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.downloading_new_sua)));
                    emitter.onNext(getSuaJSONObject(region, newSuaFilename));
                }
            } else {
                Timber.d("No server sua_regions file found ");
            }
            emitter.onComplete();
        });
    }

    private String getSuaFilenameForRegion(SUARegionFiles suaRegionFiles, String region) {
        for (SUARegion suaRegion : suaRegionFiles.getSuaRegionList()) {
            if (region.equalsIgnoreCase(suaRegion.getRegion())) {
                return suaRegion.getSuaFileName();
            }
        }
        return null;

    }

    /**
     * SUA files will be stored with a filename associated to the region, e.g. "NewEngland_sterling7_sua.geojson"
     * So only 1 SUA file per region.
     * This method finds out is the specified regions SUA has been download.
     *
     * @param region
     * @return The name of the SUA file (if it exists) that was previously downloaded and saved
     */
    public String seeIfRegionSUAFileExists(String region) {
        String suaFilename = null;
        File fileDir = context.getFilesDir();
        if (fileDir.exists() && fileDir.isDirectory()) {
            final Pattern p = Pattern.compile(region + "_.*\\.geojson");
            File[] flists = fileDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    Timber.d("Found stored sua file with name %1$s", file.getName());
                    return p.matcher(file.getName()).matches();
                }
            });

            // Should only be 0 or 1
            if (flists.length > 0) {
                suaFilename = flists[0].getName().replaceFirst(region + '_', "");
            }
        }
        return suaFilename;
    }

    @NonNull
    private File getSuaFile(String regionName, String suaFilename) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + getCompleteSuaFilename(regionName, suaFilename);
        return new File(path);
    }

    private void deleteSUAFile(String regionName, String suaFilename) {
        File suaFile = getSuaFile(regionName, suaFilename);
        if (suaFile.exists()) {
            Timber.d("Deleting existing %1$s file.", suaFilename);
            suaFile.delete();
        }
    }

    public void deleteAllSuaFilesOnDevice() {
        File fileDir = context.getFilesDir();
        final Pattern p = Pattern.compile("\\.geojson");
        File[] flists = fileDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                Timber.d("Found stored sua file with name %1$s", file.getName());
                return p.matcher(file.getName()).matches();
            }
        });

        for (int i = 0; i < flists.length; ++i) {
            flists[i].delete();
        }
    }

    public Completable getDownloadSUACompleteable(String regionName, String suaFileName) {
        return jsonServerApi.downloadSuaFile(suaFileName).flatMapCompletable(responseBody -> Completable.fromAction(() -> {
            try {
                writeSUAFileToDevice(regionName, suaFileName, responseBody.body());
                Timber.d("SUA file downloaded from server for region: %1$s  filename: %2$s", regionName, suaFileName);
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        }));
    }

    /**
     * Download and save geojson sua file
     * To allow
     *
     * @param regionName  - e.g. "NewEngland"
     * @param suaFileName - sua geojson file on server
     */
    private void writeSUAFileToDevice(String regionName, String suaFileName, ResponseBody responseBody) {
        if (responseBody == null){
            EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.error_getting_sua_file_for_region, regionName), Snackbar.LENGTH_SHORT));
        }
        try {
            byte[] buffer = new byte[4096];
            int length;
            InputStream inputStream = responseBody.byteStream();
            OutputStream outputStream = context.openFileOutput(getCompleteSuaFilename(regionName, suaFileName)
                    , Context.MODE_PRIVATE);
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            Timber.e(e, "Error reading/writing sua geojson file");
            EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.error_getting_sua_file_for_region, regionName), Snackbar.LENGTH_SHORT));
        }
    }

    @NonNull
    private String getCompleteSuaFilename(String regionName, String suaFileName) {
        StringBuilder sb = new StringBuilder();
        return sb.append(regionName).append('_').append(suaFileName).toString();
    }


    public JSONObject getSuaJSONObject(String regionName, String suaFileName) throws IOException, JSONException {
        InputStream is = new FileInputStream(getSuaFile(regionName, suaFileName));
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder responseStrBuilder = new StringBuilder(500 * 1024);
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null) {
            responseStrBuilder.append(inputStr);
        }
        streamReader.close();
        //Timber.d("Sua json string: %1$s", responseStrBuilder.toString());
        return new JSONObject(responseStrBuilder.toString());
    }

}