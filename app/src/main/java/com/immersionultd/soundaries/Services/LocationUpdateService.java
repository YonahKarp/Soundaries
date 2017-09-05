package com.immersionultd.soundaries.Services;

/**
 * Created by YonahKarp on 6/18/17.
 */

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class LocationUpdateService extends Service {
    private static final String TAG = "SOUNDARIES:TESTGPS";
    private LocationManager mLocationManager = null;
    private int LOCATION_INTERVAL = 60000;
    private float LOCATION_DISTANCE = 5f;
    private boolean shouldPollForLocation = StoredData.shouldUseLocationPolling;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
//            if(location.getProvider().equals(LocationManager.GPS_PROVIDER) && location.getAccuracy() < 30)
//                removeUpdatesForListener(0);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        //todo requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper)
        //todo timed function to run and re-evaluate which to use
        Log.e(TAG, "onCreate");

        initializeLocationManager();


        if (shouldPollForLocation)
            initializeProviderRequests(LocationManager.GPS_PROVIDER, 0);
        initializeProviderRequests(LocationManager.NETWORK_PROVIDER, 1);

    }

    private void initializeProviderRequests(String provider, int listenerIndex){
        try {
            mLocationManager.requestLocationUpdates(provider, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[listenerIndex]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, provider + " provider does not exist " + ex.getMessage());
        }
    }

    private void removeUpdatesForListener(int index){
        try {
            mLocationManager.removeUpdates(mLocationListeners[index]);
        } catch (Exception ex) {
            Log.i(TAG, "fail to remove location listners, ignore", ex);
        }
    }

        @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        StoredData storedData = new StoredData(getApplicationContext(), null);
        LOCATION_INTERVAL = storedData.updateTimeMilliseconds;
        LOCATION_DISTANCE = storedData.updateRadiusMeters;
        shouldPollForLocation = storedData.shouldUseLocationPolling;
    }
}
