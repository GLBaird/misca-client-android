package org.qumodo.miscaclient.fragments;


import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.qumodo.data.models.MiscaImage;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.activities.MainActivity;
import org.qumodo.miscaclient.dataProviders.ImageListProvider;
import org.qumodo.miscaclient.dataProviders.LocationImageProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class QMiscaMapView extends Fragment implements OnMapReadyCallback, LocationImageProvider.LocationImageProviderListener, ClusterManager.OnClusterClickListener<MiscaImage>, ClusterManager.OnClusterItemClickListener<MiscaImage> {

    private Location userLocation;
    private GoogleApiClient googleApiClient;
    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private ClusterManager<MiscaImage> clusterManager;

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

    private void setupClusterManager(GoogleMap map) {
        clusterManager = new ClusterManager<>(getContext(), map);
        clusterManager.setAnimation(true);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qmisca_map_view, container, false);
        mapFragment = (MapFragment) getFragmentManager().findFragmentByTag("MAP_FRAGMENT");
        LocationImageProvider.addListener(this);

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

        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle("Location search");
        }

        return  view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MAP", "LOADED");
        this.googleMap = googleMap;
        setupClusterManager(googleMap);
        if (userLocation != null) {
            updatePositionOnMap();
        }
    }

    @Override
    public void locationImageProviderHasUpdatedWithData() {
        clusterManager.clearItems();
        clusterManager.addItems(LocationImageProvider.ITEMS);
        clusterManager.cluster();
    }

    @Override
    public void onDestroy() {
        LocationImageProvider.removeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onClusterClick(Cluster<MiscaImage> cluster) {
        ImageListProvider.setITEMS(new ArrayList<>(cluster.getItems()));
        Intent openImageList = new Intent();
        openImageList.setAction(MainActivity.ACTION_SHOW_IMAGE_GALLERY);
        getContext().sendBroadcast(openImageList);
        return true;
    }

    @Override
    public boolean onClusterItemClick(MiscaImage image) {
        Intent openImageView = new Intent();
        openImageView.setAction(MainActivity.ACTION_SHOW_IMAGE_VIEW);
        openImageView.putExtra(QImageViewFragment.INTENT_IMAGE_PATH, image.getPath());
        openImageView.putExtra(QImageViewFragment.INTENT_IMAGE_ID, image.getId());
        openImageView.putExtra(QImageViewFragment.INTENT_SERVICE, QImageViewFragment.IMAGE_SERVICE_CORE_IMAGE);
        getContext().sendBroadcast(openImageView);
        return true;
    }
}
