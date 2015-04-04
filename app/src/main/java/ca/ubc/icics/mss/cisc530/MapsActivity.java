package ca.ubc.icics.mss.cisc530;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends ActionBarActivity {
    final private String LOG_TAG = "MapsActivityLogTag";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private GPSTracker mGPS;

    private RelativeLayout  mTimeRuler;
    private SeekBar         mTimeSeekBar;
    private int             mTimeSeekBarMax;
    private Date            mTimeSeekBarStart;
    private long            mTimeSeekBarInterval;
    private Button          mTimeBtnPrev;
    private Button          mTimeBtnNext;
    private TextView        mTimeMsg;

    private boolean     bShowTimeRuler;
    private boolean     bShowMarker;

    private double  dValueMax, dValueMix;

    private HashMap<LatLng, ArrayList<DisplaySample>> dataMatrix = new HashMap<LatLng, ArrayList<DisplaySample>>();
    private HashMap<LatLng, Marker> markerMatrix = new HashMap<LatLng, Marker>();

    private DbManager dbManager;
    //private Marker mMarkers;

    private String[]        mTypeNames;
    private String          mMarkingType;
    private DataSample[]    mMarkingRawSamples;

    final private int ANIMATION_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_maps);

        dbManager = DbManager.getInstance(this);

        //initial display status
        bShowTimeRuler = false;
        bShowMarker = false;

        mTimeRuler = (RelativeLayout) findViewById(R.id.layout_time_ruler);
        mTimeSeekBar = (SeekBar)findViewById(R.id.seekbar_time);
        mTimeBtnPrev = (Button) findViewById(R.id.btn_pre);
        mTimeBtnNext = (Button) findViewById(R.id.btn_aft);
        mTimeMsg = (TextView) findViewById(R.id.txt_message);

        adjustShowHideTimeRuler();

        mTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTimeBtnPrev.setEnabled(progress > 0);
                mTimeBtnNext.setEnabled(progress < mTimeSeekBarMax);
                if(mTimeSeekBarStart!=null) {
                    //mTimeMsg.setText(progress + "/" + mTimeSeekBarMax + " " + new Date(mTimeSeekBarStart.getTime() + progress * mTimeSeekBarInterval));
                    mTimeMsg.setText("" + new Date(mTimeSeekBarStart.getTime() + progress * mTimeSeekBarInterval));
                }
                if(bShowMarker){
                    updateDataMarkers();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTimeBtnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cur = mTimeSeekBar.getProgress();
                Log.d(LOG_TAG, "setOnClickListener() PREV@" + cur + "/" + mTimeSeekBarMax);
                mTimeSeekBar.setProgress(cur - 1);
            }
        });

        mTimeBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cur = mTimeSeekBar.getProgress();
                Log.d(LOG_TAG, "setOnClickListener() NEXT@" + cur + "/" + mTimeSeekBarMax);
                mTimeSeekBar.setProgress(cur + 1);
            }
        });

        setUpMapIfNeeded();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG, "onConfigurationChanged");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");

        setUpMapIfNeeded();

        if(mGPS==null){
            mGPS = new GPSTracker(getApplicationContext());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");

        mGPS.stopUsingGPS();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemShow;
        MenuItem itemHide;

        itemShow = menu.findItem(R.id.maps_show_marker);
        itemHide = menu.findItem(R.id.maps_hide_marker);
        if(bShowMarker){
            itemShow.setVisible(false);
            itemHide.setVisible(true);
        }else{
            itemShow.setVisible(true);
            itemHide.setVisible(false);
        }

        itemShow = menu.findItem(R.id.maps_show_time_ruler);
        itemHide = menu.findItem(R.id.maps_hide_time_ruler);
        if(bShowMarker){
            if(canMix(dataMatrix.size())){
                menu.findItem(R.id.maps_mix_marker).setVisible(true);
            }
            menu.findItem(R.id.maps_reset_marker).setVisible(true);

            if(bShowTimeRuler){
                itemShow.setVisible(false);
                itemHide.setVisible(true);
            }else{
                itemShow.setVisible(true);
                itemHide.setVisible(false);
            }
        }else{
            menu.findItem(R.id.maps_mix_marker).setVisible(false);
            menu.findItem(R.id.maps_reset_marker).setVisible(false);

            itemShow.setVisible(false);
            itemHide.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.maps_settings) {
            Log.d(LOG_TAG, "ELI:Menu->Settings");
            Toast.makeText(getApplicationContext(), R.string.settings, Toast.LENGTH_SHORT).show();
            return true;
        } else
        */
        if (id == R.id.maps_quit) {
            Log.d(LOG_TAG, "ELI:Menu->Quit");
            System.exit(0);
            Toast.makeText(getApplicationContext(), R.string.quit, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_here) {
            if(mGPS.canGetLocation()){
                LatLng loc = mGPS.getLatLng();
                moveToLocation(loc);
            }else{
                Toast.makeText(getApplicationContext(), "GPS Setting Error!", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.maps_get_data) {
            startDownloadData();
        } else if (id == R.id.maps_show_time_ruler) {
            Log.d(LOG_TAG, "ELI:Menu->Show Time Ruler");
            bShowTimeRuler = true;
            adjustShowHideTimeRuler();
            Toast.makeText(getApplicationContext(), R.string.show_time_ruler, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_hide_time_ruler) {
            Log.d(LOG_TAG, "ELI:Menu->Hide Time Ruler");
            bShowTimeRuler = false;
            adjustShowHideTimeRuler();
            Toast.makeText(getApplicationContext(), R.string.hide_time_ruler, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_show_marker) {
            Log.d(LOG_TAG, "ELI:Menu->Show Marker");
            showSelectionByName();
            Toast.makeText(getApplicationContext(), R.string.show_marker, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_hide_marker) {
            Log.d(LOG_TAG, "ELI:Menu->Hide Marker");
            bShowMarker = false;
            hideDataMarkers();

            bShowTimeRuler = false;
            adjustShowHideTimeRuler();
            Toast.makeText(getApplicationContext(), R.string.hide_marker, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_mix_marker) {
            Log.d(LOG_TAG, "ELI:Menu->Mix Marker");
            mixDataMarkersOnMap();
            Toast.makeText(getApplicationContext(), R.string.mix_marker, Toast.LENGTH_SHORT).show();
        } else if (id == R.id.maps_reset_marker) {
            Log.d(LOG_TAG, "ELI:Menu->Reset Marker");
            resetDataMarkersOnMap();
            Toast.makeText(getApplicationContext(), R.string.reset_marker, Toast.LENGTH_SHORT).show();
        } else {
            Log.d(LOG_TAG, "ELI:Menu->Error");
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    private void adjustShowHideTimeRuler() {
        if(mTimeRuler!=null){
            if(bShowTimeRuler){
                mTimeRuler.setVisibility(View.VISIBLE);
            }else{
                mTimeRuler.setVisibility(View.GONE);
            }
        }
    }

    private void startDownloadData(){

        BackgroundDownloader.OnTaskCompleted callback = new BackgroundDownloader.OnTaskCompleted(){

            @Override
            public void OnTaskCompleted() {
                Toast.makeText(getApplicationContext(), R.string.download_finished, Toast.LENGTH_LONG).show();
            }
        };

        new BackgroundDownloader().setDatabaseManager(dbManager).setCallbackListener(callback).execute();   //start loading data in background thread

        //DataSample[] randomSamples = generateRandomDataSample(10);
        //
        //for(DataSample sample : randomSamples){
        //    dbManager.add(sample);
        //}
        //
        //DataSample[] databaseSample = dbManager.get(null);
        //for( DataSample s : databaseSample ){
        //    boolean suc = dbManager.add(s);
        //}
        //
        //DataSample[] databaseSample2 = dbManager.get(null);
    }

    private void moveToLocation(LatLng location){
        final LatLng VANCOUVER = new LatLng(49.2569684,-123.1239135);
        final LatLng HAMBURG   = new LatLng(53.558, 9.927);
        if (mMap != null) {
            if(location==null) {
                location = VANCOUVER;
                Toast.makeText(getApplicationContext(), "Vancouver", Toast.LENGTH_SHORT).show();
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 5), ANIMATION_DURATION, null);
        }
    }

    private int fillDataMatrixWithDataSample(DataSample[] samples){
        //clear previous data
        for (HashMap.Entry<LatLng, ArrayList<DisplaySample>> oneLocation : dataMatrix.entrySet()) {
            oneLocation.getValue().clear();
        }
        dataMatrix.clear();

        if(samples.length>0){   //init
            dValueMax = samples[0].value;
            dValueMix = samples[0].value;
        }else{
            return 0;
        }

        for(DataSample sample : samples){
            if(sample.value>dValueMax){
                dValueMax = sample.value;
            }
            if(sample.value<dValueMix){
                dValueMix = sample.value;
            }

            if(!dataMatrix.containsKey(sample.location)){
                ArrayList<DisplaySample> newLocation = new ArrayList<DisplaySample>();
                newLocation.add(new DisplaySample(sample));
                dataMatrix.put(sample.location, newLocation);
            }else{
                ArrayList<DisplaySample> oldLocation = dataMatrix.get(sample.location);
                oldLocation.add(new DisplaySample(sample));
            }
        }

        for (HashMap.Entry<LatLng, ArrayList<DisplaySample>> oneLocation : dataMatrix.entrySet()) {
            ArrayList<DisplaySample> list = oneLocation.getValue();
            Collections.sort(list);
        }

        return dataMatrix.size();
    }

    private DataSample[] generateRandomDataSample(int size){
        final LatLng VANCOUVER = new LatLng(49.2569684,-123.1239135);
        final LatLng RICHMOND  = new LatLng(49.1717992,-123.1184624);
        final LatLng BURNABY   = new LatLng(49.2380378,-122.9581694);

        if(size>0){
            DataSample[] dataSamples = new DataSample[size];
            for(int i=0; i<size; i++){
                dataSamples[i] = new DataSample();
                if(i%5==0){
                    dataSamples[i].station  = "VANCOUVER";
                    dataSamples[i].units    = "ppb";
                    dataSamples[i].name     = "NO2";
                    dataSamples[i].details  = "DataSample-" + i + "/" + size;
                    dataSamples[i].value    = Math.random();
                    dataSamples[i].time     = new Date( new Date().getTime() - (long)i*24*60*60*1000 );
                    dataSamples[i].location = VANCOUVER;
                }else if(i%5==1){
                    dataSamples[i].station  = "RICHMOND";
                    dataSamples[i].units    = "ppb";
                    dataSamples[i].name     = "NO2";
                    dataSamples[i].details  = "DataSample-" + i + "/" + size;
                    dataSamples[i].value    = Math.random();
                    dataSamples[i].time     = new Date( new Date().getTime() - (long)i*24*60*60*1000 );
                    dataSamples[i].location = RICHMOND;
                }else if(i%5==2){
                    dataSamples[i].station  = "BURNABY";
                    dataSamples[i].units    = "ppb";
                    dataSamples[i].name     = "NO2";
                    dataSamples[i].details  = "DataSample-" + i + "/" + size;
                    dataSamples[i].value    = Math.random();
                    dataSamples[i].time     = new Date( new Date().getTime() - (long)i*24*60*60*1000 );
                    dataSamples[i].location = BURNABY;
                }else if(i%5==3){
                    dataSamples[i].station  = "BURNABY";
                    dataSamples[i].units    = "ug/m3s";
                    dataSamples[i].name     = "PM25";
                    dataSamples[i].details  = "DataSample-" + i + "/" + size;
                    dataSamples[i].value    = Math.random();
                    dataSamples[i].time     = new Date( new Date().getTime() - (long)i*24*60*60*1000 );
                    dataSamples[i].location = BURNABY;
                }else{
                    dataSamples[i].station  = "BURNABY";
                    dataSamples[i].units    = "ug/m3s";
                    dataSamples[i].name     = "PM10";
                    dataSamples[i].details  = "DataSample-" + i + "/" + size;
                    dataSamples[i].value    = Math.random();
                    dataSamples[i].time     = new Date( new Date().getTime() - (long)i*24*60*60*1000 );
                    dataSamples[i].location = BURNABY;
                }
            }
            return dataSamples;
        }else{
            return null;
        }
    }

    private void adjustTimeRuler(){
        Date oldest=null, newest=null;
        mTimeSeekBarMax = 0;

        for (HashMap.Entry<LatLng, ArrayList<DisplaySample>> oneLocation : dataMatrix.entrySet()) {
            ArrayList<DisplaySample> sampleList = oneLocation.getValue();
            if(sampleList.size()>0){
                if(oldest==null){
                    oldest = sampleList.get(0).time;
                }else if(sampleList.get(0).time.compareTo(oldest)<0){ //the first time in list is older than oldest
                    oldest = sampleList.get(0).time;
                }

                if(newest==null){
                    newest = sampleList.get(sampleList.size()-1).time;
                }else if(sampleList.get(sampleList.size()-1).time.compareTo(newest)>0){ //the last time in list later than newest
                    newest = sampleList.get(sampleList.size()-1).time;
                }

                if(sampleList.size()>mTimeSeekBarMax){
                    mTimeSeekBarMax = sampleList.size();
                }
            }
        }

        mTimeSeekBarStart = oldest;
        mTimeSeekBarInterval = ( newest.getTime() - oldest.getTime() )/ mTimeSeekBarMax;

        if(mTimeSeekBar!=null){
            mTimeSeekBar.setMax(mTimeSeekBarMax);
            mTimeSeekBar.setProgress(mTimeSeekBarMax);
        }
    }

    private String getSnippet(DataSample sample){
        return sample.name + ": " + sample.value + " " + sample.units;
    }

    private String getSnippet(DisplaySample sample){
        return getSnippet((DataSample)sample);
    }

    private MarkerOptions getMarkerOptions(DisplaySample sample){
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(getIconDescriptor(sample.value))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .title(sample.station)
                .snippet(getSnippet(sample))
                .alpha(getAlphaValue(sample.value))
                .position(sample.location);
        return markerOptions;
    }

    private BitmapDescriptor getIconDescriptor(Double value){
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker();
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.house_flag))
        return icon;
    }

    private float getAlphaValue(Double value){
        return (float) ((float) 0.1 + 0.9 * (value-dValueMix)/dValueMax);
    }

    private void showSelectionByName(){
        int selectionIdx = -1;
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        mTypeNames = dbManager.getNames();
        if(mTypeNames==null){
            return;
        }

        for(int i=0; i<mTypeNames.length; i++){
            if(mMarkingType!=null && mTypeNames[i].compareTo(mMarkingType)==0){
                selectionIdx = i;
            }
        }

        AlertDialog.Builder builder = adb.setSingleChoiceItems(mTypeNames, selectionIdx, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOG_TAG, "showSelectionByName() setSingleChoiceItems onClick which=" + which);
                mMarkingType = mTypeNames[which];
            }
        });
        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOG_TAG, "showSelectionByName() setPositiveButton onClick which=" + which);

                //refresh marker
                DataSample[] samples = dbManager.get(mMarkingType);
                if(samples.length>0){
                    mMarkingRawSamples = samples;
                    fillDataMatrixWithDataSample(mMarkingRawSamples);
                    adjustTimeRuler();

                    bShowMarker = true;
                    showDataMarkers();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.not_enough_sample, Toast.LENGTH_SHORT).show();
                }
            }
        });
        //adb.setNegativeButton(R.string.cancel, null);
        adb.setTitle(R.string.select);
        adb.show();
    }

    private void showDataMarkers(){
        //clear all displaying markers
        hideDataMarkers();

        updateDataMarkers();
    }

    private DisplaySample searchListByTime(ArrayList<DisplaySample> sampleList, long time){
        if(sampleList.size()>0){
            DisplaySample point2Sample = null;
            for(int i=sampleList.size()-1; i>=0; i-- ) {
                point2Sample = sampleList.get(i);
                if(point2Sample.time.getTime()<=time){
                    break;
                }
            }

            //Log.d(LOG_TAG, "searchListByTime() point2Sample=" + point2Sample);
            return point2Sample;
        }else{
            return null;
        }
    }

    private void updateDataMarkers(){

        long point2Time = new Date().getTime();
        if(mTimeSeekBar!=null && mTimeSeekBarStart!=null){
            point2Time = mTimeSeekBarStart.getTime() + mTimeSeekBar.getProgress() * mTimeSeekBarInterval;
            Log.d(LOG_TAG, "updateDataMarkers() point2Time=" + point2Time);
        }

        for (HashMap.Entry<LatLng, ArrayList<DisplaySample>> oneLocation : dataMatrix.entrySet()) {
            ArrayList<DisplaySample> sampleList = oneLocation.getValue();

            DisplaySample point2Sample = searchListByTime(sampleList, point2Time);

            Log.d(LOG_TAG, "updateDataMarkers() point2Sample=" + point2Sample);

            if(point2Sample!=null){
                LatLng location = sampleList.get(0).location;

                if(!markerMatrix.containsKey(location)){
                    Marker aMarker = mMap.addMarker(getMarkerOptions(point2Sample));
                    markerMatrix.put(location, aMarker);
                    Log.d(LOG_TAG, "updateDataMarkers() add marker:" + point2Sample);
                }else{
                    Marker theMarker = markerMatrix.get(location);
                    boolean bInfo = theMarker.isInfoWindowShown();
                    theMarker.setTitle(point2Sample.station);
                    theMarker.setSnippet(getSnippet(point2Sample));
                    theMarker.setIcon(getIconDescriptor(point2Sample.value));
                    theMarker.setAlpha(getAlphaValue(point2Sample.value));
                    if(bInfo) {
                        theMarker.showInfoWindow();
                    }
                    Log.d(LOG_TAG, "updateDataMarkers() update marker:" + point2Sample);
                }
            }
        }
    }

    private void hideDataMarkers(){
        for (HashMap.Entry<LatLng, Marker> oneMarker : markerMatrix.entrySet()) {
            oneMarker.getValue().remove();
        }
        markerMatrix.clear();
    }

    private void mixDataMarkersOnMap(){
        dataMatrix = mixDataMarker(dataMatrix);
        showDataMarkers();
    }

    private HashMap<LatLng, ArrayList<DisplaySample>> mixDataMarker(HashMap<LatLng, ArrayList<DisplaySample>> oldMatrix){
        HashMap<LatLng, ArrayList<DisplaySample>> newMatrix = new HashMap<LatLng, ArrayList<DisplaySample>>();
        if( canMix(oldMatrix.size()) ){
            int partNum = getSplitNum(oldMatrix.size());

            double latMin=360, lngMin=360, latMax=-360, lngMax=-360;
            for (HashMap.Entry<LatLng, ArrayList<DisplaySample>> oneLocation : oldMatrix.entrySet()) {
                LatLng loc = oneLocation.getKey();
                if(loc.latitude <latMin) latMin = loc.latitude;
                if(loc.latitude >latMax) latMax = loc.latitude;
                if(loc.longitude<lngMin) lngMin = loc.longitude;
                if(loc.longitude>lngMax) lngMax = loc.longitude;
            }
            double latDelta = (latMax-latMin)/partNum;
            double lngDelta = (lngMax-lngMin)/partNum;

            //use the corner LatLng as the key of the Mix group
            HashMap<LatLng, ArrayList<ArrayList<DisplaySample>>> tmp = new HashMap<LatLng, ArrayList<ArrayList<DisplaySample>>>();
            for (HashMap.Entry<LatLng, ArrayList<DisplaySample>> oneLocation : oldMatrix.entrySet()) {
                LatLng location = oneLocation.getKey();
                LatLng corner   = new LatLng(latMin + latDelta * (int)((location.latitude -latMin)/latDelta),
                                        lngMin + lngDelta * (int)((location.longitude-lngMin)/lngDelta));

                if(tmp.containsKey(corner)){
                    tmp.get(corner).add(oneLocation.getValue());
                }else{
                    ArrayList<ArrayList<DisplaySample>> doubleList = new ArrayList<ArrayList<DisplaySample>>();
                    doubleList.add(oneLocation.getValue());
                    tmp.put(corner, doubleList);
                }
            }

            //Mix each group into array of same location into the new matrix for return
            for (HashMap.Entry<LatLng, ArrayList<ArrayList<DisplaySample>>> oneGroup : tmp.entrySet()){
                ArrayList<ArrayList<DisplaySample>> doubleList = oneGroup.getValue();
                double tLat=0, tLng=0;
                String station="", name="", unit="";
                HashMap<Date, Double> tValueHM = new HashMap<Date, Double>();
                for(ArrayList<DisplaySample> listSample: doubleList){
                    tLat += listSample.get(0).location.latitude;
                    tLng += listSample.get(0).location.longitude;
                    station += listSample.get(0).station + " ";

                    for(int i=0; i<mTimeSeekBarMax; i++){
                        Date d = new Date(mTimeSeekBarStart.getTime()+mTimeSeekBarInterval*i);
                        DisplaySample curV = searchListByTime(listSample, d.getTime());
                        if(tValueHM.containsKey(d)){
                            Double tmpV = tValueHM.get(d);
                            tValueHM.put(d, curV.value + tmpV.doubleValue());
                        }else{
                            tValueHM.put(d, curV.value);
                        }
                    }
                }

                name = doubleList.get(0).get(0).name;
                unit = doubleList.get(0).get(0).units;

                //create new data according to time ruler
                LatLng center = new LatLng(tLat/doubleList.size(), tLng/doubleList.size());
                ArrayList<DisplaySample> mixList = new ArrayList<DisplaySample>();
                for(HashMap.Entry<Date, Double> tValue: tValueHM.entrySet()){
                    mixList.add(new DisplaySample(new DataSample(station, name, null, tValue.getValue()/doubleList.size(), unit, tValue.getKey(), center)));
                }

                newMatrix.put(center, mixList);
            }
        }
        return newMatrix;
    }

    boolean canMix(int sampleNum){
        return getSplitNum(sampleNum)>1 ? true : false;
    }

    int getSplitNum(int sampleNum){
        if(sampleNum>0){
            int sr = (int) Math.sqrt(sampleNum);
            return sr/2;
        }else{
            return 0;
        }
    }

    private void resetDataMarkersOnMap(){
        if(mMarkingRawSamples.length>0){
            fillDataMatrixWithDataSample(mMarkingRawSamples);
            adjustTimeRuler();

            bShowMarker = true;
            showDataMarkers();
        }else{
            Toast.makeText(getApplicationContext(), R.string.not_enough_sample, Toast.LENGTH_SHORT).show();
        }
    }
}
