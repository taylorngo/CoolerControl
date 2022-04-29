//Map Activity
package com.example.coolercontrol;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MapsActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private GoogleMap mMap;
    private Location mCurrentLocation;
    private static final String KEY_LOCATION = "location";
    private static final String TAG = MapsActivity.class.getSimpleName();
    private boolean startTracking;
    private ArrayList<LatLng> geoPoints = new ArrayList<>();
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;

    private static final String[] LOCATION_PERM = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLocationPerms();
        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View infoWindow = getLayoutInflater().inflate(R.layout.activity_maps, (FrameLayout) findViewById(R.id.map), false);

                        return infoWindow;
                    }
                });
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("route"));

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.startButton);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.endButton);
        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startLocationService();
                    mMap.setMyLocationEnabled(true);
                    startTracking = true;
                    setButtonsState(true);
            }
        });
        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finishTracking();
            }
        });
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receiver received");
            geoPoints = intent.getParcelableArrayListExtra("theRoute");
            PolylineOptions routeOptions = new PolylineOptions().addAll(geoPoints).color(R.color.ColorPolyline).width(10).visible(true);
            mMap.addPolyline(routeOptions);
        }
    };

    private boolean isLocationServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null){
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)){
                if(LocationService.class.getName().equals(service.service.getClassName())){
                    if(service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "location service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService(){
        if(isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "location service stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        setButtonsState(startTracking);
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
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes");
                        break;
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
        }
        super.onSaveInstanceState(outState);
    }



    public void finishTracking(){
        startTracking = false;
        AlertDialog.Builder finishedDialog = new AlertDialog.Builder(MapsActivity.this);
        finishedDialog.setTitle("Confirm Run Completion?");
        finishedDialog.setMessage("This will stop and save the current run if you continue.");
        finishedDialog.setPositiveButton(R.string.cont, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setButtonsState(false);
                stopLocationService();
            }
        });
        finishedDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setButtonsState(true);
            }
        });
        finishedDialog.show();
    }


    private boolean hasLocationPermission() {
        return EasyPermissions.hasPermissions(this, LOCATION_PERM);
    }

    private void checkLocationPerms(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            if(!EasyPermissions.hasPermissions(this, LOCATION_PERM)){
                EasyPermissions.requestPermissions(this,getString(R.string.rationale_bluetooth), LOCATION_PERMISSION_REQUEST_CODE, LOCATION_PERM);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

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

    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    protected void onResume(){
        super.onResume();
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

