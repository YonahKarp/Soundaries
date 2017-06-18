package com.immersionultd.soundaries.Objects;

import android.graphics.Color;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.internal.ParcelableGeofence;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by YonahKarp on 6/11/17.
 */



public class Soundary implements Serializable {

    private transient Marker marker;
    private transient Circle circle;
    private transient Geofence geofence;

    private transient LatLng location;
    private transient MarkerOptions markerOptions;
    private transient CircleOptions circleOptions;

    private String name;
    private double latitude;
    private double longitude;
    private int radius;
    private int volume;

    public Soundary(MarkerOptions markerOptions){
        name = markerOptions.getTitle();
        this.markerOptions = markerOptions;
        radius = 150;
        circleOptions = buildCircleOptions();
        latitude = markerOptions.getPosition().latitude;
        longitude = markerOptions.getPosition().longitude;
        volume = 0;
    }

    public void addToMap(GoogleMap map){
        if (marker == null && circle == null){
            marker = map.addMarker(markerOptions);
            circle = map.addCircle(circleOptions);
        }

    }
    public void removeFromMap(){
        if (marker != null && circle != null) {
            marker.remove();
            circle.remove();
        }
    }

    public Circle getCircle() {
        return circle;
    }

    public String getName() {return name;}
    public void setName(String name) {
        this.name = name;
        markerOptions.title(name);
        marker.setTitle(name);
    }

    public int getVolume() {
        return volume;
    }
    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getRadius() {return radius;}
    public void setRadius(int radius) {
        this.radius = radius;
        circle.setRadius(radius);
    }

    public LatLng getLocation() {
        if (location != null)
            return location;
        location = new LatLng(latitude,longitude);
        return location;
    }


    private CircleOptions buildCircleOptions(){
        return new CircleOptions()
                .center(markerOptions.getPosition())
                .radius(radius)
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);
    }

    public Geofence getGeofence() {
        if(geofence == null)
            buildNewGeofence();
        return geofence;
    }

    public void setGeofence(Geofence geofence) {
        name = geofence.getRequestId();
        this.geofence = geofence;
        marker.setTitle(geofence.getRequestId());
    }

    public void buildNewGeofence(){
        geofence = new Geofence.Builder()
                .setRequestId(marker.getTitle())
                .setCircularRegion(
                        latitude,
                        longitude,
                        radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // default serialization
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        // default deserialization
        ois.defaultReadObject();
        location = new LatLng(latitude,longitude);
        markerOptions = new MarkerOptions().position(location).title(name);
        circleOptions = buildCircleOptions();
    }


}
