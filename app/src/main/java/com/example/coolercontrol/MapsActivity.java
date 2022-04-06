package com.example.coolercontrol;


import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastKnownLocation;
    private static String KEY_LOCATION = "location";
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng defaultLocation = new LatLng(30.612651, -96.333572);
    private static final String TAG = MapsActivity.class.getSimpleName();
    private TextView txtLocation;
    private LocationCallback mCallback;
    private LocationRequest mREQUEST;
    private ArrayList<LatLng> geoPoints;
    Polyline tracker;

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
        findViewById(R.id.endButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationWizardry();
            }
        });

        mCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                super.onLocationResult(locationResult);
                if(locationResult == null){
                    Log.d(TAG, "locationResult null");
                    return;
                }
                Log.d(TAG, "received " + locationResult.getLocations().size() + "locations");
                for (Location loc : locationResult.getLocations()){
                    txtLocation.append("Lat:" + loc.getLatitude() + ",Lon:" + loc.getLongitude());
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability){
                Log.d(TAG, "locationAvailability is " + locationAvailability.isLocationAvailable());
                super.onLocationAvailability(locationAvailability);
            }
        };

        // check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request for permission
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this, permission.ACCESS_FINE_LOCATION)){
                showRationale();
            }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    2);
            }
        } else {
            locationWizardry();
        }
    }

    //Gets last location
    @SuppressLint("MissingPermission")
    private void locationWizardry(){
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>(){
            @Override
            public void onSuccess(Location location){
                if(location != null){
                    KEY_LOCATION = location.getProvider() + ":Accu:(" + location.getAccuracy() + "). Lat:" + location.getLatitude() + ",Lon:" + location.getLongitude();
                    txtLocation.setText(KEY_LOCATION);
                }
            }
        });

        createLocRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mREQUEST);

        SettingsClient client = LocationServices.getSettingsClient(MapsActivity.this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e){
                if(e instanceof ResolvableApiException){

                try{
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this, 500);
                }catch (IntentSender.SendIntentException sendEx){

                }
            }
        }
    });
}
    @Override
    protected void onSaveInstanceState(Bundle outState){
        if(mMap != null){
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }
    //when map is created
    @Override
    public void onMapReady(GoogleMap map) {
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
        mMap.addMarker(new MarkerOptions()
                .position(defaultLocation)
                .title("Zachary"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));

    }
    //gets device location
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
    //check location perms
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true;
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationClickListener(this::onMyLocationClick);
            mMap.setOnMyLocationButtonClickListener(this::onMyLocationButtonClick);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    //request permissions
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
    //Adds mylocation button
    private void updateLocationUI(){
        if (mMap == null){
            return;
        }
        try{
            if(permissionGranted){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    //reaction when location icon clicked
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }
    //reaction when location button clicked
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }
    //ask for perms dialog
    private void showRationale(){
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("We need this, just suck it up and grant us the permission :)").setPositiveButton("Sure", (dialogInterface, i) -> {
            ActivityCompat.requestPermissions(this, new String[]{permission.ACCESS_COARSE_LOCATION},2);
            dialogInterface.dismiss();
        }).create();
        dialog.show();
    }
    //reaction for on resume
    @Override
    public void onResume(){
        super.onResume();
        startLocationUpdates();
    }
    //reaction for on pause
    @Override
    public void onPause(){
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mCallback);;
    }
    //start location updates
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates(){
        mFusedLocationClient.requestLocationUpdates(mREQUEST, mCallback, null);
    }
    //mrequest parameters
    @SuppressLint("MissingPermission")
    protected void createLocRequest(){
        mREQUEST = new LocationRequest();
        mREQUEST.setInterval(10000); //request location every 10s
        mREQUEST.setFastestInterval(5000); //fastest interval is 5s
        mREQUEST.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mFusedLocationClient.requestLocationUpdates(mREQUEST,mCallback, Looper.getMainLooper());
    }

    public void handleNewLocation(Location location){
        Log.d(TAG, location.toString());

        double currentLat = location.getLatitude();
        double currentLong = location.getLongitude();
        LatLng latLng = new LatLng(currentLat, currentLong);

        geoPoints.add(latLng);

        drawRoute(geoPoints);
    }
    public void drawRoute(ArrayList<LatLng> location){
        mMap.clear();
        Polyline route = mMap.addPolyline(new PolylineOptions());
        route.setPoints(geoPoints);
    }

/*    @Override
   protected void onStart(){
       super.onStart();
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

