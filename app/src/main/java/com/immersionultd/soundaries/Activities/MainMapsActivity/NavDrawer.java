package com.immersionultd.soundaries.Activities.MainMapsActivity;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.immersionultd.soundaries.R;
import com.immersionultd.soundaries.Services.StoredData;

/**
 * Created by YonahKarp on 6/18/17.
 */

class NavDrawer {
    private MainMapsActivity activity;
    ArrayAdapter<String> listAdapter;
    private StoredData storedData;



    NavDrawer(MainMapsActivity activity){
        this.activity = activity;
        storedData = activity.storedData;
        setUpNavs();
    }

    private void setUpNavs() {

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        setUpSoundaryList(navigationView);
    }

    private void setUpSoundaryList(final NavigationView navigationView){
        listAdapter = new ArrayAdapter<>(activity, R.layout.list_item_soundary, storedData.soundaries.getSoundaryNames());
        ListView listView = (ListView) navigationView.findViewById(R.id.soundariesList);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                activity.tempSoundary = storedData.soundaries.get(index);
                activity.bottomSheet.showModal(activity.tempSoundary.getLocation());
                DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

}
