package com.immersionultd.soundaries.Services;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.immersionultd.soundaries.Objects.Soundary;
import com.immersionultd.soundaries.Objects.SoundaryList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by YonahKarp on 6/13/17.
 */

public class StoredData implements Serializable {
    //private ArrayList<Soundary> soundaries = null;
    public SoundaryList soundaries = null;
    public int previousVolume = 5;
    public static int updateTimeMilliseconds = 60 * 1000;
    public static int updateRadiusMeters = 5;
    public static boolean shouldUseLocationPolling = false;

    public int maxSoundaries = 3;

    private transient GoogleApiClient googleApiClient;

    private final String dataFileName = "savedData";


    //todo make static (singleton)
    public StoredData(Context context, GoogleApiClient googleApiClient){
        this.googleApiClient = googleApiClient;
        loadData(context);
    }

    public StoredData(Context context){
        loadJustSoundaries(context);
    }

    private boolean loadData(Context context) {
        if (soundaries == null)
            return loadData(context, dataFileName);
        return true;
    }

    public boolean saveData(Context context, String fileName){
        try {
            FileOutputStream fileOut = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeObject(soundaries);
            out.writeInt(previousVolume);
            out.writeInt(updateTimeMilliseconds);
            out.writeInt(updateRadiusMeters);
            out.writeBoolean(shouldUseLocationPolling);
            out.writeInt(maxSoundaries);

            out.close();
            fileOut.close();
            return true; //success
        } catch (Exception e) {e.printStackTrace();}
        return false; //failure
    }

    private boolean loadData(Context context, String fileName){
        try {
            FileInputStream fileIn = context.openFileInput(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            soundaries = (SoundaryList) in.readObject();
            soundaries.setTransientData(context, googleApiClient, this);

            previousVolume = in.readInt();
            updateTimeMilliseconds = in.readInt();
            updateRadiusMeters = in.readInt();
            shouldUseLocationPolling = in.readBoolean();
            maxSoundaries = in.readInt();

            in.close();
            fileIn.close();

        } catch (Exception e) {
            e.printStackTrace();
            soundaries = new SoundaryList(context, googleApiClient, this);
            return false;
        }
        return true;
    }

    private boolean loadJustSoundaries(Context context) {
        try {
            FileInputStream fileIn = context.openFileInput(dataFileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            soundaries = (SoundaryList) in.readObject();
        } catch (IOException | ClassNotFoundException e) {return false;}
        return true;
    }

}
