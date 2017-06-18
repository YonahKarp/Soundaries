package com.immersionultd.soundaries.Activities.MainMapsActivity;//package com.immersionultd.soundaries.Activities.MainMapsActivity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.immersionultd.soundaries.R;

/**
 * Created by YonahKarp on 6/18/17.
 */

class BottomSheet {

    private final int RADIUS_MULTIPLIER = 25;
    private final int SMALLEST_RADIUS = 50;

    private MainMapsActivity activity;
    private BottomSheetBehavior bottomSheetBehavior;
    private boolean shouldCancelModalChanges = true;


    BottomSheet(MainMapsActivity activity){
        this.activity = activity;
        setUpBottomSheet();
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(activity.findViewById(R.id.bottomSheetLayout));
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.locationModalBar);
        toolbar.inflateMenu(R.menu.modal_menu);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset == 0) {
                    ((FloatingActionButton) activity.findViewById(R.id.locationButton)).show();
                    if (shouldCancelModalChanges) {
                        if (!activity.storedData.soundaries.contains(activity.tempSoundary))
                            activity.tempSoundary.removeFromMap();
                        else
                            activity.tempSoundary.getCircle().setRadius(activity.tempSoundary.getRadius());

                        Toast toast = Toast.makeText(activity, "Changed Canceled \n(To save, use the green check button)", Toast.LENGTH_SHORT);
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

                        if (!activity.storedData.soundaries.contains(activity.tempSoundary)) {
                            setTempSoundaryData();
                            activity.tempSoundary.buildNewGeofence();
                            if (activity.storedData.soundaries.add(activity.tempSoundary)) //not clean, side affect
                                hideModal();
                        } else
                        if (editCurrentSoundary())
                            hideModal();

                        activity.navDrawer.listAdapter.notifyDataSetChanged();
                        return false;
                    }
                });

        //delete button
        toolbar.findViewById(R.id.action_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideModal();
                if (activity.storedData.soundaries.contains(activity.tempSoundary))
                    activity.storedData.soundaries.remove(activity.tempSoundary);
                activity.tempSoundary.removeFromMap();

                activity.navDrawer.listAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean editCurrentSoundary(){
        String name = ((EditText) activity.findViewById(R.id.titleEditText)).getText().toString();
        int volume = ((SeekBar) activity.findViewById(R.id.volumeBar)).getProgress();
        int radiusProgress = ((SeekBar) activity.findViewById(R.id.radiusBar)).getProgress();

        return activity.storedData.soundaries.editSoundary(activity, activity.tempSoundary, name, volume, SMALLEST_RADIUS + radiusProgress*RADIUS_MULTIPLIER);
    }

    private void setTempSoundaryData(){
        activity.tempSoundary.setName(((EditText) activity.findViewById(R.id.titleEditText)).getText().toString());
        activity.tempSoundary.setVolume(((SeekBar) activity.findViewById(R.id.volumeBar)).getProgress());
        activity.tempSoundary.setRadius(((SeekBar) activity.findViewById(R.id.radiusBar)).getProgress()*RADIUS_MULTIPLIER + SMALLEST_RADIUS);
    }


    void showModal(LatLng latLng){
        shouldCancelModalChanges = true;
        ((FloatingActionButton) activity.findViewById(R.id.locationButton)).hide();


        activity.map.map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude - .005, latLng.longitude), 14f));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        fillModalData();
    }

    private void hideModal(){
        shouldCancelModalChanges = false;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void fillModalData(){
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.locationModalBar);

        EditText titleText = (EditText) activity.findViewById(R.id.titleEditText);
        SeekBar volumeBar = (SeekBar) activity.findViewById(R.id.volumeBar);
        final SeekBar radiusBar = (SeekBar) activity.findViewById(R.id.radiusBar);
        final TextView distanceText = (TextView) activity.findViewById(R.id.distanceText);

        titleText.setText(activity.tempSoundary.getName()+"");
        volumeBar.setProgress(activity.tempSoundary.getVolume());
        radiusBar.setProgress((activity.tempSoundary.getRadius() - SMALLEST_RADIUS) / RADIUS_MULTIPLIER);

        titleText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard();
            }
        });

        distanceText.setText("" + activity.tempSoundary.getRadius() + " m");
        distanceText.setX(radiusBar.getThumb().getBounds().left + 76);

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                int radius = SMALLEST_RADIUS + progress*RADIUS_MULTIPLIER;
                activity.tempSoundary.getCircle().setRadius(radius);
                distanceText.setText("" + radius + " m");
                distanceText.setX(radiusBar.getThumb().getBounds().left + 76);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void hideKeyboard() {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
