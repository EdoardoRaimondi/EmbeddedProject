package com.example.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.app.dialogs.RadiusDialog;
import com.example.app.factories.DialogFactory;
import com.example.app.factories.IntentFactory;
import com.example.app.finals.NearbyRequestType;
import com.example.app.finals.ResponseStatus;
import com.example.app.listeners.OnLocationSetListener;
import com.example.app.listeners.OnMarkersDownloadedListener;
import com.example.app.listeners.OnResultSetListener;
import com.example.app.ui_tools.ProgressAnimation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, RadiusDialog.RadiusDialogListener {

    private static final int DEFAULT_ZOOM          = 12;
    private static final String NEARBY_URL_REQUEST = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String GOOGLE_KEY         = "AIzaSyCIN8HCmGWXf5lzta5Rv2nu8VdIUV4Jp7s";

    // activity connectors
    public static final String NEARBY_KEY = "nearby key";
    public static final String RADIUS = "radius";

    // instance state keys
    private static final String TITLES_KEY = "titles_k";
    private static final String LATITUDES_KEY = "lat_k";
    private static final String LONGITUDES_KEY = "lng_k";
    private static final String LAT_KEY = "lat_last_k";
    private static final String LNG_KEY = "lng_last_k";

    private GoogleMap mMap;
    private LocationCallback locationCallback;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location myLastLocation;
    private ProgressBar progressBar;

    private NearbyRequestType requestType;
    private int radius; //max radius can be 50000m

    // instance restoring control variables
    private boolean canRestore = false;
    private List<MarkerOptions> restoreMarkers = new ArrayList<>();

    private OnLocationSetListener onLocationSetListener;

    /**
     * Method to set the location listener.
     * @param listener to set
     */
    private void setOnLocationSetListener(OnLocationSetListener listener){
        onLocationSetListener = listener;
    }

    /**
     * Callback when the activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_maps);
        progressBar = findViewById(R.id.prog_bar);
        animateProgress(0,20,1000);
        restoreMarkers = new ArrayList<>();

        // restoring instance state if any
        if(savedInstanceState != null){
            String[] titles = savedInstanceState.getStringArray(TITLES_KEY);
            double[] latitudes = savedInstanceState.getDoubleArray(LATITUDES_KEY);
            double[] longitudes = savedInstanceState.getDoubleArray(LONGITUDES_KEY);
            // create a marker list, in order to be display then
            if (titles != null && latitudes != null && longitudes != null) {
                for (int i = 0; i < titles.length; i++) {
                    MarkerOptions newMarker = new MarkerOptions();
                    newMarker.title(titles[i]);
                    double lat = latitudes[i];
                    double lng = longitudes[i];
                    LatLng latLng = new LatLng(lat, lng);
                    newMarker.position(latLng);
                    newMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    // now the marker is created I add it on the marker list
                    restoreMarkers.add(newMarker);
                }
                canRestore = true;
            }
        }

        // obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
    }


    /**
     * Callback when the map fragment ui is ready.
     * If triggered it means surely a button has been pressed
     * @param googleMap the map
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);
        }

        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                setDeviceLocation();
                //Get button pressed information
                Intent requestInfo = getIntent();
                requestType = (NearbyRequestType) requestInfo.getSerializableExtra(NEARBY_KEY);
                radius = requestInfo.getIntExtra(RADIUS, 1000);

                if(canRestore){
                    mMap.clear();
                    displayMarkers(restoreMarkers);
                    restoreMarkers.clear();
                    animateProgress(20,100,1000);
                }
                else {
                    showPlaces(radius, requestType);
                }
            }
        });
    }


    /**
     * Method to get user location pointer on the map
     */
    @SuppressLint("MissingPermission")
    private void setDeviceLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            myLastLocation = task.getResult();
                            if (myLastLocation != null) {
                                //trigger the listeners
                                loadLocation(myLastLocation);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude()), DEFAULT_ZOOM));
                                animateProgress(20,100,1000);
                            }
                            else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        myLastLocation = locationResult.getLastLocation();
                                        loadLocation(myLastLocation);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude()), DEFAULT_ZOOM));
                                        animateProgress(20,100,1000);
                                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        }
                    }
                });
    }



    /**
     * method to get the url string containing all the place information
     * @param latitude     of the searching centre position
     * @param longitude    of the searching centre position
     * @param nearbyPlace  to search for
     * @param radius       of the research
     * @return URL string
     */
    private String getUrl(double latitude, double longitude, String nearbyPlace, int radius)
    {

        StringBuilder googleURL = new StringBuilder(NEARBY_URL_REQUEST);
        googleURL.append("location=").append(latitude).append(",").append(longitude);
        googleURL.append("&radius=").append(radius);
        googleURL.append("&type=").append(nearbyPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key=").append(GOOGLE_KEY);

        Log.d("GoogleMapsActivity", "url = " + googleURL.toString());

        return googleURL.toString();
    }


     private void showPlaces(final int radius, final NearbyRequestType type){
         setOnLocationSetListener(new OnLocationSetListener() {
             /**
              * Callback when the position is ready
              * @param location the centre of the research
              */
             @Override
             public void onLocationSet(Location location) {
                 //create the request
                 String url = getUrl(myLastLocation.getLatitude(), myLastLocation.getLongitude(), type.toString(), radius);
                 Object[] transferData = new Object[2];
                 transferData[0] = mMap;
                 transferData[1] = url;
                 //the request will be downloaded and displayed
                 GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces();
                 getNearbyPlaces.execute(transferData);
                 getNearbyPlaces.setOnResultSetListener(new OnResultSetListener() {
                     /**
                      * Callback when the response result is ready
                      * @param result to get
                      */
                     @Override
                     public void onResultSet(String result) {
                         showResponseInfo(result);
                     }
                 });
             }
         });
     }





    /**
     * Callback for the activity result. If check is passed, let the method execute
     * @param requestCode activity request code
     * @param resultCode  activity result code
     * @param data        intent representing the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                setDeviceLocation();
            }
        }
    }

    //INSTANCE SAVE

    /**
     * Callback to save the state when necessary
     * @param savedInstanceState Bundle where to save places information
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // calling super class method
        super.onSaveInstanceState(savedInstanceState);
        // getting the list of found nearby places
        List<MarkerOptions> markerList = GetNearbyPlaces.markerList;
        // only the array list titles will tell how many places by its size
        if(markerList.size() > 0) {
            String[] title = new String[markerList.size()];
            double[] lat = new double[markerList.size()];
            double[] lng = new double[markerList.size()];
            for (int currentMarker = 0; currentMarker < markerList.size(); currentMarker++) {
                // filling the arrays
                MarkerOptions marker = markerList.get(currentMarker);
                title[currentMarker] = marker.getTitle();
                lat[currentMarker] = marker.getPosition().latitude;
                lng[currentMarker] = marker.getPosition().longitude;
            }
            // now arrays are filled with the information I need
            savedInstanceState.putStringArray(TITLES_KEY, title);
            savedInstanceState.putDoubleArray(LATITUDES_KEY, lat);
            savedInstanceState.putDoubleArray(LONGITUDES_KEY, lng);
            // saving current position
            if (myLastLocation != null) {
                savedInstanceState.putDouble(LAT_KEY, myLastLocation.getLatitude());
                savedInstanceState.putDouble(LNG_KEY, myLastLocation.getLongitude());
            }
        }
    }

    /**
     * Callback when app has been resume from onPause. It recreate the precedent state
     * @param markerList the list of the marker representing the precedent state
     */
    private void displayMarkers(List<MarkerOptions> markerList){
        for(int currentMarker = 0; currentMarker < markerList.size(); currentMarker++) {
            mMap.addMarker(markerList.get(currentMarker));
        }
    }

    /**
     * Method to recognize the response status
     * and act accordingly (show different dialog).
     * Act normally if the status is OK
     * To see all the possible status see {@link ResponseStatus}
     * @param status of the response
     */
    private void showResponseInfo(String status){
        switch (status) {
            case ResponseStatus.ZERO_RESULTS:
                RadiusDialog dialog = new RadiusDialog(radius);
                try {
                    dialog.show(getSupportFragmentManager(), "example dialog");
                }
                catch(IllegalStateException e){
                    //just ignore it
                }
                break;
            case ResponseStatus.NOT_FOUND:
                DialogFactory.showNotFoundAlertDialog(this);
                break;
            case ResponseStatus.OVER_QUERY_LIMIT:
                DialogFactory.showOverQueryAlertDialog(this);
                break;
            case ResponseStatus.NO_CONNECTION:
                DialogFactory.showNoConnectionAlertDialog(this);
                break;
                // FOLLOWING STATES SHOULD BE MANAGED BY PROGRAMMERS, THEY ARE NOT USER FAULT
            case ResponseStatus.INVALID_REQUEST:
            case ResponseStatus.CONNECTION_LOW:
                DialogFactory.showNoConnectionAlertDialog(this);
                break;
            case ResponseStatus.UNKNOWN_ERROR:
                DialogFactory.showUnknownErrorAlertDialog(this);
                break;
            case ResponseStatus.REQUEST_DENIED:
                DialogFactory.showRequestDeniedAlertDialog(this);
                break;
        }
    }

    /**
     * Method that triggered the {@link OnLocationSetListener}
     * @param location my last location
     */
    private void loadLocation(Location location){
        if(onLocationSetListener != null){
            onLocationSetListener.onLocationSet(location);
        }
    }

    private void animateProgress(int from, int to, int duration){
        Animation anim = new ProgressAnimation(progressBar, from, to);
        anim.setDuration(duration);
        if(to == 100){
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO: shuld make progress bar floating or transparent
                    progressBar.setVisibility(View.GONE); // resizing is horrible
                }

            });
        }
        progressBar.setAnimation(anim);
    }

    public void onRadiusDialogResult(int radius){
        startActivity(IntentFactory.createNearbyRequestIntent(this, requestType, radius));
    }
}
