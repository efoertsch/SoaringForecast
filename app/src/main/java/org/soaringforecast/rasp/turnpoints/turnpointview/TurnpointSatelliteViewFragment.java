package org.soaringforecast.rasp.turnpoints.turnpointview;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;

import java.util.List;

import javax.inject.Inject;

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
    //private FusedLocationProviderClient mFusedLocationProviderClient;

    @Inject
    public TurnpointBitmapUtils turnpointBitmapUtils;

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
            checkForGPSLocationPermission();
        }
        return view;
    }

    private void checkForGPSLocationPermission() {
        // Check for permission to read downloads directory
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            turnOnGPSToFindCurrentLocation();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_find_current_location), FINE_LOCATION_ACCESS, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void turnOnGPSToFindCurrentLocation() {

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
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        LatLng turnppointLatLng = new LatLng(turnpoint.getLatitudeDeg(), turnpoint.getLongitudeDeg());
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(turnppointLatLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(turnppointLatLng, 14f));
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Timber.d("Zoom level: %1$d", (int) googleMap.getCameraPosition().zoom);
            }
        });

        Bitmap turnpointBitmap = turnpointBitmapUtils.getSizedTurnpointBitmap(getContext(), turnpoint, 8);
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(turnppointLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(turnpointBitmap)));

        progressBar.setVisibility(View.GONE);
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
            turnOnGPSToFindCurrentLocation();
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

