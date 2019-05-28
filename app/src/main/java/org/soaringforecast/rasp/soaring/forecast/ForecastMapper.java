package org.soaringforecast.rasp.soaring.forecast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.json.Sounding;
import org.soaringforecast.rasp.soaring.messages.DisplayLatLngForecast;
import org.soaringforecast.rasp.soaring.messages.DisplaySounding;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.ViewUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Responsible for handling map display
 */
public class ForecastMapper implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener
        , GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener {

    private boolean drawingTask = false;

    //Default for NewEngland - try to get something displayed quickly rather than blank screen
    private LatLngBounds mapLatLngBounds = new LatLngBounds(new LatLng(41.2665329, -73.6473083)
            , new LatLng(45.0120811, -70.5046997));
    //private LatLngBounds mapLatLngBounds;
    private List<Sounding> soundings = new ArrayList<>();
    private List<TaskTurnpoint> taskTurnpoints = new ArrayList<>();

    private List<Marker> taskTurnpointMarkers = new ArrayList<>();
    private List<Polyline> taskTurnpointLines = new ArrayList<>();
    private List<Marker> soundingMarkers = new ArrayList<>();
    private GoogleMap googleMap;

    private GroundOverlay forecastOverlay;
    private int forecastOverlayOpacity;
    private Marker lastMarkerOpened;
    private GeoJsonLayer geoJsonLayer = null;

    private List<Marker> turnpointMarkers = new ArrayList<>();
    private List<Turnpoint> turnpoints;
    private Bitmap largeTurnpointBitmap;
    private Bitmap smallerTurnpointBitmap;

    /**
     * Use to center task route in googleMap frame
     */
    private LatLng southwest = null;
    private double swLat = 0;
    private double swLong = 0;
    private double neLat = 0;
    private double neLong = 0;
    private Context context;
    private boolean listenToLayerClicks;
    private int zoomLevel;
    private int soundingsTextZoomLevel = 0;
    private float soundingsTextSize;
    private ScaleDrawable soundingsSkewTDrawable;
    private Resources resources;

    // MapType must be one of GoogleMap.MAP_TYPE_xxxx
    private int mapType = GoogleMap.MAP_TYPE_TERRAIN;

    @Inject
    public ForecastMapper() {
    }

    public ForecastMapper setContext(Context context) {
        this.context = context;
        resources = context.getResources();
        return this;
    }

    public ForecastMapper displayMap(SupportMapFragment mapFragment) {
        mapFragment.getMapAsync(this);
        return this;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(mapType);
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                int newZoom = (int) googleMap.getCameraPosition().zoom;
                if (newZoom != zoomLevel) {
                    zoomLevel = newZoom;
                    Timber.d("Zoom level: %1$d", (int) googleMap.getCameraPosition().zoom);
                    mapTurnpoints();
                    mapSoundingMarkers();
                }
            }
        });
        // if delay in map getting ready and bounds, sounding locations or task already passed in display them as
        // required
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapLongClickListener(this);
        googleMap.setInfoWindowAdapter(new MapperInfoWindowAdapter());
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnInfoWindowCloseListener(this);
        updateMapBounds();
        mapSoundingMarkers();
        plotTaskTurnpoints();
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
        if (googleMap != null) {
            googleMap.setMapType(mapType);
        }
    }

    public void setMapLatLngBounds(LatLngBounds mapLatLngBounds) {
        this.mapLatLngBounds = mapLatLngBounds;
        updateMapBounds();
    }

    private void updateMapBounds() {
        if (googleMap != null && mapLatLngBounds != null) {
            googleMap.setLatLngBoundsForCameraTarget(mapLatLngBounds);
            if (!drawingTask) {
                // if drawing task use the task latlng bounds for map positioning
                googleMap.setOnMapLoadedCallback(() -> googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapLatLngBounds, 0)));

            }
        }
    }

    // ---- Forecast overlay ------------------------------------
    // 100% opacity = 0% transnparent
    public void setGroundOverlay(Bitmap bitmap) {
        if (bitmap == null) {
            if (googleMap != null && forecastOverlay != null) {
                forecastOverlay.remove();
                forecastOverlay = null;
                return;
            } else {
                return;
            }
        }

        if (forecastOverlay == null) {
            GroundOverlayOptions forecastOverlayOptions = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .positionFromBounds(mapLatLngBounds);
            forecastOverlayOptions.transparency(1.0f - forecastOverlayOpacity / 100.0f);
            if (googleMap != null) {
                forecastOverlay = googleMap.addGroundOverlay(forecastOverlayOptions);
            }
        } else {
            setForecastOverlayTranparency();
            forecastOverlay.setImage(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }

    public void setForecastOverlayOpacity(int forecastOverlayOpacity) {
        this.forecastOverlayOpacity = forecastOverlayOpacity;
        setForecastOverlayTranparency();
    }

    private void setForecastOverlayTranparency() {
        if (forecastOverlay != null) {
            forecastOverlay.setTransparency(1.0f - forecastOverlayOpacity / 100.0f);
        }
    }

    // ----- Sounding markers ----------------------------------------
    public void setSoundings(List<Sounding> soundings) {
        this.soundings.clear();
        if (soundings == null || soundings.size() == 0) {
            removeSoundingMarkers();
        } else {
            this.soundings.addAll(soundings);
            mapSoundingMarkers();
        }
    }

    private void mapSoundingMarkers() {
        if (googleMap == null) {
            return;
        }
        removeSoundingMarkers();
        soundingMarkers.clear();
        LatLng latLng;
        Marker marker;
        for (Sounding sounding : soundings) {
            latLng = new LatLng(sounding.getLat(), sounding.getLng());
            marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap( createSoundingMarker(sounding.getLocation()))));
            marker.setTag(sounding);
            soundingMarkers.add(marker);
        }
    }

    private void removeSoundingMarkers() {
        if (soundingMarkers != null && soundingMarkers.size() > 0) {
            for (Marker marker : soundingMarkers) {
                marker.remove();
            }
        }
    }

    // ------ Task Turnpoints ---------------------------------
    public void setTaskTurnpoints(List<TaskTurnpoint> newTaskTurnpoints) {
        removeTaskTurnpoints();
        taskTurnpoints.clear();
        if (newTaskTurnpoints != null) {
            taskTurnpoints.addAll(newTaskTurnpoints);
            plotTaskTurnpoints();
        }
    }

    public void removeTaskTurnpoints() {
        drawingTask = false;
        for (Polyline polyline : taskTurnpointLines) {
            polyline.remove();
        }

        taskTurnpointLines.clear();
        for (Marker marker : taskTurnpointMarkers) {
            marker.remove();
        }
        taskTurnpointMarkers.clear();
    }

    @SuppressLint("DefaultLocale")
    private void plotTaskTurnpoints() {
        TaskTurnpoint taskTurnpoint;
        LatLng fromLatLng = new LatLng(0d, 0d); // to get rid of syntax checker flag
        LatLng toLatLng;

        if (googleMap == null) {
            return;
        }
        if (taskTurnpoints != null && taskTurnpoints.size() > 0) {
            drawingTask = true;
            for (int i = 0; i < taskTurnpoints.size(); ++i) {
                taskTurnpoint = taskTurnpoints.get(i);
                if (i == 0) {
                    fromLatLng = new LatLng(taskTurnpoint.getLatitudeDeg(), taskTurnpoint.getLongitudeDeg());
                    // placeTaskTurnpointMarker(taskTurnpoint.getTitle()
                    //        , String.format("%1$.1fkm", taskTurnpoint.getDistanceFromStartingPoint()), fromLatLng);
                    placeTaskTurnpointMarker(getTurnpointMarkerBitmap(taskTurnpoint), fromLatLng, taskTurnpoint);
                } else {
                    toLatLng = new LatLng(taskTurnpoint.getLatitudeDeg(), taskTurnpoint.getLongitudeDeg());
                    //placeTaskTurnpointMarker(taskTurnpoint.getTitle(),
                    //        String.format("%1$.1fkm", taskTurnpoint.getDistanceFromStartingPoint()), toLatLng);
                    placeTaskTurnpointMarker(getTurnpointMarkerBitmap(taskTurnpoint), toLatLng, taskTurnpoint);
                    drawLine(fromLatLng, toLatLng);
                    fromLatLng = toLatLng;

                }
                getTurnpointMarkerBitmap(taskTurnpoint);
                updateMapLatLongCorners(fromLatLng);
            }
            LatLng southwest = new LatLng(swLat, swLong);
            LatLng northeast = new LatLng(neLat, neLong);
            googleMap.setOnMapLoadedCallback(() ->
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                            southwest, northeast), 60)));
        }
    }

    private Bitmap getTurnpointMarkerBitmap(TaskTurnpoint taskTurnpoint) {
        if (googleMap == null) {
            EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.googlemap_not_defined_can_not_create_turnpoint_markers)
                    , Snackbar.LENGTH_LONG));
        }
        View markerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.task_turnpoint_marker, null);

        ((TextView) markerView.findViewById(R.id.turnpoint_marker_name)).setText(taskTurnpoint.getTitle());
        ((TextView) markerView.findViewById(R.id.turnpoint_marker_distance)).setText(context.getString(R.string.distance_from_prior_and_start
                , (int) taskTurnpoint.getDistanceFromPriorTurnpoint(), (int) taskTurnpoint.getDistanceFromStartingPoint()));

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        Bitmap returnedBitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        //canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = markerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        markerView.draw(canvas);
        return returnedBitmap;

    }

    /**
     * Find the most southwest and northeast task lat/long
     *
     * @param latLng - latlng of task turnpoint
     */
    private void updateMapLatLongCorners(LatLng latLng) {
        if (southwest == null) {
            southwest = latLng;
            swLat = latLng.latitude;
            swLong = latLng.longitude;

            neLat = latLng.latitude;
            neLong = latLng.longitude;
        }
        if (latLng.latitude < swLat) {
            swLat = latLng.latitude;
        }
        if (latLng.longitude < swLong) {
            swLong = latLng.longitude;
        }
        if (latLng.latitude > neLat) {
            neLat = latLng.latitude;
        }
        if (latLng.longitude > neLong) {
            neLong = latLng.longitude;
        }
    }

    private void drawLine(LatLng fromLatLng, LatLng toLatLng) {
        Polyline polyline = googleMap.addPolyline(new PolylineOptions().add(fromLatLng, toLatLng)
                .width(5).color(Color.RED));
        taskTurnpointLines.add(polyline);
    }


    private void placeTaskTurnpointMarker(Bitmap bitmap, LatLng latLng, TaskTurnpoint taskTurnpoint) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        marker.setTag(taskTurnpoint);
        taskTurnpointMarkers.add(marker);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (lastMarkerOpened != null) {
            // Close the info window
            lastMarkerOpened.hideInfoWindow();

            // Is the marker the same marker that was already open
            if (lastMarkerOpened.equals(marker)) {
                // Nullify the last opened object
                lastMarkerOpened = null;
                // Return so that the info window isn't opened again
                return true;
            }
        }

        if (marker.getTag() instanceof Turnpoint) {
            lastMarkerOpened = marker;
            marker.showInfoWindow();
            return true;
        }
        if (marker.getTag() instanceof TaskTurnpoint) {
            return true;
        }
        if (marker.getTag() instanceof Sounding) {
            EventBus.getDefault().post(new DisplaySounding((Sounding) marker.getTag()));
            return true;
        }
        return false;
    }

    //----- Draw SUA -------------------------------
    // TODO improve to allow display of different SUA regions
    @SuppressLint("ResourceType")
    public void setSuaRegionName(String suaRegionName) {
        if (suaRegionName != null) {
            int geoJsonRawId = 0;
            if (suaRegionName.equalsIgnoreCase(context.getString(R.string.new_england_region))) {
                geoJsonRawId = R.raw.sterlng7_sua_geojson;
            } else if (suaRegionName.equalsIgnoreCase(context.getString(R.string.mifflin_region))) {
                geoJsonRawId = R.raw.mifflin8_sua_geojson;
            }

            if (geoJsonRawId != 0) {
                addGeoJsonLayerToMap(geoJsonRawId, suaRegionName);
            } else {
                EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.no_sua_defined_for_specified_region, suaRegionName)
                        , Snackbar.LENGTH_LONG));
            }
        }
    }

    // TODO iterate through features and assign color to each based on type of SUA
    private void addGeoJsonLayerToMap(@IntegerRes int geoJsonid, String suaRegionName) {
        try {
            geoJsonLayer = new GeoJsonLayer(googleMap, geoJsonid, context);
            // TODO reimplement after Google fixes bugs
            // Bug when clicking on map, may not get correct feature, also still getting click event
            // after layer removed from map so listener not currently used.
            //geoJsonLayer.setOnFeatureClickListener(geoJsonOnFeatureClickListener);
        } catch (Exception e) {
            postError(e, suaRegionName);
        }
        geoJsonLayer.addLayerToMap();
    }

    private GeoJsonLayer.GeoJsonOnFeatureClickListener geoJsonOnFeatureClickListener = feature -> {
        displaySuaDetails(feature);
    };

    private void displaySuaDetails(Feature feature) {
        ArrayList<String> suaProperties = new ArrayList<>();
        Iterator it = feature.getProperties().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            suaProperties.add(context.getString(R.string.sua_property, pair.getKey(), pair.getValue()));
        }
        if (suaProperties.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = ViewUtilities.getActivity(context).getLayoutInflater();
            View suaPropertiesView = inflater.inflate(R.layout.sua_properties_list, null);
            builder.setView(suaPropertiesView);
            ListView suaPropertiesListView = suaPropertiesView.findViewById(R.id.sua_properties_listview);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
            arrayAdapter.addAll(suaProperties);
            suaPropertiesListView.setAdapter(arrayAdapter);
            builder.setTitle("SUA");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(true);
        }
    }

    private void postError(Exception e, String suaRegionName) {
        // TODO report exception
        e.printStackTrace();
        EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.oops_error_loading_sua_file, suaRegionName)
                , Snackbar.LENGTH_LONG));
    }

    // Simply going geoJsonLayer.removeLayerFromMap still leaves GeoJsonOnFeatureClickListener active (some bug)
    // so remove the features one by 1
    public void removeSuaFromMap() {
        if (geoJsonLayer != null) {
            // click listener continues to function even when layer removed from map, so set flag (for now) to
            // indicate to ignore clicks
            geoJsonLayer.removeLayerFromMap();
            geoJsonLayer = null;
        }
    }

    // ---- Turnpoints -----------------------------
    public void mapTurnpoints(List<Turnpoint> turnpoints) {
        this.turnpoints = turnpoints;
        mapTurnpoints();
    }

    private void mapTurnpoints() {
        Bitmap turnpointBitmap;
        if (googleMap == null) {
            return;
        }
        clearTurnpointMarkers();
        if (largeTurnpointBitmap == null) {
            largeTurnpointBitmap = BitmapImageUtils.getBitmapFromVectorDrawable(context, R.drawable.ic_turnpoint);
        }
        if (turnpoints != null) {
            if (zoomLevel <= 8) {
                // use smaller bitmap icon
                if (smallerTurnpointBitmap == null) {
                    smallerTurnpointBitmap = Bitmap.createScaledBitmap(largeTurnpointBitmap
                            , largeTurnpointBitmap.getWidth() / 2, largeTurnpointBitmap.getHeight() / 2
                            , true);
                }
                turnpointBitmap = smallerTurnpointBitmap;
            } else {
                turnpointBitmap = largeTurnpointBitmap;
            }
            for (Turnpoint turnpoint : turnpoints) {
                placeTurnpointMarker(turnpoint, turnpointBitmap);
            }
        }
    }

    private void placeTurnpointMarker(Turnpoint turnpoint, Bitmap bitmap) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(turnpoint.getLatitudeDeg(), turnpoint.getLongitudeDeg()))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        marker.setTag(turnpoint);
        turnpointMarkers.add(marker);
    }

    private void clearTurnpointMarkers() {
        for (Marker marker : turnpointMarkers) {
            marker.remove();
        }
        turnpointMarkers.clear();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        try {
            EventBus.getDefault().post(new DisplayLatLngForecast(latLng));
        } catch (Exception e){
            EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.oops_try_that_again), Snackbar.LENGTH_SHORT));
        }
        
    }

    public void displayLatLngForecast(LatLngForecast latLngForecast) {
        if (googleMap != null) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLngForecast.getLatLng())
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            BitmapImageUtils.getBitmapFromVectorDrawable(context, R.drawable.transparent_marker)))); // transparent image
            marker.setTag(latLngForecast);
            marker.showInfoWindow();
        }

    }

    //--------- InfoWindow ---------------------------------------------------------------------------
    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        if (marker.getTag() instanceof LatLngForecast) {
            marker.remove();
        }
    }

    class MapperInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mapperInfoWindowView;
        private final TextView mapperInfoWindowInfo;

        MapperInfoWindowAdapter() {
            mapperInfoWindowView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.turnpoint_infowindow, null);
            mapperInfoWindowInfo = mapperInfoWindowView.findViewById(R.id.turnpoint_infowindow_info);
            mapperInfoWindowInfo.setMovementMethod(new ScrollingMovementMethod());
        }

        public View getInfoWindow(Marker marker) {
            render(marker, mapperInfoWindowInfo);
            return mapperInfoWindowView;
        }

        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, TextView view) {
            if (marker.getTag() instanceof Turnpoint) {
                try {
                    Turnpoint turnpoint = (Turnpoint) marker.getTag();
                    switch (turnpoint.getStyle()) {
                        case "2":
                        case "4":
                        case "5":
                            view.setText(context.getString(R.string.turnpoint_airport_info_window_text
                                    , turnpoint.getTitle(), turnpoint.getCode()
                                    , turnpoint.getStyleName()
                                    , turnpoint.getLatitudeDeg(), turnpoint.getLongitudeDeg()
                                    , turnpoint.getElevation()
                                    , turnpoint.getDirection(), turnpoint.getLength()
                                    , turnpoint.getFrequency()
                                    , turnpoint.getDescription()));
                        default:
                            view.setText(context.getString(R.string.turnpoint_non_airport_info_window_text
                                    , turnpoint.getTitle(), turnpoint.getCode()
                                    , turnpoint.getStyleName()
                                    , turnpoint.getLatitudeDeg(), turnpoint.getLongitudeDeg()
                                    , turnpoint.getElevation()
                                    , turnpoint.getDescription()));
                    }

                } catch (NumberFormatException nfe) {
                    view.setText(context.getString(R.string.unknow_error_in_render));
                }

            } else if (marker.getTag() instanceof LatLngForecast) {
                LatLngForecast latLngForecast = (LatLngForecast) marker.getTag();
                view.setText(latLngForecast.getForecastText());
            }

        }
    }

    // ------------ Sounding markers
    private Bitmap createSoundingMarker(String location) {
        View soundingsPin;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        soundingsPin = inflater.inflate(R.layout.soundings_pin, null);
        TextView locationView = soundingsPin.findViewById(R.id.soundings_pin_location);
        locationView.setText(location.length() < 5 ? location : location.substring(0, 5));

        // zoomLevel may be 0 if camera hasn't moved to location yet, if 0 using 8 as default
        if (soundingsTextZoomLevel == 0 || (soundingsTextZoomLevel != zoomLevel && zoomLevel != 0 )) {
            soundingsTextZoomLevel = (zoomLevel == 0 ? 8 : zoomLevel);
            soundingsTextSize = soundingsTextZoomLevel <= 7 ? resources.getDimension(R.dimen.text_size_regular)
                    : resources.getDimension(R.dimen.text_size_extra_large);
            soundingsSkewTDrawable = createSoundingMarkerIcon(locationView, (int) soundingsTextSize, R.drawable.skew_t);
        }

        locationView.setTextSize(TypedValue.COMPLEX_UNIT_PX, soundingsTextSize);
        locationView.setCompoundDrawablesWithIntrinsicBounds(soundingsSkewTDrawable, null, null, null);

        soundingsPin.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT));
        soundingsPin.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //Assign a size and position to the view and all of its descendants
        soundingsPin.layout(0, 0, soundingsPin.getMeasuredWidth(), soundingsPin.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(soundingsPin.getWidth(), soundingsPin.getHeight(), Bitmap.Config.ARGB_8888);
        soundingsPin.layout(0, 0, soundingsPin.getMeasuredWidth(), soundingsPin.getMeasuredHeight());
        soundingsPin.draw(new Canvas(bitmap));
        return bitmap;
    }

    private ScaleDrawable createSoundingMarkerIcon(TextView textView, int size, @DrawableRes int drawable) {
        Drawable underlyingDrawable =
                new BitmapDrawable(resources, BitmapFactory.decodeResource(resources, drawable));

        // Wrap to scale up to the TextView height
        final ScaleDrawable scaledDrawable =
                new ScaleDrawable(underlyingDrawable, Gravity.CENTER, 1F, 1F) {
                    // Give this drawable a height being at
                    // TextView text size. It will be
                    // used by
                    // TextView.setCompoundDrawablesWithIntrinsicBounds
                    public int getIntrinsicHeight() {
                        return   size;
                    }

                    public int getIntrinsicWidth() {
                        return  size;
                    }
                };

        // Set explicitly level else the default value
        // (0) will prevent .draw to effectively draw
        // the underlying Drawable
        scaledDrawable.setLevel(10000);
        textView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                textView.setCompoundDrawablesWithIntrinsicBounds(scaledDrawable, null, null, null);
            }
        });
       return scaledDrawable;
    }


}
