package com.immersionultd.soundaries.Activities.MainMapsActivity;

import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.immersionultd.soundaries.R;
import com.immersionultd.soundaries.Services.StoredData;
import com.immersionultd.soundaries.Objects.Soundary;

public class MainMapsActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainMapsActivity.class.getSimpleName();



    private GoogleApiClient mGoogleApiClient;
    protected StoredData storedData;
    protected Soundary tempSoundary;

    protected BottomSheet bottomSheet;
    protected NavDrawer navDrawer;
    protected SoundaryMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_maps);

        buildGoogleApiClient();
        storedData = new StoredData(this, mGoogleApiClient);

        setUpPieces();
        createLocationRequest(); //todo fix this
    }

    private void setUpPieces(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        map = new SoundaryMap(this);
        bottomSheet = new BottomSheet(this);
        navDrawer = new NavDrawer(this);

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
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }
    
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "connection to Google Places failed, auto complete not available", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }


    /**
     * my Methods
     */

    //set up methods
    //-----

    private void setUpAutoCompleteFragment(){
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input).setVisibility(View.GONE);
        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .getLayoutParams().width = 0;

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                map.handleNonRegisteredSoundary();
                map.addSoundaryToMap(place.getName() + "", place.getLatLng());
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
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(10f);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        builder.setAlwaysShow(true);

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
                    map.map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),
                                    location.getLongitude()), 14f));
                else
                    Toast.makeText(MainMapsActivity.this, "location unknown", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //todo address same-name bug (where )
}
