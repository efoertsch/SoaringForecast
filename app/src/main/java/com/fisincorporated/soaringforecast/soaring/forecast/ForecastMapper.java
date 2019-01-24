package com.fisincorporated.soaringforecast.soaring.forecast;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.fisincorporated.soaringforecast.messages.DisplaySounding;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.soaring.json.Sounding;
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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class ForecastMapper implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private boolean drawingTask = false;

    //Default for NewEngland - revise if new regions become available
//    private LatLngBounds mapLatLngBounds = new LatLngBounds(new LatLng(41.2665329, -73.6473083)
//            , new LatLng(45.0120811, -70.5046997));
    private LatLngBounds mapLatLngBounds ;
    private List<Sounding> soundings = new ArrayList<>();
    private List<TaskTurnpoint> taskTurnpoints = new ArrayList<>();

    private List<Marker> taskTurnpointMarkers = new ArrayList<>();
    private List<Polyline> taskTurnpointLines = new ArrayList<>();
    private List<Marker> soundingMarkers = new ArrayList<>();

    private GoogleMap googleMap;

    private GroundOverlay forecastOverlay;
    private int forecastOverlayOpacity;
    private Marker lastMarkerOpened;

    /**
     * Use to center task route in googleMap frame
     */
    private LatLng southwest = null;
    private double swLat = 0;
    private double swLong = 0;
    private double neLat = 0;
    private double neLong = 0;


    @Inject
    public ForecastMapper() {
    }

    public void displayMap(SupportMapFragment mapFragment) {

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // if delay in map getting ready and bounds, sounding locations or task already passed in display them as
        // required
        googleMap.setOnMarkerClickListener(this);
        updateMapBounds();
        displaySoundingMarkers(true);
        createSoundingMarkers();
        plotTaskTurnpoints();
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

        if (marker.getTag() == null) {
            // tag null if task marker
            lastMarkerOpened = marker;
            marker.showInfoWindow();
            return true;
        } else {
            EventBus.getDefault().post(new DisplaySounding((Sounding) marker.getTag()));
            return true;
        }
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
                    placeTaskTurnpointMarker(taskTurnpoint.getTitle()
                            , String.format("%1$.1fkm", taskTurnpoint.getDistanceFromStartingPoint()), fromLatLng);
                } else {
                    toLatLng = new LatLng(taskTurnpoint.getLatitudeDeg(), taskTurnpoint.getLongitudeDeg());
                    placeTaskTurnpointMarker(taskTurnpoint.getTitle(),
                            String.format("%1$.1fkm", taskTurnpoint.getDistanceFromStartingPoint()), toLatLng);
                    drawLine(fromLatLng, toLatLng);
                    fromLatLng = toLatLng;
                }
                updateMapLatLongCorners(fromLatLng);
            }
            LatLng southwest = new LatLng(swLat, swLong);
            LatLng northeast = new LatLng(neLat, neLong);
            googleMap.setOnMapLoadedCallback(() ->
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                    southwest, northeast), 60)));
        }
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

    private void placeTaskTurnpointMarker(String title, String snippet, LatLng latLng) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .title(title)
                .snippet(snippet)
                .position(latLng));
        taskTurnpointMarkers.add(marker);
    }

}
