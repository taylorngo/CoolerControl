package com.example.coolercontrol;


import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastKnownLocation;
    private static final String KEY_LOCATION = "location";
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng defaultLocation = new LatLng(30.612651, -96.333572);
    private static final String TAG = MapsActivity.class.getSimpleName();

    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private TextView txtLocation;
    private StringBuilder stringBuilder;
    private boolean isContinue = false;
    private boolean isGPS = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        txtLocation = (TextView) findViewById(R.id.location_txt);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Set up FusedLocation
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){getDeviceLocation();}
        });
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
        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        } else {
            // already permission granted
            // get location here
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    wayLatitude = location.getLatitude();
                    wayLongitude = location.getLongitude();
                    txtLocation.setText(String.format(Locale.US, "%s -- %s", wayLatitude, wayLongitude));
                }
            });*/
        }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        if(mMap != null){
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }
    @Override
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
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();

        // Add a marker in Texas and move the camera
        LatLng texas = new LatLng(30, -96);
        mMap.addMarker(new MarkerOptions()
                .position(texas)
                .title("Marker in Texas"));
        mMap.addMarker(new MarkerOptions()
                .position(defaultLocation)
                .title("Zachary"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));

    }
    private void getDeviceLocation(){
        try{
            if(permissionGranted){
                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>(){
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            lastKnownLocation = task.getResult();
                            if(lastKnownLocation != null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults");
                            Log.e(TAG, "Exceptions: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }

            });
        }
    } catch(SecurityException e){
            Log.e("Exceptions: %s", e.getMessage());
        }
    }
    private void getLocationPermission(){
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            permissionGranted = true;
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionGranted = false;
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                permissionGranted = true;
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }
    private void updateLocationUI(){
        if (mMap == null){
            return;
        }
        try{
            if(permissionGranted){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else{
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e){
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private void showUserLocation(){
        mMap.isMyLocationEnabled();
    }
/*    @Override
   protected void onStart(){
       super.onStart();
    }
    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    protected void onStop(){
        super.onStop();
        //Save values
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }*/


}

