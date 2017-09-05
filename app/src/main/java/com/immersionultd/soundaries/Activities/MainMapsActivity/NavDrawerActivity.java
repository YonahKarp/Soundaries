package com.immersionultd.soundaries.Activities.MainMapsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.immersionultd.soundaries.Activities.HelpActivity;
import com.immersionultd.soundaries.Activities.SettingsActivity;
import com.immersionultd.soundaries.Objects.Soundary;
import com.immersionultd.soundaries.Objects.SoundaryList;
import com.immersionultd.soundaries.R;
import com.immersionultd.soundaries.Services.StoredData;

/**
 * Created by YonahKarp 6/18/17.
 */

public abstract class NavDrawerActivity extends AppCompatActivity {

    ArrayAdapter<String> listAdapter;
    StoredData storedData;
    SoundaryList soundaries;
    Soundary tempSoundary;


     protected void setUpNavs() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        setUpSoundaryList(navigationView);
    }

    private void setUpSoundaryList(final NavigationView navigationView){
        listAdapter = new ArrayAdapter<>(this, R.layout.list_item_soundary, soundaries.getSoundaryNames());
        ListView listView = (ListView) navigationView.getHeaderView(0).findViewById(R.id.soundariesList);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(buildListItemClickListener());

        //override navview scroll intercept
        listView.setOnTouchListener(buildOnTouchListener());
    }

     abstract void showModal(LatLng latLng);



    //listener providers
    private AdapterView.OnItemClickListener buildListItemClickListener(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                tempSoundary = soundaries.get(index);
                showModal(tempSoundary.getLocation());
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        };
    }

    private ListView.OnTouchListener buildOnTouchListener(){
        return new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        };
    }

    public void goToHelp(MenuItem item){
        startActivity(new Intent(this, HelpActivity.class));
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(Gravity.START);
    }


    public void goToSettings(MenuItem item){
        Intent intent = new Intent(this, SettingsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("storedData", storedData);
        intent.putExtras(bundle);
        startActivity(intent);
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(Gravity.START);
    }

}



