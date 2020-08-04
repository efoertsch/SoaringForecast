package org.soaringforecast.rasp.landout;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.databinding.LandoutViewBinding;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import dagger.android.support.DaggerFragment;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class LandoutFragment extends DaggerFragment implements OnMapReadyCallback
        , EasyPermissions.PermissionCallbacks {

    private GoogleMap googleMap;
    private float currentZoom = 14f;
    private static final int FINE_LOCATION_ACCESS = 2020;
    private static final int SEND_SMS = 3333;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker landoutMarker;
    private LatLng currentLatLng;
    private LandoutViewBinding landoutViewBinding;
    private static String googleSmsLatLng = "http://maps.google.com/?q=%1$f,%2$f";

    public static LandoutFragment newInstance() {
        return new LandoutFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        landoutViewBinding = DataBindingUtil.inflate(inflater, R.layout.landout_fragment, container, false);
        landoutViewBinding.setLifecycleOwner(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        checkForGPSLocationPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.landout_map);
        mapFragment.getMapAsync(this);
        landoutViewBinding.landoutSendSmsBtn.setOnClickListener(v -> checkForSMSPermission()
        );

        return landoutViewBinding.getRoot();
    }

    private void sendLocationViaSMS() {
        String smsText = String.format(googleSmsLatLng, currentLatLng.latitude, currentLatLng.longitude) ;
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:"));
        sendIntent.putExtra("sms_body", smsText);
        startActivity(sendIntent);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        moveCameraToLatLng(currentLatLng);
    }

    private void moveCameraToLatLng(LatLng currentLatLng) {
        if (googleMap != null && currentLatLng != null ) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, currentZoom));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, currentZoom));
            googleMap.setOnCameraIdleListener(() -> {
                currentZoom = googleMap.getCameraPosition().zoom;
                Timber.d("Zoom level: %.4f", currentZoom);
            });

            if (landoutMarker == null) {
                landoutMarker = googleMap.addMarker(new MarkerOptions()
                        .position(currentLatLng));
            }
            landoutMarker.setPosition(currentLatLng);
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
                Location currentLocation = task.getResult();
                if (currentLocation != null) {
                     currentLatLng = new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude());
                    moveCameraToLatLng(currentLatLng);
                    displayLatLng();
                    landoutViewBinding.landoutSendSmsBtn.setEnabled(true);
                } else {
                    post(new SnackbarMessage(getString(R.string.could_not_get_lat_long_from_gps)));
                }
            } else {
                post(new SnackbarMessage(getString(R.string.can_not_determine_location)));
                Timber.d("Current location is null. Using defaults.");
                Timber.e(task.getException(), " Exception");
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });
    }

    private void displayLatLng() {
        landoutViewBinding.landoutGpsLocation.setText(getString(R.string.landout_gps_latlng, currentLatLng.latitude, currentLatLng.longitude));
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

    //----- SMS Permission stuff
    private void checkForSMSPermission() {
        // Check for permission to read downloads directory
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.SEND_SMS)) {
            sendLocationViaSMS();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_send_location_via_sms), SEND_SMS, Manifest.permission.SEND_SMS);
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

        if (requestCode == SEND_SMS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.SEND_SMS))
            sendLocationViaSMS();
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

        if (requestCode == SEND_SMS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.SEND_SMS)) {
            Timber.d("Permission has been denied");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.no_permission_to_send_loaction_via_sms)
                    .setTitle(R.string.permission_denied)
                    .setPositiveButton(R.string.ok, (dialog, id) -> {
                        post(new PopThisFragmentFromBackStack());
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }
    }

    private void post(Object object) {
        EventBus.getDefault().post(object);
    }
}
