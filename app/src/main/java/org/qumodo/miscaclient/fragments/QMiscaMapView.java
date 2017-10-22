package org.qumodo.miscaclient.fragments;


import android.graphics.BitmapFactory;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.LocationImageProvider;

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
        Log.d("MAP", "Update map view " + userLocation);
        this.userLocation = userLocation;
        this.googleApiClient = googleApiClient;
        if (googleMap != null) {
            updatePositionOnMap();
        }
    }

    private LatLng getLocationPosition() {
        if (userLocation != null) {
            return new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        }

        return null;
    }

    private MarkerOptions getUserPositionMarkerOptions(LatLng position) {
        return new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location))
                .anchor(0.5f, 0.5f)
                .title("User position");
    }

    Marker currentUserPosition;

    private void updatePositionOnMap() {
        LatLng location = getLocationPosition();
        if (currentUserPosition == null && location != null) {
            MarkerOptions markerOptions = getUserPositionMarkerOptions(location);
            currentUserPosition = googleMap.addMarker(markerOptions);
        } else if (currentUserPosition != null && location != null) {
            currentUserPosition.setPosition(location);
        }
        if (googleMap!= null && location != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLocationPosition(), 15));
        }

        LocationImageProvider.getLocationImages(userLocation, getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qmisca_map_view, container, false);
        mapFragment = (MapFragment) getFragmentManager().findFragmentByTag("MAP_FRAGMENT");

        if (mapFragment == null) {
            Log.d("MAP", "creating new map fragment");
            mapFragment = MapFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.map_container, mapFragment, "MAP_FRAGMENT")
                    .commit();
        } else {
            Log.d("MAP", "Attaching existing fragment");
            MapFragment newFrag = MapFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .remove(mapFragment)
                    .add(R.id.map_container, newFrag, "MAP_FRAGMENT")
                    .commit();
            updatePositionOnMap();
            mapFragment = newFrag;
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
