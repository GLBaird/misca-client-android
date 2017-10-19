package org.qumodo.miscaclient.fragments;


import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.qumodo.miscaclient.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class QMiscaMapView extends Fragment implements OnMapReadyCallback {

    Location userLocation;
    GoogleApiClient googleApiClient;
    MapFragment mapFragment;
    GoogleMap googleMap;

    public QMiscaMapView() {
        // Required empty public constructor
    }

    public void updateMapView(Location userLocation, GoogleApiClient googleApiClient) {
        Log.d("MAP", "Update map view");
        this.userLocation = userLocation;
        this.googleApiClient = googleApiClient;
        if (googleMap != null) {
            updatePositionOnMap();
        }
    }

    private LatLng getLocationPosition() {
        return new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
    }

    private MarkerOptions getUserPositionMarkerOptions() {
        return new MarkerOptions()
                .position(getLocationPosition())
                .title("User position");
    }

    Marker currentUserPosition;

    private void updatePositionOnMap() {
        if (currentUserPosition == null) {
            googleMap.addMarker(getUserPositionMarkerOptions());
        } else {
            currentUserPosition.setPosition(getLocationPosition());
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLocationPosition(), 15));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qmisca_map_view, container, false);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_container);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.map_container, mapFragment, "MAP_FRAGMENT")
                    .commit();
        } else {
            getFragmentManager()
                    .beginTransaction()
                    .attach(mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);

        return  view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MAP", "LOADED");
        this.googleMap = googleMap;
        if (userLocation != null) {
            updatePositionOnMap();
        }
    }
}
