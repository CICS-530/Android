package ca.ubc.icics.mss.cisc530;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends ActionBarActivity {
    final private String LOG_TAG = "MapsActivityLogTag";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private GPSTracker mGPS;

    private RelativeLayout mTimeRuler;

    private Marker mMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mTimeRuler = (RelativeLayout) findViewById(R.id.layout_time_ruler);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        if(mGPS==null){
            mGPS = new GPSTracker(getApplicationContext());
        }

        if(mGPS.canGetLocation()){
            LatLng loc = mGPS.getLatLng();
            moveToLocation(loc);
        }else{
            Toast.makeText(getApplicationContext(), "GPS Setting error!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mGPS.stopUsingGPS();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.maps_settings) {
            Log.d(LOG_TAG, "ELI:Menu->Settings");
            Toast.makeText(getApplicationContext(), R.string.settings, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_quit) {
            Log.d(LOG_TAG, "ELI:Menu->Quit");
            Toast.makeText(getApplicationContext(), R.string.quit, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_show_time_ruler) {
            Log.d(LOG_TAG, "ELI:Menu->Show Time Ruler");
            if(mTimeRuler!=null){
                mTimeRuler.setVisibility(View.VISIBLE);
            }
            Toast.makeText(getApplicationContext(), R.string.show_time_ruler, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_hide_time_ruler) {
            Log.d(LOG_TAG, "ELI:Menu->Hide Time Ruler");
            if(mTimeRuler!=null){
                mTimeRuler.setVisibility(View.GONE);
            }
            Toast.makeText(getApplicationContext(), R.string.hide_time_ruler, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_show_marker) {
            Log.d(LOG_TAG, "ELI:Menu->Show Marker");

            final LatLng VANCOUVER = new LatLng(49.2569684,-123.1239135);
            mMarkers = mMap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.house_flag))
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                    .position(VANCOUVER));

            Toast.makeText(getApplicationContext(), R.string.show_marker, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.maps_hide_marker) {
            Log.d(LOG_TAG, "ELI:Menu->Hide Marker");
            mMarkers.remove();
            
            Toast.makeText(getApplicationContext(), R.string.hide_marker, Toast.LENGTH_SHORT).show();
            return true;
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    private void moveToLocation(LatLng location){
        final LatLng VANCOUVER = new LatLng(49.2569684,-123.1239135);
        final LatLng HAMBURG   = new LatLng(53.558, 9.927);
        if (mMap != null) {
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(VANCOUVER, 5));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(VANCOUVER, 5), 2000, null);
        }
    }
}
