package com.immersionultd.soundaries.Services;

/**
 * Created by YonahKarp on 6/16/17.
 */

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;

import com.google.android.gms.location.LocationRequest;

// here is the OnRevieve methode which will be called when boot completed
public class BootCompleted extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        //we double check here for only boot complete event
        if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
        {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(30000);
            mLocationRequest.setFastestInterval(10000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setSmallestDisplacement(10f);
        }
    }
}
