//Map Activity
package com.example.coolercontrol;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MapsActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    private Activity A_;
    private LocationReceiver mLocationReceiver;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private static final String KEY_LOCATION = "location";
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng defaultLocation = new LatLng(30.612651, -96.333572);
    private static final String TAG = MapsActivity.class.getSimpleName();
    private boolean startTracking;
    private ArrayList<LatLng> geoPoints = new ArrayList<>();
    private LocationUpdatesService mService;
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    double mLatitude, mLongitude;
    private TextView txtLocation;
    private String provider;
    private static final String[] LOCATION_PERM = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        txtLocation = (TextView) findViewById(R.id.location_txt);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.setMyLocationEnabled(true);
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View infoWindow = getLayoutInflater().inflate(R.layout.activity_maps, (FrameLayout) findViewById(R.id.map), false);

                        txtLocation = infoWindow.findViewById(R.id.location_txt);
                        txtLocation.setText(marker.getTitle());

                        return infoWindow;
                    }
                });
                mMap.addMarker(new MarkerOptions()
                        .position(defaultLocation)
                        .title("Zachary"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));

            }
        });

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.startButton);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.endButton);
        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.checkLocationPermission(MapsActivity.this)) {
                    Toast.makeText(MapsActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
                    registerLocationService();
                    startTracking = true;
                    setButtonsState(true);
                }

            }
        });
        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finishTracking();
                setButtonsState(false);
            }
        });

        //Set up FusedLocation
        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

/*        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(10*1000); //10 seconds
        locationRequest.setFastestInterval(5*1000); //5 seconds

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLongitude = location.getLongitude();
                        wayLatitude = location.getLatitude();
                        if (!isContinue) {
                            txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                        } else {
                            stringBuilder.append(wayLatitude);
                            stringBuilder.append("-");
                            stringBuilder.append(wayLongitude);
                            stringBuilder.append("\n\n");
                            txtLocation.setText(stringBuilder.toString());
                        }
                    }
                }
            }
        };
        */
        // check permission
        if(Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastLocation = location;


                            mLatitude = location.getLatitude();
                            mLongitude= location.getLongitude();

                        } else {
                            Toast.makeText(MapsActivity.this,
                                    R.string.rationale_location,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            // request for permission
        } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);

            }
    }
        }

    @Override
    protected void onStart(){
        super.onStart();
        
        setButtonsState(startTracking);


    }

    private class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey(LocationUpdatesService.GPS_NOT_ENABLED)) {
                    LocationRequest locationRequest = intent.getParcelableExtra(LocationUpdatesService.GPS_NOT_ENABLED);
                    checkForLocationSettings(locationRequest);
                }
                if (extras.containsKey(LocationUpdatesService.INTENT_LOCATION_VALUE)) {
                    Location location = intent.getParcelableExtra(LocationUpdatesService.INTENT_LOCATION_VALUE);
                    handleLocationUpdates(location);
                }
            }
        }

    }

    private void handleLocationUpdates(Location location) {
        String msg = "Current Location Update : " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d("LocationUpdates:", msg);
        txtLocation.setText(msg);
    }


    //Check for location settings.
    public void checkForLocationSettings(LocationRequest locationRequest) {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.addLocationRequest(locationRequest);
            SettingsClient settingsClient = LocationServices.getSettingsClient(MapsActivity.this);

            settingsClient.checkLocationSettings(builder.build())
                    .addOnSuccessListener(A_, new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            //Setting is success...
                            Toast.makeText(A_, "Enabled the Location successfully. Now you can start", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Enabled the Location successfully.");
                            registerLocationService();
                        }
                    })
                    .addOnFailureListener(A_, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    Log.i(TAG, "Location settings are not satisfied. Show the location prompt");
                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the
                                        // result in onActivityResult().
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(A_, LOCATION_PERMISSION_REQUEST_CODE);
                                    } catch (Exception e1) {
                                        Log.i(TAG, "Location not satisfied. Show the location enable dialog Error :", e1);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    Log.i(TAG, "Setting change is not available. Try in another device");
                                    Toast.makeText(A_, "Setting change is not available.Try in another device.", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

        } catch (Exception ex) {
            Log.i(TAG, "checkForLocationSettings :", ex);
        }
    }

    private void registerLocationService(){
        Intent intent = new Intent(A_, LocationUpdatesService.class);
        startService(intent);

        mLocationReceiver = new LocationReceiver();
        registerReceiver(mLocationReceiver, new IntentFilter(LocationUpdatesService.MY_LOCATION));
        Log.i(TAG, " Registered location broadcast receiver");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case LOCATION_PERMISSION_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes");
                        //PERMISSION
                        if (Utils.checkLocationPermission(A_)) {
                            registerLocationService();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes");
                        break;
                }
                break;
        }
    }

    private void initGoogleMapLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        /**
         * Location Setting API to
         */
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        /*
         * Callback returning location result
         */
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                super.onLocationResult(result);
                //mCurrentLocation = locationResult.getLastLocation();
                mCurrentLocation = result.getLocations().get(0);


                if(mCurrentLocation!=null)
                {
                    Log.e("Location(Lat)==",""+mCurrentLocation.getLatitude());
                    Log.e("Location(Long)==",""+mCurrentLocation.getLongitude());
                }


                MarkerOptions options = new MarkerOptions();
                options.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                Marker marker = mMap.addMarker(options);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
                /**
                 * To get location information consistently
                 * mLocationRequest.setNumUpdates(1) Commented out
                 * Uncomment the code below
                 */
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }

            //Location Meaning that all relevant information is available
            @Override
            public void onLocationAvailability(LocationAvailability availability) {
                //boolean isLocation = availability.isLocationAvailable();
            }
        };
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        //To get location information only once here
        mLocationRequest.setNumUpdates(3);
        //Acquired location information based on balance of battery and accuracy (somewhat higher accuracy)
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        /**
         * Stores the type of location service the client wants to use. Also used for positioning.
         */
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);
        locationResponse.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.e("Response", "Successful acquisition of location information!!");
                //
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        });
        //When the location information is not set and acquired, callback
        locationResponse.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e("onFailure", "Location environment check");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Check location setting";
                        Log.e("onFailure", errorMessage);
                }
            }
        });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
        }
        super.onSaveInstanceState(outState);
    }

/*    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.mMap = map;

        this.mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View infoWindow = getLayoutInflater().inflate(R.layout.activity_maps, (FrameLayout) findViewById(R.id.map), false);

                txtLocation = infoWindow.findViewById(R.id.location_txt);
                txtLocation.setText(marker.getTitle());

                return infoWindow;
            }
        });




        // Add a marker in Texas and move the camera
        LatLng texas = new LatLng(30, -96);
        mMap.addMarker(new MarkerOptions()
                .position(texas)
                .title("Marker in Texas"));
        mMap.addMarker(new MarkerOptions()
                .position(defaultLocation)
                .title("Zachary"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));

    }*/

    @AfterPermissionGranted(LOCATION_PERMISSION_REQUEST_CODE)
    public void locationTask(){
        if(hasLocationPermission()){
            initGoogleMapLocation();
            updateLocationUI();
            getDeviceLocation();
            handleNewLocation(mCurrentLocation);
        } else{
            EasyPermissions.requestPermissions(this,getString(R.string.rationale_location),LOCATION_PERMISSION_REQUEST_CODE,LOCATION_PERM);
        }

    }

    public void handleNewLocation(Location location) {

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng point = new LatLng(currentLatitude, currentLongitude);

        if(geoPoints.size() == 1){
            MarkerOptions options = new MarkerOptions().position(geoPoints.get(0)).title("Starting Point1");
            mMap.addMarker(options);
        }

        if(startTracking){
            geoPoints.add(point);

            PolylineOptions routeOptions = new PolylineOptions().addAll(geoPoints).color(R.color.ColorPolyline).width(10).visible(true);
            mMap.addPolyline(routeOptions);
        }


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, DEFAULT_ZOOM));
    }

    public void finishTracking(){
        startTracking = false;
        AlertDialog.Builder finishedDialog = new AlertDialog.Builder(MapsActivity.this);
        finishedDialog.setTitle("Confirm Run Completion?");
        finishedDialog.setMessage("This will stop and save the current run if you continue.");
        finishedDialog.setPositiveButton(R.string.cont, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        finishedDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startTracking = true;
                mRequestLocationUpdatesButton.setEnabled(true);
                mRemoveLocationUpdatesButton.setEnabled(false);
            }
        });
        finishedDialog.show();
    }

    private void getDeviceLocation() {
        try {
                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mCurrentLocation = task.getResult();
                            if (mCurrentLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mCurrentLocation.getLatitude(),
                                                mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults");
                            Log.e(TAG, "Exceptions: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }

                });
        } catch (SecurityException e) {
            Log.e("Exceptions: %s", e.getMessage());
        }
    }

    private boolean hasLocationPermission() {
        return EasyPermissions.hasPermissions(this, LOCATION_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case Utils.MY_PERMISSIONS_REQUEST_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    registerLocationService();
                }else{
                    Toast.makeText(this, "You have denied permission to access location", Toast.LENGTH_LONG).show();
                    finish();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
/*        permissionGranted = false;
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                permissionGranted = true;
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }*/
    }

    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if(mLocationReceiver != null){
            unregisterReceiver(mLocationReceiver);
        }
        stopService(new Intent(getApplicationContext(),LocationUpdatesService.class));
        Log.i("onPause", " GPS and Location Services are un-registered");

    }
    @Override
    protected void onResume(){
        super.onResume();

/*        if(Utils.checkLocationPermission(A_)){
            registerLocationService();
        }*/
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }
}

