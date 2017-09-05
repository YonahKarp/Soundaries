package com.immersionultd.soundaries.Activities.MainMapsActivity;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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

public abstract class SoundaryMapActivity extends NavDrawerActivity implements
        OnMapReadyCallback,GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private final double US_LAT = 38.904756393677125;
    private final double US_LON = -99.48939766734838;
    GoogleMap map;

    /*
      setUp methods
     --------------*/

    protected void setUpMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setUpSoundaries(){
        for (Soundary soundary: soundaries.list())
            soundary.addToMap(map);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);
        setUpSoundaries();

        moveMapToLocation();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        handleNonRegisteredSoundary();

        if (soundaries.get(marker.getTitle()) != null)
            tempSoundary = soundaries.get(marker.getTitle());
        showModal(marker.getPosition());
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
        addSoundaryToMap("new location" + soundaries.size(), latLng);
    }

    private void moveMapToLocation(){
        if (soundaries.size() > 0)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(soundaries.get(0).getLocation(),9f));
        else
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(US_LAT, US_LON), 3f));
    }



    /*
      soundary methods
     -----------*/

    void addSoundaryToMap(String name, LatLng location){
        tempSoundary = new Soundary(createNewMarkerOptions(name,location));
        tempSoundary.addToMap(map);
        showModal(location);
    }

    private MarkerOptions createNewMarkerOptions(String name, LatLng location) {
        return new MarkerOptions().position(location).title(name);
    }

    void handleNonRegisteredSoundary(){
        if (tempSoundary != null &&!soundaries.contains(tempSoundary))
            tempSoundary.removeFromMap();
        else if (tempSoundary != null)
            tempSoundary.getCircle().setRadius(tempSoundary.getRadius());
    }

    protected void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        findViewById(R.id.titleEditText).clearFocus();
    }


}
