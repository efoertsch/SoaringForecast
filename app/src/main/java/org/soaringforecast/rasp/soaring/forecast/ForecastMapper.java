package org.soaringforecast.rasp.soaring.forecast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntegerRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
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
import org.json.JSONException;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.messages.DisplaySounding;
import org.soaringforecast.rasp.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.json.Sounding;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.ViewUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Responsible for handling map display
 */
public class ForecastMapper implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

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
    // MapType must be one of GoogleMap.MAP_TYPE_xxxx
    private int mapType = GoogleMap.MAP_TYPE_TERRAIN;

    @Inject
    public ForecastMapper() {
    }

    public ForecastMapper setContext(Context context) {
        this.context = context;
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
                zoomLevel = (int) googleMap.getCameraPosition().zoom;
                Timber.d("Zoom level: %1$d", (int) googleMap.getCameraPosition().zoom);
                mapTurnpoints();

            }
        });
        // if delay in map getting ready and bounds, sounding locations or task already passed in display them as
        // required
        googleMap.setOnMarkerClickListener(this);
        googleMap.setInfoWindowAdapter(new TurnpointInfoWindowAdapter());
        updateMapBounds();
        displaySoundingMarkers(true);
        createSoundingMarkers();
        plotTaskTurnpoints();
    }

    public void setMapType(int mapType){
        this.mapType = mapType;
        if (googleMap != null){
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
            displaySoundingMarkers(false);
        } else {
            this.soundings.addAll(soundings);
            createSoundingMarkers();
        }
    }

    private void createSoundingMarkers() {
        if (googleMap == null) {
            return;
        }
        soundingMarkers.clear();
        LatLng latLng;
        Marker marker;
        for (Sounding sounding : soundings) {
            latLng = new LatLng(sounding.getLat(), sounding.getLng());
            marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                    .title(sounding.getLocation()));
            soundingMarkers.add(marker);
            marker.setTag(sounding);
            displaySoundingMarkers(true);
        }
    }

    private void displaySoundingMarkers(boolean display) {
        if (soundingMarkers != null && soundingMarkers.size() > 0) {
            for (Marker marker : soundingMarkers) {
                marker.setVisible(display);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Check if there is an open info window (currently would just be taskmarker)
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

    // ------ Task Turnpoints ---------------------------------
    // TODO Simplify(?) just pass in task id and get turnpoints here.
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
            // after layer removed from map
            //geoJsonLayer.setOnFeatureClickListener(geoJsonOnFeatureClickListener);
        } catch (IOException e) {
            postError(e, suaRegionName);
        } catch (JSONException e) {
            postError(e, suaRegionName);
        }

        geoJsonLayer.addLayerToMap();
    }

    private GeoJsonLayer.GeoJsonOnFeatureClickListener geoJsonOnFeatureClickListener = feature -> {
        displaSuaDetails(feature);
    };

    private void displaSuaDetails(Feature feature) {
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
            builder.show();
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

    //------------------------------------------------------------------------------------
    class TurnpointInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View turnpointInfoWindowView;
        private final TextView turnpointInfoWindowInfo;

        TurnpointInfoWindowAdapter() {
            turnpointInfoWindowView =((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.turnpoint_infowindow, null);
            turnpointInfoWindowInfo = turnpointInfoWindowView.findViewById(R.id.turnpoint_infowindow_info);
            turnpointInfoWindowInfo.setMovementMethod(new ScrollingMovementMethod());
        }

        public View getInfoWindow(Marker marker) {
            render(marker, turnpointInfoWindowInfo);
            return turnpointInfoWindowView;
        }

        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, TextView view) {
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

        }
    }

}
