package com.immersionultd.soundaries.Activities.MainMapsActivity;//package com.immersionultd.soundaries.Activities.MainMapsActivity;

import android.content.Context;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.immersionultd.soundaries.Objects.Soundary;
import com.immersionultd.soundaries.R;

/**
 * Created by YonahKarp on 6/18/17.
 */

class SoundaryMap implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private final double US_LAT = 38.904756393677125;
    private final double US_LON = -99.48939766734838;

    GoogleMap map;
    private MainMapsActivity activity;

    SoundaryMap(MainMapsActivity activity){
        this.activity = activity;
        setUpMap();
    }

    private void setUpMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.setOnMapLongClickListener(this);
        setUpSoundaries();
        //showUILocationButton(); todo add in mainmapsactivity

        if (activity.storedData.soundaries.size() > 0) {
            map.moveCamera(CameraUpdateFactory.newLatLng(activity.storedData.soundaries.get(0).getLocation()));
            map.animateCamera(CameraUpdateFactory.zoomTo(9f));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(US_LAT, US_LON)));
            map.animateCamera(CameraUpdateFactory.zoomTo(3f));
        }

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        handleNonRegisteredSoundary();

        if (activity.storedData.soundaries.get(marker.getTitle()) != null)
            activity.tempSoundary = activity.storedData.soundaries.get(marker.getTitle());
        activity.bottomSheet.showModal(marker.getPosition());
        // Return false for default behavior to occur (marker is centered).
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        hideKeyboard();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        handleNonRegisteredSoundary();
        addSoundaryToMap("new location" + activity.storedData.soundaries.size(), latLng);
    }

    /*
      setUp methods
     --------------*/



    private void setUpSoundaries(){
        for (Soundary soundary: activity.storedData.soundaries.list())
            soundary.addToMap(map);
    }




    /*
      soundary methods
     -----------*/

    void addSoundaryToMap(String name, LatLng location){
        activity.tempSoundary = new Soundary(createNewMarkerOptions(name,location));
        activity.tempSoundary.addToMap(map);
        activity.bottomSheet.showModal(location);
    }

    private MarkerOptions createNewMarkerOptions(String name, LatLng location) {
        return new MarkerOptions().position(location).title(name);
    }

    void handleNonRegisteredSoundary(){
        if (activity.tempSoundary != null &&!activity.storedData.soundaries.contains(activity.tempSoundary))
            activity.tempSoundary.removeFromMap();
        else if (activity.tempSoundary != null)
            activity.tempSoundary.getCircle().setRadius(activity.tempSoundary.getRadius());
    }

    private void hideKeyboard() {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}
