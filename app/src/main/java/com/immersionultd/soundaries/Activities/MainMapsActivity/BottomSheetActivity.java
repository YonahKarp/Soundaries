package com.immersionultd.soundaries.Activities.MainMapsActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.immersionultd.soundaries.R;
import com.immersionultd.soundaries.util.IabHelper;
import com.immersionultd.soundaries.util.IabResult;
import com.immersionultd.soundaries.util.Purchase;

/**
 * Created by YonahKarp on 6/18/17.
 */

public abstract class BottomSheetActivity extends SoundaryMapActivity {
    private final int RADIUS_MULTIPLIER = 25;
    private final int SMALLEST_RADIUS = 50;
    private BottomSheetBehavior bottomSheetBehavior;
    private boolean shouldCancelModalChanges = true;

    private PopupWindow window;
    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener;
    private IabHelper mHelper;
    private final String SKU_FIVE_MORE = "add_five_soundaries";
    private final String SKU_UNLIMITED= "unlimited_soundaries";




    protected void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheetLayout));
        Toolbar toolbar = (Toolbar) findViewById(R.id.locationModalBar);
        toolbar.inflateMenu(R.menu.modal_menu);

        bottomSheetBehavior.setBottomSheetCallback(buildBottomSheetCallbacks());
        MenuItem check = (MenuItem) toolbar.getMenu().getItem(0).setOnMenuItemClickListener(buildCheckOnClick());
        toolbar.findViewById(R.id.action_delete).setOnClickListener(buildDeleteOnClick());
    }

    public void setUpInAppBilling(){
        mHelper = new IabHelper(this, getPK1() + getPK2());
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess())
                    Log.d("BottomSheetActivity", "Problem setting up In-app Billing: " + result);
            }
        });

        mPurchaseFinishedListener = bulidOnPurchaseFinishedListener();

    }

    private boolean editCurrentSoundary(){
        String name = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        int volume = ((SeekBar) findViewById(R.id.volumeBar)).getProgress();
        int radiusProgress = ((SeekBar) findViewById(R.id.radiusBar)).getProgress();

        return soundaries.editSoundary(this, tempSoundary, name, volume, SMALLEST_RADIUS + radiusProgress*RADIUS_MULTIPLIER);
    }

    private void setTempSoundaryData(){
        tempSoundary.setName(((EditText) findViewById(R.id.titleEditText)).getText().toString());
        tempSoundary.setVolume(((SeekBar) findViewById(R.id.volumeBar)).getProgress());
        tempSoundary.setRadius(((SeekBar) findViewById(R.id.radiusBar)).getProgress()*RADIUS_MULTIPLIER + SMALLEST_RADIUS);
    }


    void showModal(LatLng latLng){
        shouldCancelModalChanges = true;
        ((FloatingActionButton) findViewById(R.id.locationButton)).hide();

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude - .005, latLng.longitude), 14f));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        fillModalData();
    }

    private void hideModal(){
        shouldCancelModalChanges = false;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void fillModalData(){

        EditText titleText = (EditText) findViewById(R.id.titleEditText);
        SeekBar volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        SeekBar radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        TextView distanceText = (TextView) findViewById(R.id.distanceText);

        titleText.setText("" + tempSoundary.getName()+"");
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

        radiusBar.setOnSeekBarChangeListener(buildSeekBarOnChange(distanceText, radiusBar));
    }

    public void showPurchasePopover(){
        LayoutInflater inflater = (LayoutInflater) BottomSheetActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Inflate the view from a predefined XML layout
        View layout = inflater.inflate(R.layout.purchase_popover,null);

        window = new PopupWindow(layout, 550, 350, true);
        window.setBackgroundDrawable(new BitmapDrawable(getResources(),""));
        window.setOutsideTouchable(true);
        window.showAtLocation(findViewById(R.id.map), Gravity.CENTER, 0, 0);


    }



    //onSomething providers

    public  MenuItem.OnMenuItemClickListener buildCheckOnClick(){
        return new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (!soundaries.contains(tempSoundary)) {
                    setTempSoundaryData();
                    tempSoundary.buildNewGeofence();

                    if (soundaries.size() >= storedData.maxSoundaries){
                        Toast.makeText(BottomSheetActivity.this, "You already have the max number of Soundaries. To add more, please purchase", Toast.LENGTH_LONG).show();
                        tempSoundary.removeFromMap();

                        hideModal();
                        showPurchasePopover();

                        //todo show purchase
                    } else if (soundaries.add(tempSoundary)) //not clean, side affect
                        hideModal();
                } else
                if (editCurrentSoundary())
                    hideModal();

                hideKeyboard();
                listAdapter.notifyDataSetChanged();
                return false;
            }
        };
    }

    public View.OnClickListener buildDeleteOnClick(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideModal();
                if (soundaries.contains(tempSoundary))
                    soundaries.remove(tempSoundary);
                tempSoundary.removeFromMap();

                hideKeyboard();
                listAdapter.notifyDataSetChanged();
            }
        };
    }

    public BottomSheetBehavior.BottomSheetCallback buildBottomSheetCallbacks(){
        return new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset == 0) {
                    ((FloatingActionButton) findViewById(R.id.locationButton)).show();
                    if (shouldCancelModalChanges) {
                        if (!soundaries.contains(tempSoundary))
                            tempSoundary.removeFromMap();
                        else
                            tempSoundary.getCircle().setRadius(tempSoundary.getRadius());

                        showChangesCanceledToast();
                    }
                }
            }
        };
    }

    public void showChangesCanceledToast(){
        Toast toast = Toast.makeText(BottomSheetActivity.this, "Changed Canceled \n(To save, use the green check button)", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    public SeekBar.OnSeekBarChangeListener buildSeekBarOnChange(final TextView distanceText, final SeekBar radiusBar){
        return new SeekBar.OnSeekBarChangeListener() {
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
        };
    }


    //in-app billing methods

    public void purchaseFiveMore(View view){
        try {
            mHelper.launchPurchaseFlow(this, SKU_FIVE_MORE, 10001,
                    mPurchaseFinishedListener, "");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    public void purchaseUnlimited(View view){
        try {
            mHelper.launchPurchaseFlow(this, SKU_UNLIMITED, 10001,
                    mPurchaseFinishedListener, "");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }


    private IabHelper.OnIabPurchaseFinishedListener bulidOnPurchaseFinishedListener() {
        return new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isFailure()) {
                    Toast.makeText(BottomSheetActivity.this, "Error purchasing: " + result, Toast.LENGTH_SHORT).show();
                    return;
                } else if (purchase.getSku().equals(SKU_FIVE_MORE)) {
                    storedData.maxSoundaries += 5;
                    storedData.saveData(BottomSheetActivity.this, "savedData");

                } else if (purchase.getSku().equals(SKU_UNLIMITED)) {
                    storedData.maxSoundaries = 100;
                    storedData.saveData(BottomSheetActivity.this, "savedData");
                }

                Toast.makeText(BottomSheetActivity.this, "Thank you for your purchase!", Toast.LENGTH_SHORT).show();
                window.dismiss();
            }
        };
    }

    private String getPK1(){
        String s1,s2;
        s1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmxBiZMna0z7SGHwuieoBp6KlIRdwKdYf";
        s2 = "+BH68hCRiQ0s3dzPrUQFCiiEnyHWF7TTbaMrg+08bwmX52jQf4qOAmFVymdpQPWQPrcIhjTH0cRWB";
        return s1 +s2;
    }

    private String getPK2(){
        String s3, s4;
        s3 = "6orNRq1kg5YJSLkKocBRXFbmZ6x/tbtxXbgjS+xilj9GQ16h0FCIeeU/k7JTwVU7U2oXQi7Vumgxl";
        s4 = "Dh+cJ1oMENSRy44/YO8G7hnKOPuS+mL2AvU4fZ7ILvFoILzXs9BQCrUrhaK4BZsCpPgsH4/QY2pt5ntS1tJq+YQx5pyyzZ74dPBxAeW8PKkq9+pY5ZY0PVLyEa4TPucsKr4EhWnx8E0NcJOvAyFkwOLfPb0QIDAQAB";
        return s3 + s4;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) try {
            mHelper.dispose();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        mHelper = null;
    }


}
