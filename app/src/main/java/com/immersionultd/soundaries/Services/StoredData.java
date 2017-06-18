package com.immersionultd.soundaries.Services;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.immersionultd.soundaries.Objects.Soundary;
import com.immersionultd.soundaries.Objects.SoundaryList;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private GoogleApiClient googleApiClient;

    private final String dataFileName = "savedData";


    //todo make static (singleton)
    public StoredData(Context context, GoogleApiClient googleApiClient){
        this.googleApiClient = googleApiClient;
        loadData(context);
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

            in.close();
            fileIn.close();

        } catch (Exception e) {
            e.printStackTrace();
            soundaries = new SoundaryList(context, googleApiClient, this);
            return false;
        }

        return true;
    }

}
