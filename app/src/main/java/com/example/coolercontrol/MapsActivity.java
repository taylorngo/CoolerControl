//Map Activity (need comments)
package com.example.coolercontrol;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.translation.ViewTranslationCallback;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.graphql.GraphQLRequest;
import com.amplifyframework.api.graphql.PaginatedResult;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelPagination;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Coordinate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.Date;
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
    private static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    private static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";
    private boolean startTracking;
    private List<Double> longitude = new ArrayList<>();
    private List<Double> latitude = new ArrayList<>();
    public List<Double> cloudLongitude = new ArrayList<>();
    public List<Double> cloudLatitude = new ArrayList<>();
    private ArrayList<LatLng> geoPoints = new ArrayList<>();
    public ArrayList<LatLng> cloudPoints = new ArrayList<>();
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    private Button cloudSaveButton;
    private Button cloudLoadButton;
    ListView listView;
    Context mContext;
    Context sContext;
    String cloudName;
    public ArrayList<Coordinate> loaded = new ArrayList<>();
    public RecyclerAdapter recyclerAdapter;


    //location permissions
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
            @SuppressLint("MissingPermission")
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

                        return infoWindow;
                    }
                });
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("route"));

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.startButton);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.endButton);
        cloudSaveButton = (Button) findViewById(R.id.saveButton);
        cloudLoadButton = (Button) findViewById(R.id.loadButton);
        loaded.clear();
        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                startLocationService();
                mMap.setMyLocationEnabled(true);
                startTracking = true;
                setButtonsState(true);
            }
        });
        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishTracking();
            }
        });
        cloudSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sContext = MapsActivity.this;
                final Dialog dialog = new Dialog(sContext);
                dialog.setContentView(R.layout.cloud_name);
                final EditText et = (EditText) dialog.findViewById(R.id.cloud_name);
                Button writeButton = (Button) dialog.findViewById(R.id.ok_button);

                //alertDialogBuilder.setView(et);
                dialog.show();
                // set dialog message
                writeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cloudName = et.getText().toString();
                        dialog.dismiss();
                        saveTracking();
                    }
                });

            }
        });
        cloudLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext = getApplicationContext();
                PopupWindow popupWindow = new PopupWindow(mContext);
                recyclerAdapter = new RecyclerAdapter(mContext, R.layout.recycler_view, loaded);
                listView = new ListView(mContext);
                listView.setAdapter(recyclerAdapter);
                listView.setOnItemClickListener(cloudListClickListener);
                popupWindow.setFocusable(true);
                popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
                popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.spinner_background));

                popupWindow.setContentView(listView);
                popupWindow.showAsDropDown(view, 0, 0);
            }
        });
        setButtonsState(isLocationServiceRunning());
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receiver received");
            geoPoints = intent.getParcelableArrayListExtra("theRoute");
            PolylineOptions routeOptions = new PolylineOptions().addAll(geoPoints).color(R.color.ColorPolyline).width(10).visible(true);
            mMap.addPolyline(routeOptions);

            LatLng firstMarker = geoPoints.get(0);
            LatLng lastMarker = geoPoints.get(geoPoints.size() - 1);


            mMap.addMarker(new MarkerOptions().position(firstMarker));
            mMap.addMarker(new MarkerOptions().position(lastMarker));
        }
    };

    //function to check if location service is running
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

    //function to start location service
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "location service started", Toast.LENGTH_SHORT).show();
        }
    }

    //function to stop location service
    private void stopLocationService(){
        if(isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "location service stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        setButtonsState(startTracking);
        //queryFirstPage();
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



    //function to prompt users if they want to end tracking
    public void finishTracking(){
        startTracking = false;
        AlertDialog.Builder finishedDialog = new AlertDialog.Builder(MapsActivity.this);
        finishedDialog.setTitle("Confirm tracking complete?");
        finishedDialog.setMessage("This will stop and save the current tracking if you continue.");
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

    //function to store tracking data into phone
    public void saveTracking() {
        for (int i = 0; i < geoPoints.size(); i++) {
            latitude.add(geoPoints.get(i).latitude);
            longitude.add(geoPoints.get(i).longitude);
        }

        String date1 = com.amazonaws.util.DateUtils.formatISO8601Date(new Date());
        Coordinate item = Coordinate.builder()
                .name(cloudName)
                .datetime(new Temporal.DateTime(date1))
                .latitude(latitude)
                .longitude(longitude)
                .build();
        Amplify.API.mutate(
                ModelMutation.create(item),
                response -> Log.i("Amplify", "Added route with ID: " + response.getData().getId()),
                error -> Log.e("Amplify", "Could not save item to DataStore", error)
        );

    }

    //check if user has location permissions
    private void checkLocationPerms() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            if (!EasyPermissions.hasPermissions(this, LOCATION_PERM)) {
                EasyPermissions.requestPermissions(this, getString(R.string.rationale_bluetooth), LOCATION_PERMISSION_REQUEST_CODE, LOCATION_PERM);
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
        setButtonsState(isLocationServiceRunning());
        queryFirstPage();
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
    private final AdapterView.OnItemClickListener cloudListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cloudLatitude.clear();
                cloudLongitude.clear();
                mMap.clear();
                cloudPoints.clear();
                Toast.makeText(getApplicationContext(), "You select value from list one " + loaded.get(i).getName(), Toast.LENGTH_LONG).show();
                cloudLongitude = loaded.get(i).getLongitude();
                cloudLatitude = loaded.get(i).getLatitude();
                if(cloudLongitude.size() > 0) {
                    for (int j = 0; j < cloudLongitude.size(); j++) {
                        cloudPoints.add(new LatLng(cloudLatitude.get(j), cloudLongitude.get(j)));
                    }

                    PolylineOptions cloudOptions = new PolylineOptions().addAll(cloudPoints).color(R.color.ColorPolyline).width(10).visible(true);
                    Polyline polyline = mMap.addPolyline(cloudOptions);

                    LatLng cloudFirstMarker = cloudPoints.get(0);
                    LatLng cloudLastMarker = cloudPoints.get(cloudPoints.size() - 1);


                    mMap.addMarker(new MarkerOptions().position(cloudFirstMarker));
                    mMap.addMarker(new MarkerOptions().position(cloudLastMarker));
                }
        }
    };

    public void queryFirstPage() {
        query(ModelQuery.list(Coordinate.class, ModelPagination.limit(1_000)));
    }

    private void query(GraphQLRequest<PaginatedResult<Coordinate>> request) {
        if (loaded.size() > 0) {
            loaded.clear();
        }
            Amplify.API.query(
                    request,
                    response -> {
                        if (response.hasData()) {
                            for (Coordinate coordinate : response.getData()) {
                                Log.d("MyAmplifyApp", coordinate.getId());
                                loaded.add(coordinate);
                            }
                            if (response.getData().hasNextResult()) {
                                query(response.getData().getRequestForNextResult());
                            }
                        }
                    },
                    failure -> Log.e("MyAmplifyApp", "Query failed.", failure)
            );
        }
}

