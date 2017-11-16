package org.qumodo.miscaclient.dataProviders;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationProvider implements LocationListener {

    private static LocationProvider locationProvider;

    public static LocationProvider getSharedLocationProvider() {
        if (locationProvider == null) {
            locationProvider = new LocationProvider();
        }

        return locationProvider;
    }

    private LocationProvider() {}

    private Location location;
    private GoogleApiClient googleApiClient;

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Nullable
    public LatLng getCurrentLocation() {
        if (location == null) {
            return null;
        }
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public Location getAllLocationData() {
        return location;
    }

    public LocationProvider setApiClient(GoogleApiClient client) {
        googleApiClient = client;
        return this;
    }

    public void updateLocation(Context context, boolean fakeLocation) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && !fakeLocation) {
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(500);
            locationRequest.setFastestInterval(1);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
        } else {
            location = new Location("");
            location.setLatitude(51.508515);
            location.setLongitude(-0.099034);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }
}
