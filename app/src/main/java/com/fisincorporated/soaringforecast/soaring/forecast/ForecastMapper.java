package com.fisincorporated.soaringforecast.soaring.forecast;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.fisincorporated.soaringforecast.messages.DisplaySoundingLocation;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.soaring.json.SoundingLocation;
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

    private SupportMapFragment supportMapFragment;
    private boolean drawingTask;

    private List<Marker> taskTurnpointMarkers = new ArrayList<>();
    private List<Polyline> taskTurnpointLines = new ArrayList<>();
    private List<Marker> soundingMarkers = new ArrayList<>();

    private GoogleMap googleMap;

    // Default for NewEngland
    private LatLngBounds mapLatLngBounds = new LatLngBounds( new LatLng(41.2665329, -73.6473083)
            ,new LatLng(45.0120811, -70.5046997));
    private GroundOverlay forecastOverlay;
    private int forecastOverlayOpacity;

    /**
     * Use to center task route in googleMap frame
     */
    private LatLng southwest = null;
    private double swLat = 0;
    private double swLong = 0;
    private double neLat = 0;
    private double neLong = 0;
    private List<TaskTurnpoint> taskTurnpoints;

    @Inject
    public ForecastMapper() {
    }

    public void displayMap(SupportMapFragment mapFragment) {
        mapFragment.getMapAsync(this);
    }


    private void setMapBounds(LatLng southWestLatLng, LatLng northEastLatLng) {
        setMapLatLngBounds(new LatLngBounds(southWestLatLng, northEastLatLng));
    }

    public void setMapLatLngBounds(LatLngBounds mapLatLngBounds) {
        this.mapLatLngBounds = mapLatLngBounds;
        setupMap();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

    }


    private void setupMap() {
        if (mapLatLngBounds != null) {
            googleMap.setLatLngBoundsForCameraTarget(mapLatLngBounds);
        }
        if (!drawingTask ) {
            // if drawing task use the task latlng bounds for map positioning
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapLatLngBounds, 0));
        }
    }

    public void setForecastOverlayOpacity(int forecastOverlayOpacity) {
        this.forecastOverlayOpacity = forecastOverlayOpacity;
    }

    // ---- Forecast overlay ------------------------------------
    //
    public void setGroundOverlay(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        if (forecastOverlay == null) {
            GroundOverlayOptions forecastOverlayOptions = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .positionFromBounds(mapLatLngBounds);
            forecastOverlayOptions.transparency(1.0f - forecastOverlayOpacity / 100.0f);
            forecastOverlay = googleMap.addGroundOverlay(forecastOverlayOptions);
        } else {
            forecastOverlay.setTransparency(1.0f - forecastOverlayOpacity / 100.0f);
            forecastOverlay.setImage(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }

    // ----- Sounding markers ----------------------------------------
    public void setSoundingLocations(List<SoundingLocation> soundingLocations) {
        if (soundingLocations == null || soundingLocations.size() == 0) {
            displaySoundingMarkers(false);
        } else {
            soundingMarkers.clear();
            LatLng latLng;
            Marker marker;
            for (SoundingLocation soundingLocation : soundingLocations) {
                latLng = new LatLng(soundingLocation.getLatitude(), soundingLocation.getLongitude());
                marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                        .title(soundingLocation.getLocation()));
                soundingMarkers.add(marker);
                marker.setTag(soundingLocation);
                googleMap.setOnMarkerClickListener(this);
                displaySoundingMarkers(true);
            }
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
        if (marker.getTag() == null) {
            // a click on task marker causes onMarkerClick to fire, even though task marker
            // onClickListener not assigned. Go figure.
            return false;
        }
        EventBus.getDefault().post(new DisplaySoundingLocation((SoundingLocation) marker.getTag()));
        return true;
    }

    // ------ Task Turnpoints ---------------------------------
    public void setTaskTurnpoints(List<TaskTurnpoint> taskTurnpoints) {
        this.taskTurnpoints = taskTurnpoints;
        plotTurnpointsOnForecast(taskTurnpoints);
    }

    private void plotTurnpointsOnForecast(List<TaskTurnpoint> taskTurnpoints) {
        TaskTurnpoint taskTurnpoint;
        LatLng fromLatLng = new LatLng(0d, 0d); // to get rid of syntax checker
        LatLng toLatLng;

        if (googleMap != null) {

            if (taskTurnpoints != null && taskTurnpoints.size() > 0) {
                int numberTurnpoints = taskTurnpoints.size();
                for (int i = 0; i < taskTurnpoints.size(); ++i) {
                    taskTurnpoint = taskTurnpoints.get(i);
                    if (i == 0) {
                        fromLatLng = new LatLng(taskTurnpoint.getLatitudeDeg(), taskTurnpoint.getLongitudeDeg());
                        placeTaskTurnpointMarker(taskTurnpoint.getTitle(), "Start", fromLatLng);

                    } else {
                        toLatLng = new LatLng(taskTurnpoint.getLatitudeDeg(), taskTurnpoint.getLongitudeDeg());
                        placeTaskTurnpointMarker(taskTurnpoint.getTitle(), (i < numberTurnpoints - 1 ? String.format("%1$.1fkm", taskTurnpoint.getDistanceFromStartingPoint()) : "Finish"), toLatLng);
                        drawLine(fromLatLng, toLatLng);
                        fromLatLng = toLatLng;
                    }
                    updateMapLatLongCorners(fromLatLng);
                }
                LatLng southwest = new LatLng(swLat, swLong);
                LatLng northeast = new LatLng(neLat, neLong);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                        southwest, northeast), 700, 700, 0));
            }

        }
    }

    /**
     * Find the most southwest and northeast task lat/long
     *
     * @param latLng
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

}
