//Location Services (need comments)
package com.example.coolercontrol;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class LocationService extends Service {

    private static final String TAG = LocationService.class.getSimpleName();
    private static final int LOCATION_SERVICE_ID = 175;
    private static final long UPDATE_INTERVAL = 15000;  /* 15 secs */
    private static final long FASTEST_INTERVAL = 5000; /* 5 sec */
    private static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    private static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";
    private ArrayList<LatLng> geoPoints = new ArrayList<>();
    Context mContext;
    
    //LocationCallback function to acquire coordinates of user's location
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if( locationResult != null && locationResult.getLastLocation() != null){
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d(TAG, latitude + ", " + longitude);
                handleNewLocation(locationResult);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //function to start location services
    private void startLocationService(){
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(notificationManager != null && notificationManager.getNotificationChannel(channelId) == null){
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location services");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(LOCATION_SERVICE_ID, builder.build());
    }

    //function to pause location services
    private void stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);

        Intent geoPointIntent = new Intent("route");
        geoPointIntent.putParcelableArrayListExtra("theRoute", geoPoints);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(geoPointIntent);

        stopSelf();
    }

    //controls buttons to start and stop location services
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null){
            String action = intent.getAction();
            if (action != null){
                if (action.equals(ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    //function to update an array with user's coordinates
    private void handleNewLocation(LocationResult locationResult) {

        double currentLatitude = locationResult.getLastLocation().getLatitude();
        double currentLongitude = locationResult.getLastLocation().getLongitude();
        LatLng point = new LatLng(currentLatitude, currentLongitude);
        geoPoints.add(point);

    }


}
