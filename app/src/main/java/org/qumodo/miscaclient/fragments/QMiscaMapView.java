package org.qumodo.miscaclient.fragments;


import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class QMiscaMapView extends Fragment implements OnMapReadyCallback,
        LocationImageProvider.LocationImageProviderListener, ClusterManager.OnClusterClickListener<MiscaImage>,
        ClusterManager.OnClusterItemClickListener<MiscaImage>, TextView.OnEditorActionListener, GoogleMap.OnCameraIdleListener, View.OnClickListener, View.OnTouchListener {

    private Location userLocation;
    private GoogleApiClient googleApiClient;
    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private ClusterManager<MiscaImage> clusterManager;
    private ImageButton mapMode;
    private EditText searchBox;
    private float zoom = 15f;
    private View searchButtons;

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

    public boolean hasLocation() {
        return userLocation != null;
    }

    private LatLng getLocationPosition() {
        if (userLocation != null) {
            return new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        }

        return null;
    }


    private void updatePositionOnMap() {
        Log.d("MAP", "Checking update pos");
        if (!waitForMap) {
            Log.d("MAP", "UPDAT POS NOW");
            LatLng location = getLocationPosition();
            if (googleMap != null && location != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
            }
        }
    }

    ClusterRenderer<MiscaImage> clusterRenderer;
    Collection<MiscaImage> clusterItems = new ArrayList<>();

    private void setupClusterManager(GoogleMap map) {
        clusterManager = new ClusterManager<>(getContext(), map);
        clusterManager.setAnimation(true);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.addItems(clusterItems);
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
        searchBox.setOnTouchListener(this);

        searchButtons = view.findViewById(R.id.search_buttons);

        view.findViewById(R.id.button_object_search).setOnClickListener(this);
        view.findViewById(R.id.button_place_search).setOnClickListener(this);

        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle("Location search");
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MAP", "MAP LOADED");
        waitForMap = false;
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        View map_view = mapFragment.getView();
        if (map_view != null) {
            View location_button = map_view.findViewWithTag("GoogleMapMyLocationButton");
            View zoom_in_button = map_view.findViewWithTag("GoogleMapZoomInButton");
            View zoom_layout = (View) zoom_in_button.getParent();
            RelativeLayout.LayoutParams location_layout = (RelativeLayout.LayoutParams) location_button.getLayoutParams();
            location_layout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            location_layout.addRule(RelativeLayout.ABOVE, zoom_layout.getId());
        }

        setupClusterManager(googleMap);
        googleMap.setOnCameraIdleListener(this);

        updatePositionOnMap();
    }

    @Override
    public void locationImageProviderHasUpdatedWithData() {

        Collection<MiscaImage> items =  clusterManager.getAlgorithm().getItems();
        Map<String, MiscaImage> newItems = new HashMap<>(LocationImageProvider.ITEMS_MAP);

        for (MiscaImage item : items) {
            if (newItems.containsKey(item.getId())) {
                newItems.remove(item.getId());
            } else {
                clusterManager.getAlgorithm().removeItem(item);
            }
        }
        clusterManager.getAlgorithm().addItems(newItems.values());
        clusterManager.cluster();
    }

    private boolean waitForMap = false;

    @Override
    public void onDetach() {
        super.onDetach();
        googleMap = null;
        waitForMap = true;
        LocationImageProvider.removeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        userLocation = getCameraLocation();
        zoom = googleMap.getCameraPosition().zoom;
        clusterItems = clusterManager.getAlgorithm().getItems();
        Log.d("MAP", "STOP");
        super.onStop();
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
        if (view == null)
            view = new View(activity);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    String searchTerm;

    @Override
    public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
        if (actionID == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            searchBox.clearFocus();
            hideKeyboard(getActivity());
            searchTerm = searchBox.getText().toString();
            LocationImageProvider.getLocationObjectImages(getCameraLocation(), searchTerm, getCameraDistance(), getContext());
            return true;
        }

        return false;
    }

    private Location getCameraLocation() {
        LatLng position = googleMap.getCameraPosition().target;
        Location cameraLocation = new Location("");
        cameraLocation.setLatitude(position.latitude);
        cameraLocation.setLongitude(position.longitude);
        return cameraLocation;
    }

    private String getCameraDistance() {
        LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng ne = bounds.northeast;
        LatLng sw = bounds.southwest;

        Location locationA = new Location("point A");
        locationA.setLatitude(ne.latitude);
        locationA.setLongitude(ne.longitude);

        Location locationB = new Location("point B");
        locationB.setLatitude(sw.latitude);
        locationB.setLongitude(sw.longitude);

        float distance = locationA.distanceTo(locationB);

        return distance + "m";
    }

    @Override
    public void onCameraIdle() {
        Log.d("MAP", "Camera IDLE Zoom " + googleMap.getCameraPosition().zoom);
        userLocation = getCameraLocation();
        String distance = getCameraDistance();
       if (searchTerm != null) {
           LocationImageProvider.getLocationObjectImages(userLocation, searchTerm, distance, getContext());
       } else {
           LocationImageProvider.getLocationImages(userLocation, distance, getContext());
       }
    }

    private void openPlaceSearch() {
        Intent openPlaceSearch = new Intent();
        openPlaceSearch.setAction(MainActivity.ACTION_SHOW_PLACE_SEARCH);
        getContext().sendBroadcast(openPlaceSearch);
    }

    Marker currentPlace;

    public void addPlaceSearchResult(Place place) {
        Log.d("MAP", "Place found " + place.getName());
        if (currentPlace != null) {
            currentPlace.remove();
        }

        String title = "Result";
        CharSequence name = place.getName();
        if (name != null)
            title = name.toString();

        MarkerOptions options = new MarkerOptions().position(place.getLatLng()).title(title);
        currentPlace = googleMap.addMarker(options);
        currentPlace.showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(), 1));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_object_search:
                searchButtons.setVisibility(View.GONE);
                searchBox.setText("");
                searchBox.setVisibility(View.VISIBLE);
                break;
            case R.id.button_place_search:
                openPlaceSearch();
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view instanceof EditText) {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (motionEvent.getRawX() >= (searchBox.getRight() - searchBox.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    searchTerm = null;
                    searchBox.setVisibility(View.INVISIBLE);
                    searchButtons.setVisibility(View.VISIBLE);
                    LocationImageProvider.getLocationImages(getCameraLocation(), getCameraDistance(), getContext());
                    return true;
                }
            }
        }
        return false;
    }
}
