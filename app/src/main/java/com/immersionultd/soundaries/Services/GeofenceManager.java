package com.immersionultd.soundaries.Services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.immersionultd.soundaries.Objects.Soundary;

import java.util.ArrayList;

/**
 * Created by YonahKarp on 6/13/17.
 */
public class GeofenceManager implements ResultCallback<Status> {

    private GoogleApiClient mGoogleApiClient;

    public GeofenceManager(GoogleApiClient mGoogleApiClient){
        this.mGoogleApiClient = mGoogleApiClient;
    }


    //public methods
    public boolean addGeofenceToListeningService(Context context, Soundary soundary){

        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(context, "Google API Client not connected!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return actuallyAddGeofences(context, soundary);

    }

    public boolean removeGeofenceFromListeningService(Context context, String geofenceName){

        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(context, "Google API Client not connected!", Toast.LENGTH_SHORT).show();
            return false;
        }
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, convertToArrayList(geofenceName));
        return true;
    }


    //private methods
    private boolean actuallyAddGeofences(Context context, Soundary soundary){
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    buildGeofenceRequest(convertToArrayList(soundary.getGeofence())),
                    getGeofencePendingIntent(context, soundary)
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            Toast.makeText(context, "Security Issue. Could not add soundary!", Toast.LENGTH_SHORT).show();
            return false; //failure
        }
        return true; //success
    }

    private GeofencingRequest buildGeofenceRequest(ArrayList<Geofence> fences){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(fences);
        return  builder.build();
    }

    //pending intent is kicked off when geofence event is triggered
    private PendingIntent getGeofencePendingIntent(Context context, Soundary soundary){
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);

        intent.putExtra("volume", soundary.getVolume());

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.println(Log.DEBUG, "tag", "onResult after add: " + status);
    }

    private ArrayList<Geofence> convertToArrayList(Geofence geofence){
        ArrayList<Geofence> tempList = new ArrayList<>();
        tempList.add(geofence);
        return tempList;
    }

    private ArrayList<String> convertToArrayList(String string){
        ArrayList<String> tempList = new ArrayList<>();
        tempList.add(string);
        return tempList;
    }
}
