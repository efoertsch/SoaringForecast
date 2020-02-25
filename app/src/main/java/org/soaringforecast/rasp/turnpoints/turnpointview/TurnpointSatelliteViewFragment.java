package org.soaringforecast.rasp.turnpoints.turnpointview;

import android.Manifest;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.databinding.TurnpointSatelliteView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;
import org.soaringforecast.rasp.turnpoints.edit.TurnpointEditViewModel;

import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.DaggerFragment;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * Display existing turnpoint location or
 * fire up GPS to find current location to assign to turnpoint
 * No viewmodel as initially rather simple fragment but need to reconsider if continue to adding
 * functionality
 */
public class TurnpointSatelliteViewFragment extends DaggerFragment implements OnMapReadyCallback
        , EasyPermissions.PermissionCallbacks {

    @Inject
    public TurnpointBitmapUtils turnpointBitmapUtils;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;


    private static final String TURNPOINT = "TURNPOINT";
    private ProgressBar progressBar;
    private static final int FINE_LOCATION_ACCESS = 2020;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location lastKnownLocation;
    private LatLng defaultLatLng = new LatLng(42.4259167, -71.7928611);
    private float currentZoom = 14f;
    private TurnpointEditViewModel turnpointEditViewModel;

    private GoogleMap googleMap;
    private String elevationPreference;
    TurnpointSatelliteView turnpointSatelliteView;
    private boolean inEditMode;
    private Marker turnpointMarker;
    private Bitmap turnpointMarkerBitmap;
    private boolean inDragMode = false;


    public static TurnpointSatelliteViewFragment newInstance(Turnpoint turnpoint) {
        TurnpointSatelliteViewFragment turnpointSatelliteViewFragment = new TurnpointSatelliteViewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TURNPOINT, turnpoint);
        turnpointSatelliteViewFragment.setArguments(bundle);
        return turnpointSatelliteViewFragment;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Viewmodel may already be defined if coming from TurnpointEditFragment
        // but data 'should' be the same
        turnpointEditViewModel = ViewModelProviders.of(getActivity())
                .get(TurnpointEditViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setTurnpoint(getArguments().getParcelable(TURNPOINT));
        setHasOptionsMenu(true);
        elevationPreference = appPreferences.getAltitudeDisplay();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        turnpointSatelliteView = DataBindingUtil.inflate(inflater, R.layout.turnpoint_satellite_view, container, false);
        turnpointSatelliteView.setLifecycleOwner(getViewLifecycleOwner());
        turnpointSatelliteView.setViewModel(turnpointEditViewModel);

        turnpointSatelliteView.turnpointMapProgressBar.setVisibility(View.VISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.turnpoint_map);
        mapFragment.getMapAsync(this);

        turnpointSatelliteView.turnpointMapDetails.setMovementMethod(new ScrollingMovementMethod());

        turnpointEditViewModel.getEditMode().observe(this, inEditMode -> {
            this.inEditMode = inEditMode;
            getActivity().invalidateOptionsMenu();
            if (inEditMode || (turnpointEditViewModel.getLatitudeDeg() == 0 && turnpointEditViewModel.getLongitudeDeg() == 0)) {
                getActivity().setTitle(R.string.edit_turnpoint);
                turnpointSatelliteView.turnpointMapCloseButton.setVisibility(View.GONE);
                turnpointSatelliteView.turnpointMapSaveButton.setVisibility(View.VISIBLE);
                turnpointSatelliteView.turnpointMapSaveButton.setOnClickListener(v -> {
                    removeFragment();
                });

                turnpointSatelliteView.turnpointMapCancelButton.setVisibility(View.VISIBLE);
                turnpointSatelliteView.turnpointMapCancelButton.setOnClickListener(v -> {
                    // close fragment
                    turnpointEditViewModel.resetTurnpointPosition();
                    removeFragment();

                });
                if ((turnpointEditViewModel.getLatitudeDeg() == 0 && turnpointEditViewModel.getLongitudeDeg() == 0)) {
                    mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
                    checkForGPSLocationPermission();
                }
                if (turnpointMarker != null) {
                    turnpointMarker.setDraggable(true);
                }
            } else {
                getActivity().setTitle(R.string.view_turnpoint);
                turnpointSatelliteView.turnpointMapCloseButton.setOnClickListener(v -> removeFragment());
            }
        });
        return turnpointSatelliteView.getRoot();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.turnpoint_view_options, menu);
        MenuItem resetMenuItem = menu.findItem(R.id.turnpoint_view_reset_marker);
        MenuItem dragMenuItem = menu.findItem(R.id.turnpoint_view_drag_marker);
        if (inEditMode) {
            dragMenuItem.setVisible(true);
            resetMenuItem.setVisible(true);
        } else {
            dragMenuItem.setVisible(false);
            resetMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.turnpoint_view_reset_marker:
                turnpointEditViewModel.resetTurnpointPosition();
                moveCameraToLatLng(new LatLng(turnpointEditViewModel.getLatitudeDeg(), turnpointEditViewModel.getLongitudeDeg()));
                return true;
            case R.id.turnpoint_view_drag_marker:
                inDragMode = true;
                addMarkerDragListener();
                return true;
            default:
                return false;
        }
    }

    private void removeFragment() {
        post(new PopThisFragmentFromBackStack());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (turnpointEditViewModel.getLatitudeDeg() != 0 || turnpointEditViewModel.getLongitudeDeg() != 0) {
            LatLng turnpointLatLng = new LatLng(turnpointEditViewModel.getLatitudeDeg(), turnpointEditViewModel.getLongitudeDeg());
            moveCameraToLatLng(turnpointLatLng);
        }
    }

    private void moveCameraToLatLng(LatLng latLng) {
        if (googleMap != null) {
//            if (turnpointMarkerBitmap == null) {
//                // really should just have to pass in turnpoint style but
//                turnpointMarkerBitmap = turnpointBitmapUtils.getSizedTurnpointBitmap(getContext(), turnpointEditViewModel.getTurnpoint(), (int) currentZoom);
//            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentZoom));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentZoom));
            googleMap.setOnCameraIdleListener(() -> {
                currentZoom = googleMap.getCameraPosition().zoom;
                Timber.d("Zoom level: %.4f", currentZoom);
            });
            turnpointSatelliteView.turnpointMapProgressBar.setVisibility(View.GONE);
            if (turnpointMarker == null) {
                turnpointMarker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng));
                //.icon(BitmapDescriptorFactory.fromBitmap(turnpointMarkerBitmap)));
            }
            turnpointMarker.setPosition(latLng);
            turnpointMarker.setDraggable(inEditMode);

        }
    }


    private void addMarkerDragListener() {
        if (googleMap != null) {
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker arg0) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    // Note that new location doesn't provide new elevation.
                    LatLng latLng = marker.getPosition();
                    turnpointEditViewModel.setLatitudeDeg(latLng.latitude);
                    turnpointEditViewModel.setLongitudeDeg(latLng.longitude);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    turnpointEditViewModel.getElevationAtLatLng(latLng);
                }

                @Override
                public void onMarkerDrag(Marker arg0) {
                }
            });
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
                    turnpointEditViewModel.setCurrentLocationFromGPS(lastKnownLocation);
                    LatLng currentLocationLatLng = new LatLng(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude());
                    moveCameraToLatLng(currentLocationLatLng);
                } else {
                    post(new SnackbarMessage(getString(R.string.could_not_get_lat_long_from_gps)));
                }
            } else {
                post(new SnackbarMessage(getString(R.string.can_not_determine_location)));
                Timber.d("Current location is null. Using defaults.");
                Timber.e(task.getException(), " Exception");
                moveCameraToLatLng(defaultLatLng);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });
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
                        post(new PopThisFragmentFromBackStack());
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }
    }

    private void post(Object object){
        EventBus.getDefault().post(object);
    }

}

