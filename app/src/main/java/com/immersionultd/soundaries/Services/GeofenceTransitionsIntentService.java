package com.immersionultd.soundaries.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.immersionultd.soundaries.Activities.MainMapsActivity;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    protected static final String TAG = "GeofenceTransitionsIS";


    public GeofenceTransitionsIntentService() {
        super(TAG);  // use TAG to name the IntentService worker thread
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            Log.e(TAG, "GeofencingEvent Error: " + event.getErrorCode());
            return;
        }

        int volume = intent.getIntExtra("volume", 0);

        int geofenceTransition = event.getGeofenceTransition();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Test that the reported transition was of interest.

        //todo add exit event
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, AudioManager.FLAG_PLAY_SOUND);
            Log.e(TAG, "geofence ENTER event triggered!");
        }

    }
}
