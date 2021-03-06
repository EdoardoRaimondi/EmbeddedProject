package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.example.app.dialogs.BasicDialog;
import com.example.app.finals.MapsUtility;
import com.example.app.sensors.ConnectionManager;
import com.example.app.sensors.GPSManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

/**
 * Main UI activity. Here the user can choose the main actions.
 */
public class MainActivity extends AppCompatActivity implements BasicDialog.BasicDialogListener {

    // Dialogs' ids
    private static final String RATIONALE_ID = "rationale_id";
    // Instance state and intent keys
    private static final String RADIUS_KEY = "radius_k";
    private static final String FRAGMENT_KEY = "fragment_k";
    public static final String FRAGMENT_KEY_INTENT = "fragment_k";
    // Type of location request
    private static final int REQUEST_USER_LOCATION_CODE = 99;
    // Type of Google Update or Sign In request
    private static final int GOOGLE_CHECK = 100;
    // Check delay of GPS and Connection statuses
    private static final int DELAY_CHECK = 1000;

    private static final String TAG = "MainActivity";

    // Selected radius
    private int radius;
    // Fragment displayed
    private int displayFragment;
    // GPS Manager
    private GPSManager gpsManager;
    // ConnectivityManager
    private ConnectionManager connectionManager;

    private LocationListener locationListener;

    // Child Thread
    private Handler handler;
    private Runnable runnable;

    // Snackbar statuses
    private boolean isShowingNoConnection;
    private boolean isShowingNoGPS;

    // BEGIN OF MAIN ACTIVITY'S LIFE CYCLE CALLBACKS

    /**
     * Callback invoked while creating MainActivity
     * @param savedInstanceState the Bundle were previous state has been saved
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setting main activity as full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Setting appropriate layout
        setContentView(R.layout.activity_main);
        // Initializing navigation controller
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu id as a set of ids because each
        // Menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_utils, R.id.navigation_search, R.id.navigation_saved)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // If instance state was saved then backup
        if(savedInstanceState != null){
            // Restore previous radius
            radius = savedInstanceState.getInt(RADIUS_KEY, getResources().getInteger(R.integer.default_radius) * MapsUtility.KM_TO_M);
            // Return to previous fragment
            displayFragment = savedInstanceState.getInt(FRAGMENT_KEY, R.id.navigation_search);
        }
        else{
            radius = getResources().getInteger(R.integer.default_radius) * MapsUtility.KM_TO_M;
            displayFragment = R.id.navigation_search;
        }
        // If another activity opened this one, get the fragment to return
        Intent intent = getIntent();
        displayFragment = intent.getIntExtra(FRAGMENT_KEY_INTENT, displayFragment);
        // Managing navigation graph
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.mobile_navigation);
        // Setting starting fragment
        navGraph.setStartDestination(displayFragment);
        navController.setGraph(navGraph);

        // Initializing navigation bar (footer)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

    }

    /**
     * Callback when user return here or activity
     * has just been created
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Checking Google Play services apk
        GoogleApiAvailability google = GoogleApiAvailability.getInstance();
        int result = google.isGooglePlayServicesAvailable(this);
        // If Google Play Services SDK is disabled or not present
        if(result != ConnectionResult.SUCCESS){
            google.getErrorDialog(this, result, GOOGLE_CHECK)
                .show();
        }
        else{
            // Initializing managers and statuses
            gpsManager = new GPSManager(getApplicationContext());
            connectionManager = new ConnectionManager(getApplicationContext());
            isShowingNoConnection = false;
            isShowingNoGPS = false;
            if(!gpsManager.hasPermissions()){
                // Request for permissions
                if(gpsManager.canRequestNow(this)) {
                    gpsManager.requirePermissions(this, REQUEST_USER_LOCATION_CODE);
                }
                else{
                    // Showing Rationale
                    BasicDialog.BasicDialogBuilder basicDialogBuilder = new BasicDialog.BasicDialogBuilder(RATIONALE_ID);
                    basicDialogBuilder.setTitle(getString(R.string.to_clarify));
                    basicDialogBuilder.setText(getString(R.string.rationale));
                    basicDialogBuilder.setTextForOkButton(getString(R.string.ok_button));
                    basicDialogBuilder.build().show(getSupportFragmentManager(), TAG);
                }
            }
            else if(!gpsManager.isGPSOn()){
                showNoGPS();
            }
            // The LocationListener
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }
                @Override
                @Deprecated
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }
                @Override
                public void onProviderEnabled(String provider) {
                    if(gpsManager.isGPSOn()){
                        if(isShowingNoGPS){
                            findViewById(R.id.coordinator).setVisibility(View.GONE);
                            isShowingNoGPS = false;
                        }
                    }
                }
                @Override
                public void onProviderDisabled(String provider) {
                    if(!gpsManager.isGPSOn()){
                        if(!isShowingNoGPS){
                            showNoGPS();
                        }
                    }
                }
            };
            gpsManager.requireUpdates(locationListener, DELAY_CHECK);
            handler = new Handler();
            runnable = () -> {
                // The check
                if(connectionManager.isNetworkAvailable()){
                    if(isShowingNoConnection) {
                        findViewById(R.id.coordinator2).setVisibility(View.GONE);
                        isShowingNoConnection = false;
                    }
                }
                else{
                    if(!isShowingNoConnection) {
                        showNoConnection();
                    }
                }
                // Setting delay time before one check and another
                handler.postDelayed(runnable, DELAY_CHECK);
            };
            handler.post(runnable);
        }
    }

    /**
     * Callback when activity looses foreground space
     */
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        gpsManager.removeCallback(locationListener);
    }

    /**
     * Callback to save the state when necessary
     * @param savedInstanceState Bundle where to save radius value
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(RADIUS_KEY, radius);
        savedInstanceState.putInt(FRAGMENT_KEY, displayFragment);
        super.onSaveInstanceState(savedInstanceState);
    }

    // PERMISSIONS

    /**
     * Callback to check user permission results
     * @param requestCode   of the permission
     * @param permissions   of the request
     * @param grantResults  of the permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_USER_LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(R.id.coordinator), getString(R.string.thank_you), Snackbar.LENGTH_LONG)
                        .show();
            }
            else {
                Snackbar.make(findViewById(R.id.coordinator), getString(R.string.no_gps_permission), Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }

    // END OF MAIN ACTIVITY'S LIFE CYCLE CALLBACKS

    // PUBLIC INTERFACE

    /**
     * Getter method of radius
     */
    public int getRadius(){
        return this.radius;
    }

    /**
     * Setter method of radius
     * @param radius the radius to set
     */
    public void setRadius(int radius){
        this.radius = radius;
    }

    /**
     * Setter method of displayed fragment
     * @param fragment the radius to set
     */
    public void setFragment(int fragment){
        this.displayFragment = fragment;
    }

    // DIALOGS RESULT LISTENER
    /**
     * BasicDialog common listener
     * @param id the identifier of dialog that was dismissed
     * @param option the option chosen by user
     */
    public void onDialogResult(String id, boolean option){
        if(id.equals(RATIONALE_ID)) {
            if (option) {
                gpsManager.requirePermissions(this, REQUEST_USER_LOCATION_CODE);
            }
        }
    }

    private void showNoConnection(){
        isShowingNoConnection = true;
        findViewById(R.id.coordinator2).setVisibility(View.VISIBLE);
        Snackbar.make(findViewById(R.id.coordinator2), getString(R.string.no_internet), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.yes), v -> startActivity(new Intent(Settings.ACTION_SETTINGS)))
                .show();
    }

    private void showNoGPS(){
        isShowingNoGPS = true;
        findViewById(R.id.coordinator).setVisibility(View.VISIBLE);
        Snackbar.make(findViewById(R.id.coordinator), getString(R.string.no_gps), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.yes), v -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .show();
    }
}
