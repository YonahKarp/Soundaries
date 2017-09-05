package com.immersionultd.soundaries.Activities.MainMapsActivity;

import android.Manifest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.immersionultd.soundaries.Activities.SettingsActivity;
import com.immersionultd.soundaries.R;
import com.immersionultd.soundaries.Services.LocationUpdateService;
import com.immersionultd.soundaries.Services.StoredData;
import com.immersionultd.soundaries.Objects.Soundary;
import com.immersionultd.soundaries.util.IabHelper;
import com.immersionultd.soundaries.util.IabResult;

public class MainMapsActivity extends BottomSheetActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = MainMapsActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_maps);




        buildGoogleApiClient();
        storedData = new StoredData(this, mGoogleApiClient);
        soundaries = storedData.soundaries;

        setUpPieces();
    }

    private void setUpPieces() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpNavs();
        setUpMap();

        setUpInAppBilling();
        setUpBottomSheet();

        setUpAutoCompleteFragment();
        setUpUILocationButton();


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //todo ask about
        //if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected())
        //  mGoogleApiClient.disconnect();
    }

    //googleApIClient connection methods
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "connection to Google Places failed, auto complete not available", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest(); //todo fix this
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }


    /**
     * my Methods
     */

    //set up methods
    //-----
    private void setUpAutoCompleteFragment() {
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input).setVisibility(View.GONE);
        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .getLayoutParams().width = 0;

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                handleNonRegisteredSoundary();
                addSoundaryToMap(place.getName() + "", place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }


    public void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }



    //map methods
    //----------

    protected void createLocationRequest() {
        startService(new Intent(this, LocationUpdateService.class));
    }

    private void setUpUILocationButton() {

        FloatingActionButton locationButton = (FloatingActionButton) findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;
                Location location = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient);

                if (location != null)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),
                                    location.getLongitude()), 14f));
                else
                    Toast.makeText(MainMapsActivity.this, "location unknown", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "testing message, at location " + location, Toast.LENGTH_SHORT).show();
    }

}
