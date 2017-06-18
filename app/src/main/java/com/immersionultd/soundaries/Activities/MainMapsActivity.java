package com.immersionultd.soundaries.Activities;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.view.Menu;
import android.view.MenuItem;

import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.immersionultd.soundaries.R;
import com.immersionultd.soundaries.Services.StoredData;
import com.immersionultd.soundaries.Objects.Soundary;

public class MainMapsActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainMapsActivity.class.getSimpleName();
    private final int RADIUS_MULTIPLIER = 25;
    private final int SMALLEST_RADIUS = 50;
    private final double US_LAT = 38.904756393677125;
    private final double US_LON = -99.48939766734838;


    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private StoredData storedData;

    //todo add an adapter provider

    public ArrayAdapter<String> listAdapter;

    private Soundary tempSoundary = null;
    private boolean shouldCancelModalChanges = true;

    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_maps);

        buildGoogleApiClient();
        storedData = new StoredData(this, mGoogleApiClient);

        setUpNavs();
        setUpMap();
        setUpBottomSheet();
        createLocationRequest(); //todo fix this
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

    /**
     * Map Methods
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        setUpSoundaries();
        showUILocationButton();

        if (storedData.soundaries.size() > 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(storedData.soundaries.get(0).getLocation()));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(9f));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(US_LAT, US_LON)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(3f));
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "connection to Google Places failed, auto complete not available", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        handleNonRegisteredSoundary();

        if (storedData.soundaries.get(marker.getTitle()) != null)
            tempSoundary = storedData.soundaries.get(marker.getTitle());
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
        addSoundaryToMap("new location" + storedData.soundaries.size(), latLng);
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
    public void setUpNavs() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        setUpSoundaryList(navigationView);
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheetLayout));
        Toolbar toolbar = (Toolbar) findViewById(R.id.locationModalBar);
        toolbar.inflateMenu(R.menu.modal_menu);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset == 0) {
                    ((FloatingActionButton) findViewById(R.id.locationButton)).show();
                    if (shouldCancelModalChanges) {
                        if (!storedData.soundaries.contains(tempSoundary))
                            tempSoundary.removeFromMap();
                        else
                            tempSoundary.getCircle().setRadius(tempSoundary.getRadius());

                        Toast toast = Toast.makeText(MainMapsActivity.this, "Changed Canceled \n(To save, use the green check button)", Toast.LENGTH_SHORT);
                        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                        if (v != null) v.setGravity(Gravity.CENTER);
                        toast.show();
                    }
                }
            }
        });
        setUpBottomSheetButtons(toolbar);
    }

    private void setUpBottomSheetButtons(final Toolbar toolbar) {
        MenuItem check = (MenuItem) toolbar.getMenu().getItem(0)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {

                    if (!storedData.soundaries.contains(tempSoundary)) {
                        setTempSoundaryData();
                        tempSoundary.buildNewGeofence();
                        if (storedData.soundaries.add(tempSoundary)) //not clean, side affect
                            hideModal();
                    } else
                        if (editCurrentSoundary())
                            hideModal();

                    listAdapter.notifyDataSetChanged();
                    return false;
                    }
                });

        //delete button
        toolbar.findViewById(R.id.action_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideModal();
                if (storedData.soundaries.contains(tempSoundary))
                    storedData.soundaries.remove(tempSoundary);
                tempSoundary.removeFromMap();

                listAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setUpMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

    private void setUpSoundaryList(final NavigationView navigationView){
        listAdapter = new ArrayAdapter<>(this, R.layout.list_item_soundary, storedData.soundaries.getSoundaryNames());
        ListView listView = (ListView) navigationView.findViewById(R.id.soundariesList);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                tempSoundary = storedData.soundaries.get(index);
                showModal(tempSoundary.getLocation());
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    //map methods
    //----------

    public MarkerOptions createNewMarkerOptions(String name, LatLng location) {
        return new MarkerOptions().position(location).title(name);
    }

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

    private void showUILocationButton() {

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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),
                                    location.getLongitude()), 14f));
                else
                    Toast.makeText(MainMapsActivity.this, "location unknown", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //general methods
    //---------------

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }    }

    private void showModal(LatLng latLng){
        shouldCancelModalChanges = true;
        ((FloatingActionButton) findViewById(R.id.locationButton)).hide();


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude - .005, latLng.longitude), 14f));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        fillModalData();
    }

    private void hideModal(){
        shouldCancelModalChanges = false;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void fillModalData(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.locationModalBar);

        EditText titleText = (EditText) findViewById(R.id.titleEditText);
        SeekBar volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        final SeekBar radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        final TextView distanceText = (TextView) findViewById(R.id.distanceText);

        titleText.setText(tempSoundary.getName()+"");
        volumeBar.setProgress(tempSoundary.getVolume());
        radiusBar.setProgress((tempSoundary.getRadius() - SMALLEST_RADIUS) / RADIUS_MULTIPLIER);

        titleText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard();
            }
        });

        distanceText.setText("" + tempSoundary.getRadius() + " m");
        distanceText.setX(radiusBar.getThumb().getBounds().left + 76);

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                int radius = SMALLEST_RADIUS + progress*RADIUS_MULTIPLIER;
                tempSoundary.getCircle().setRadius(radius);
                distanceText.setText("" + radius + " m");
                distanceText.setX(radiusBar.getThumb().getBounds().left + 76);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    //SoundaryList Methods
    //-------------------

    public void setUpSoundaries(){
        for (Soundary soundary: storedData.soundaries.list())
            soundary.addToMap(mMap);
    }

    public void addSoundaryToMap(String name, LatLng location){
        tempSoundary = new Soundary(createNewMarkerOptions(name,location));
        tempSoundary.addToMap(mMap);
        showModal(location);
    }

    private boolean editCurrentSoundary(){
        String name = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        int volume = ((SeekBar) findViewById(R.id.volumeBar)).getProgress();
        int radiusProgress = ((SeekBar) findViewById(R.id.radiusBar)).getProgress();

        return storedData.soundaries.editSoundary(MainMapsActivity.this, tempSoundary, name, volume, SMALLEST_RADIUS + radiusProgress*RADIUS_MULTIPLIER);
    }

    private void setTempSoundaryData(){
        tempSoundary.setName(((EditText) findViewById(R.id.titleEditText)).getText().toString());
        tempSoundary.setVolume(((SeekBar) findViewById(R.id.volumeBar)).getProgress());
        tempSoundary.setRadius(((SeekBar) findViewById(R.id.radiusBar)).getProgress()*RADIUS_MULTIPLIER + SMALLEST_RADIUS);
    }

    private void handleNonRegisteredSoundary(){
        if (tempSoundary != null &&!storedData.soundaries.contains(tempSoundary))
            tempSoundary.removeFromMap();
        else if (tempSoundary != null)
            tempSoundary.getCircle().setRadius(tempSoundary.getRadius());
    }


    //todo address same-name bug (where )
}
