package com.immersionultd.soundaries.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.immersionultd.soundaries.R;
import com.immersionultd.soundaries.Services.LocationUpdateService;
import com.immersionultd.soundaries.Services.StoredData;

public class SettingsActivity extends AppCompatActivity {

    private final int TIME_SLIDER = 1;
    private final int DISTANCE_SLIDER = 2;

    StoredData storedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.inflateMenu(R.menu.settings_menu);


        storedData = (StoredData) getIntent().getExtras().getSerializable("storedData");

        setUpOptions();

    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void setUpOptions(){
        setUpSwitch();
        setUpTimeSlide();
        setUpDistSlide();
        setOverallImages();
    }

    private void setUpSwitch(){
        Switch locationOnOff = (Switch) findViewById(R.id.locationUseSwitch);
        locationOnOff.setChecked(!storedData.shouldUseLocationPolling);

        if (locationOnOff.isChecked())
            setViewAndChildrenEnabled(findViewById(R.id.seekbars), false);
        else
            setViewAndChildrenEnabled(findViewById(R.id.seekbars), true);

        locationOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    setViewAndChildrenEnabled(findViewById(R.id.seekbars), false);
                else
                    setViewAndChildrenEnabled(findViewById(R.id.seekbars), true);

                setOverallImages();
            }
        });

    }

    private void setUpTimeSlide(){
        SeekBar timeSeekBar = (SeekBar) findViewById(R.id.timeSeek);
        int progress = storedData.updateTimeMilliseconds / (30*1000);
        setImageForSliderValue(progress, TIME_SLIDER);

        timeSeekBar.setProgress(progress);

        timeSeekBar.setOnSeekBarChangeListener(buildSeekListener(TIME_SLIDER));

        TextView textView = (TextView) findViewById(R.id.timeTxt) ;
        textView.setText("" + (.5 + progress*.5) + " min");
        textView.setX(progress * 10); //// TODO not working correctly

    }

    private void setUpDistSlide(){
        SeekBar distSeekBar = (SeekBar) findViewById(R.id.distSeek);

        int progress = storedData.updateRadiusMeters - 1; //we added one on save
        setImageForSliderValue(progress, DISTANCE_SLIDER);
        distSeekBar.setProgress(progress);

        distSeekBar.setOnSeekBarChangeListener(buildSeekListener(DISTANCE_SLIDER));

        TextView textView = (TextView) findViewById(R.id.distTxt) ;
        textView.setText("" + progress + " m");
        textView.setX(progress * 5);

    }



    private static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof  ImageView)
            ((ImageView) view).setColorFilter(enabled? Color.TRANSPARENT:Color.LTGRAY );

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }


    private SeekBar.OnSeekBarChangeListener buildSeekListener(final int type){
        return new SeekBar.OnSeekBarChangeListener() {

            TextView textView = (TextView) (type == TIME_SLIDER? findViewById(R.id.timeTxt): findViewById(R.id.distTxt));
            double baseVal = (type == TIME_SLIDER)? .5 : 1;


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                setImageForSliderValue(progress, type);
                setOverallImages();
                textView.setText("" + (baseVal + progress*baseVal) + ((type == TIME_SLIDER)? " min":" m"));
                textView.setX(seekBar.getThumb().getBounds().left);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    private void setImageForSliderValue(int progress, int type){
        ImageView batImg = (ImageView) (type == TIME_SLIDER? findViewById(R.id.batTimeImg):findViewById(R.id.batDistImg));
        ImageView accImg = (ImageView) (type == TIME_SLIDER? findViewById(R.id.accTimeImg):findViewById(R.id.accDistImg));
        int adjust = (type == TIME_SLIDER? 1 : 2);

        if (progress < 3*adjust) {
            batImg.setImageResource(R.drawable.ic_low_bat);
            accImg.setImageResource(R.drawable.ic_acc_high);
        }
        else if(progress < 6*adjust) {
            batImg.setImageResource(R.drawable.ic_bat_med);
            accImg.setImageResource(R.drawable.ic_acc_good);
        }
        else if(progress < 8*adjust) {
            batImg.setImageResource(R.drawable.ic_bat_good);
            accImg.setImageResource(R.drawable.ic_acc_med);
        }
        else {
            batImg.setImageResource(R.drawable.ic_bat_high);
            accImg.setImageResource(R.drawable.ic_acc_low);
        }
    }

    private void setOverallImages(){
        ImageView batImg = (ImageView) findViewById(R.id.overallBatImg);
        ImageView accImg = (ImageView) findViewById(R.id.overallAccImg);


        int timeVal = ((SeekBar) findViewById(R.id.timeSeek)).getProgress();
        int distVal = ((SeekBar) findViewById(R.id.distSeek)).getProgress();
        boolean dontUseLocation = ((Switch) findViewById(R.id.locationUseSwitch)).isChecked();

        if (dontUseLocation){ //todo fix
            batImg.setImageResource(R.drawable.ic_bat_high);
            accImg.setImageResource(R.drawable.ic_acc_low);
            return;
        }


        if((timeVal + (distVal /2.0))/2.0 < 3){
            batImg.setImageResource(R.drawable.ic_low_bat);
            accImg.setImageResource(R.drawable.ic_acc_high);
        }
        else if((timeVal + (distVal /2.0))/2.0 < 6) {
            batImg.setImageResource(R.drawable.ic_bat_med);
            accImg.setImageResource(R.drawable.ic_acc_good);
        }
        else if((timeVal + (distVal /2.0))/2.0 < 8) {
            batImg.setImageResource(R.drawable.ic_bat_good);
            accImg.setImageResource(R.drawable.ic_acc_med);
        }
        else {
            batImg.setImageResource(R.drawable.ic_bat_high);
            accImg.setImageResource(R.drawable.ic_acc_low);
        }

        if(distVal > 15){
            accImg.setImageResource(R.drawable.ic_acc_low);
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Toast.makeText(this, "changes canceled", Toast.LENGTH_SHORT).show();
        return true;
    }

    public void submitSettings(MenuItem item){

        int timeVal = ((SeekBar) findViewById(R.id.timeSeek)).getProgress();
        int distVal = ((SeekBar) findViewById(R.id.distSeek)).getProgress();
        boolean dontUseLocation = ((Switch) findViewById(R.id.locationUseSwitch)).isChecked();

        storedData.shouldUseLocationPolling = !dontUseLocation; //todo rename
        storedData.updateTimeMilliseconds = timeVal * 30000 + 30000;
        storedData.updateRadiusMeters = distVal + 1;
        storedData.saveData(this, "savedData");

        stopService(new Intent(SettingsActivity.this, LocationUpdateService.class));
        startService(new Intent(SettingsActivity.this, LocationUpdateService.class));

        finish();
    }


}
