package org.soaringforecast.rasp.turnpoints.turnpointview;

import android.Manifest;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;

import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import dagger.android.support.DaggerFragment;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * Display existing turnpoint location or
 * fire up GPS to find current location to assign to turnpoint
 */
public class TurnpointSatelliteViewFragment extends DaggerFragment implements OnMapReadyCallback
        , EasyPermissions.PermissionCallbacks {

    private Turnpoint turnpoint;
    private ProgressBar progressBar;
    private boolean findGPSLocation = false;
    private static final int FINE_LOCATION_ACCESS = 2020;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location lastKnownLocation;
    private LatLng defaultLatLng = new LatLng(42.4259167, -71.7928611);
    private LatLng currentLocationLatLng = defaultLatLng;
    private static final int DEFAULT_ZOOM = 8;


    @Inject
    public TurnpointBitmapUtils turnpointBitmapUtils;
    private GoogleMap googleMap;


    public static TurnpointSatelliteViewFragment newInstance(Turnpoint turnpoint) {
        TurnpointSatelliteViewFragment turnpointSatelliteViewFragment = new TurnpointSatelliteViewFragment();
        turnpointSatelliteViewFragment.setTurnpoint(turnpoint);
        return turnpointSatelliteViewFragment;
    }

    private void setTurnpoint(Turnpoint turnpoint) {
        this.turnpoint = turnpoint;
    }

    // Set a flag to fire up GPS to get current location
    public TurnpointSatelliteViewFragment useGPSToFindCurrentLocation() {
        findGPSLocation = true;
        return this;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.turnpoint_view, container, false);
        progressBar = view.findViewById(R.id.turnpoint_map_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.turnpoint_map);
        mapFragment.getMapAsync(this);
        view.findViewById(R.id.turnpoint_map_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFragment();
            }
        });
        TextView turnpointView = view.findViewById(R.id.turnpoint_map_details);
        turnpointView.setText(turnpoint.getFormattedTurnpointDetails());
        turnpointView.setMovementMethod(new ScrollingMovementMethod());

        if (findGPSLocation) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            checkForGPSLocationPermission();
        }
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private void removeFragment() {
        EventBus.getDefault().post(new IAmDone());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        LatLng turnppointLatLng = new LatLng(turnpoint.getLatitudeDeg(), turnpoint.getLongitudeDeg());
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(turnppointLatLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(turnppointLatLng, 14f));
        googleMap.setOnCameraIdleListener(() ->
                Timber.d("Zoom level: %1$d", (int) googleMap.getCameraPosition().zoom));
        progressBar.setVisibility(View.GONE);
        if (findGPSLocation) {
            // Prompt the user for permission.
            checkForGPSLocationPermission();
        }
    }

    private void getCurrentLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = task.getResult();
                if (lastKnownLocation != null) {
                    currentLocationLatLng =  new LatLng(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude());
                    moveCameraToLatLng(currentLocationLatLng);
                    turnpoint.setLatitudeDeg(currentLocationLatLng.latitude);
                    turnpoint.setLongitudeDeg(currentLocationLatLng.longitude);
                }
            } else {
                Timber.d("Current location is null. Using defaults.");
                Timber.e(task.getException(), " Exception");
                moveCameraToLatLng(defaultLatLng);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });
    }

    private void moveCameraToLatLng(LatLng latLng) {
        if (googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    latLng, DEFAULT_ZOOM));
        }
        Bitmap turnpointBitmap = turnpointBitmapUtils.getSizedTurnpointBitmap(getContext(), turnpoint, DEFAULT_ZOOM);
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(turnpointBitmap)));
    }


    //----- GPS Permission stuff
    private void checkForGPSLocationPermission() {
        // Check for permission to read downloads directory
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            getCurrentLocation();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_find_current_location), FINE_LOCATION_ACCESS, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Timber.d("Permission has been granted");
        if (requestCode == FINE_LOCATION_ACCESS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            getCurrentLocation();
        }
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == FINE_LOCATION_ACCESS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Timber.d("Permission has been denied");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.no_permission_to_use_gps_to_get_current_location)
                    .setTitle(R.string.permission_denied)
                    .setPositiveButton(R.string.ok, (dialog, id) -> {
                        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }
    }

}

