package com.example.app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.app.factories.IntentFactory;
import com.example.app.finals.NearbyRequestType;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Random;

/**
 * Main UI activity. Here the user can choose the main actions.
 * (Need to write every button what actually does, when completed)
 */
public class MainActivity extends AppCompatActivity {

    private int radius;
    private int degree    = 0;
    private int oldDegree = 0;

    //360 / 6 / 2
    private static final float FACTOR = 30f;
    // constants for restoring instance of views
    private static final String SPINNER_KEY = "spinner_k";
    private static final int INVALID_POSITION = -1;
    private static final int REQUEST_USER_LOCATION_CODE = 99;

    private Spinner radiusSpinner;
    private ImageView wheel;
    private Random r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        r = new Random();

        wheel = findViewById(R.id.wheel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkUserLocationPermission();
        }

        //create the spinner and fill it
        radiusSpinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> grade = ArrayAdapter.createFromResource(
                this,
                R.array.RADIUS,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        grade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        radiusSpinner.setAdapter(grade);
        //set the listener
        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * Callback when an item from the radiusSpinner is selected
             * @param parent    adapter view
             * @param view      reference to the spinner widget
             * @param position  of the item
             * @param id        of the spinner
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // An radius was selected. You can retrieve the selected item using
                String radiusString = parent.getItemAtPosition(position).toString();
                //Set the desired radius
                radius = parseRadius(radiusString);
            }

            /**
             * Callback when the user don't select items
             * @param parent adapter view
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Default radius is 1km
                radius = 1000;
            }
        });

        // restoring instance status of views
        if(savedInstanceState != null) {
            int spinnerSelected = savedInstanceState.getInt(SPINNER_KEY, INVALID_POSITION);
            if(spinnerSelected != INVALID_POSITION){
                radiusSpinner.setSelection(spinnerSelected);
            }
        }
    }

    /**
     * Method that animate the wheel
     * @param view button {@id lucky_button}
     */
    public void spin(View view){
        oldDegree = degree % 360;
        degree = r.nextInt(360) + 720;
        RotateAnimation rotate = new RotateAnimation(oldDegree, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(3600);
        rotate.setFillAfter(true);
        rotate.setInterpolator(new DecelerateInterpolator());
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                sendRequest(360 - (degree % 360));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        wheel.startAnimation(rotate);
    }


    /**
     * Callback to save the state when necessary
     * @param savedInstanceState Bundle where to save places information
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(SPINNER_KEY, radiusSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // checking Google Play services apk
        GoogleApiAvailability google = GoogleApiAvailability.getInstance();
        int result = google.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS){
            Dialog dial = google.getErrorDialog(this, result, GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE);
            dial.show();
        }
    }

    /**
     * Method to send a nearby disco showing request to the Maps activity
     * @param view button {@id nearby_disco}
     */
    public void nearbyDiscoRequest(View view) {
        Intent showNearbyDisco = IntentFactory.createNearbyRequestIntent(this, NearbyRequestType.night_club, radius);
        startActivity(showNearbyDisco);
    }

    /**
     * Method to send a nearby restaurant showing request to the Maps activity
     * @param view button {@id nearby_restaurant}
     */
    public void nearbyRestaurantRequest(View view) {
        Intent showNearbyRestaurant = IntentFactory.createNearbyRequestIntent(this, NearbyRequestType.restaurant, radius);
        startActivity(showNearbyRestaurant);
    }

    /**
     * Method to send an help lobby request
     * @param view button {@id button}
     */
    public void helpRequest(View view){
        Intent helpIntent = IntentFactory.createHelpIntentRequest(this);
        startActivity(helpIntent);
    }

    //PERMISSIONS

    /**
     * Callback to check user permission
     * @param requestCode   of the permission
     * @param permissions   of the request
     * @param grantResults  of the permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_USER_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    //  mMap.setMyLocationEnabled(true);
                    //}
                }
                else {
                    Toast.makeText(this, "PERMISSION FAILED", Toast.LENGTH_LONG).show();
                }
        }
    }

    /**
     * Method to check the user location permission
     * @return true if it has the permission, false otherwise
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkUserLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
            }
            else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
            }
            return false;
        }
        return true;
    }

    //NATIVE METHODS

    /**
     * Parser for the radius long
     * @param radius to parse
     */
    public native int parseRadius(String radius);

    /**
     * Library loading
     */
    static {
        System.loadLibrary("libmain_native_lib");
    }


    /**
     * Analize the wheel position and send the corresponding command
     * @param degrees the result position
     */
    private void sendRequest(int degrees){
        if((degrees >= FACTOR * 1) && (degrees < FACTOR * 3)) {
            //send some request
            Log.d("ZONE", "blu");
        }
        if((degrees >= FACTOR * 3) && (degrees < FACTOR * 5)){
            //send some request
            Log.d("ZONE", "viola");
        }
        if((degrees >= FACTOR * 5) && (degrees < FACTOR * 7)){
            //send some request
            Log.d("ZONE", "rosso");
        }
        if((degrees >= FACTOR * 7) && (degrees < FACTOR * 9)){
            //send some request
            Log.d("ZONE", "arancio");
        }
        if((degrees >= FACTOR * 9) && (degrees < FACTOR * 11)){
            //send some request
            Log.d("ZONE", "giallo");
        }
        if((degrees >= FACTOR * 11) && (degrees < FACTOR * 13)){
            //send some request
            Log.d("ZONE", "verde");
        }
    }
}
