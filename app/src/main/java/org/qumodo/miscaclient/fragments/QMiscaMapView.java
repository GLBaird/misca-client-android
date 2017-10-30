package org.qumodo.miscaclient.fragments;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.google.maps.android.clustering.view.ClusterRenderer;

import org.qumodo.data.models.MiscaImage;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.activities.ImageViewActivity;
import org.qumodo.miscaclient.activities.MainActivity;
import org.qumodo.miscaclient.dataProviders.ImageListProvider;
import org.qumodo.miscaclient.dataProviders.LocationImageProvider;
import org.qumodo.miscaclient.renderers.MapClusterRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class QMiscaMapView extends Fragment implements OnMapReadyCallback,
        LocationImageProvider.LocationImageProviderListener, ClusterManager.OnClusterClickListener<MiscaImage>,
        ClusterManager.OnClusterItemClickListener<MiscaImage>, TextView.OnEditorActionListener {

    private Location userLocation;
    private GoogleApiClient googleApiClient;
    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private ClusterManager<MiscaImage> clusterManager;
    private ImageButton mapMode;
    private EditText searchBox;

    public QMiscaMapView() {
        // Required empty public constructor
    }



    public void updateMapView(Location userLocation, GoogleApiClient googleApiClient) {
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
        if (!waitForMap) {
            LatLng location = getLocationPosition();
            if (currentUserPosition == null && location != null) {
                MarkerOptions markerOptions = getUserPositionMarkerOptions(location);
                currentUserPosition = googleMap.addMarker(markerOptions);
            } else if (currentUserPosition != null && location != null) {
                currentUserPosition.setPosition(location);
            }
            if (googleMap != null && location != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLocationPosition(), 15));
            }
            if (userLocation != null && searchTerm == null) {
                LocationImageProvider.getLocationImages(userLocation, getContext());
            } else if (userLocation != null) {
                LocationImageProvider.getLocationObjectImages(userLocation, searchTerm, getContext());
            }
        }
    }

    ClusterRenderer<MiscaImage> clusterRenderer;

    private void setupClusterManager(GoogleMap map) {
        clusterManager = new ClusterManager<>(getContext(), map);
        clusterManager.setAnimation(true);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        clusterRenderer = new MapClusterRenderer(getContext(), googleMap, clusterManager);
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
            mapFragment = MapFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.map_container, mapFragment, "MAP_FRAGMENT")
                    .commit();
        } else {
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

        mapMode = view.findViewById(R.id.map_mode_toggle);
        mapMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMapMode();
            }
        });

        searchBox = view.findViewById(R.id.et_object_search);
        searchBox.setOnEditorActionListener(this);

        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle("Location search");
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        waitForMap = false;
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

    private boolean waitForMap = false;

    @Override
    public void onDetach() {
        super.onDetach();
        currentUserPosition = null;
        googleMap = null;
        waitForMap = true;
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
        Intent openImageView = new Intent(getContext(), ImageViewActivity.class);
        openImageView.putExtra(ImageViewActivity.INTENT_IMAGE_PATH, image.getPath());
        openImageView.putExtra(ImageViewActivity.INTENT_IMAGE_ID, image.getId());
        openImageView.putExtra(ImageViewActivity.INTENT_SERVICE, QImageViewFragment.IMAGE_SERVICE_CORE_IMAGE);
        getContext().startActivity(openImageView);
        return true;
    }

    private void toggleMapMode() {
        if (googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mapMode.setImageResource(R.drawable.ic_satellite_blue_24dp);
        } else if (googleMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mapMode.setImageResource(R.drawable.ic_satellite_black_24dp);
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    String searchTerm;

    @Override
    public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
        if (actionID == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            searchBox.clearFocus();
            hideKeyboard(getActivity());
            searchTerm = searchBox.getText().toString();
            LocationImageProvider.getLocationObjectImages(userLocation, searchTerm, getContext());
            return true;
        }

        return false;
    }

}
