package com.immersionultd.soundaries.Objects;


import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.immersionultd.soundaries.Services.GeofenceManager;
import com.immersionultd.soundaries.Services.StoredData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by YonahKarp on 6/11/17.
 */

public class SoundaryList implements Serializable {
    private List<Soundary> soundaries = new ArrayList<>();
    private List<String> soundaryNames = new ArrayList<>();

    private final String dataFileName = "savedData";

    private transient Context context;
    private transient GeofenceManager geofenceManager;
    private transient StoredData storedData;


    public SoundaryList(Context context, GoogleApiClient googleApiClient, StoredData storedData){
        geofenceManager = new GeofenceManager(googleApiClient);
        this.context = context;
        this.storedData = storedData;
    }

    public void setTransientData(Context context, GoogleApiClient googleApiClient, StoredData storedData){
        geofenceManager = new GeofenceManager(googleApiClient);
        this.context = context;
        this.storedData = storedData;
    }


    public boolean add(Soundary soundary) {
        if (isDuplicate_withMessage_(soundary)){
            return false;
        }
        geofenceManager.addGeofenceToListeningService(context, soundary);

        boolean temp = soundaries.add(soundary);
        soundaryNames.add(soundary.getName());
        storedData.saveData(context, dataFileName);
        return temp;
    }

    public void remove(Soundary soundary){
        soundaries.remove(soundary);
        soundaryNames.remove(soundary.getName());
        geofenceManager.removeGeofenceFromListeningService(context, soundary.getGeofence().getRequestId());
        storedData.saveData(context, dataFileName);
    }

    public Soundary remove(int i) {
        geofenceManager.removeGeofenceFromListeningService(context, soundaries.get(i).getGeofence().getRequestId());
        Soundary temp = soundaries.remove(i);
        soundaryNames.remove(i);
        storedData.saveData(context, dataFileName);
        return temp;
    }

    public boolean editSoundary(Context context, Soundary soundary, String newName, int volume, int radius){
        int index = soundaries.indexOf(soundary);
        soundaryNames.set(index, "OoReservedNameoO");
        if (isDuplicate_withMessage_(soundary))
            return false;


        soundaryNames.set(index, newName);

        geofenceManager.removeGeofenceFromListeningService(context, soundary.getName());
        soundary.setName(newName);
        soundary.setRadius(radius);
        soundary.setVolume(volume);
        soundary.buildNewGeofence();
        geofenceManager.addGeofenceToListeningService(context, soundary);
        storedData.saveData(context, dataFileName);

        return true;
    }

    public boolean isDuplicate_withMessage_(Soundary soundary){
        if (soundaryNames.contains(soundary.getName())){
            Toast.makeText(context, "Soundary for this name already exists. Please choose a different name", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }


    public List<Soundary> list(){
        return Collections.unmodifiableList(soundaries);
    }

    public boolean contains(Soundary soundary){
        return soundaries.contains(soundary);
    }
    public boolean contains(String name){
        return soundaryNames.contains(name);
    }


    public Soundary get(String name) {
        for (Soundary soundary: soundaries)
            if (soundary.getName().equals(name))
                return soundary;
        return null;
    }

    public List<String> getSoundaryNames() {
        return soundaryNames;
    }

    public void     clear(){for (Soundary soundary:soundaries) remove(soundary);}
    public Soundary get(int i){return soundaries.get(i);}
    public int      size(){return soundaries.size();}
    public boolean  isEmpty(){return soundaries.isEmpty();}
}
