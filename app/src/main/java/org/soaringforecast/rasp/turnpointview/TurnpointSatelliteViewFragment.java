package org.soaringforecast.rasp.turnpointview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.utils.BitmapImageUtils;

import timber.log.Timber;

public class TurnpointSatelliteViewFragment extends Fragment implements OnMapReadyCallback {

    private Turnpoint turnpoint;
    private ProgressBar progressBar;

    public static TurnpointSatelliteViewFragment newInstance(Turnpoint turnpoint) {
        TurnpointSatelliteViewFragment turnpointSatelliteViewFragment = new TurnpointSatelliteViewFragment();
        turnpointSatelliteViewFragment.setTurnpoint(turnpoint);
        return turnpointSatelliteViewFragment;
    }

    private void setTurnpoint(Turnpoint turnpoint) {
        this.turnpoint = turnpoint;
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

        Bitmap turnpointBitmap = BitmapImageUtils.getBitmapFromVectorDrawable(getContext(), R.drawable.ic_turnpoint);
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(turnppointLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(turnpointBitmap)));

        progressBar.setVisibility(View.GONE);
    }

}

