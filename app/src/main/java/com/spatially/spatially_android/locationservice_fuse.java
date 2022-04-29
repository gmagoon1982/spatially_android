package com.spatially.spatially_android;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class locationservice_fuse extends Service {

    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    int NOTIFICATION_ID = 1;
    String NOTIFICATION_CHANNEL_ID = "locations";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_HIGH);

            NotificationManager notifManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

            notifManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Locations for API 26+ Running");

            startForeground(NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Toast.makeText(getApplicationContext(), "locationservice started!", Toast.LENGTH_LONG).show();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(Long.parseLong(getResources().getString(R.string.fused_location_provider_client_setFastestInterval)));
        locationRequest.setSmallestDisplacement(0);

        // PRIORITY_BALANCED_POWER_ACCURACY = GPS
        // PRIORITY_BALANCED_POWER_ACCURACY = NETWORK ?

        if (intent.hasExtra("live")) {
            if (intent.getBooleanExtra("live", false) == true) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else if (intent.getBooleanExtra("live", false) == false) {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            }
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                //System.out.println("time: " + locationResult.getLastLocation().getTime() + ", latitude: " + locationResult.getLastLocation().getLatitude() + ", longitude: " + locationResult.getLastLocation().getLongitude() + ", accuracy: " + locationResult.getLastLocation().getAccuracy() + " m");

                Intent intent = new Intent("LocationUpdates");
                intent.putExtra("Latitude", (float) locationResult.getLastLocation().getLatitude());
                intent.putExtra("Longitude", (float) locationResult.getLastLocation().getLongitude());
                intent.putExtra("Accuracy", (float) locationResult.getLastLocation().getAccuracy());
                intent.putExtra("Time", (long) locationResult.getLastLocation().getTime());
                getApplicationContext().sendBroadcast(intent);


            }

        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(getApplicationContext(), getResources().getString(R.string.locations_not_enabled_inside_service), Toast.LENGTH_LONG).show();
        }

        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        return START_STICKY;

    }


    @Override
    public ComponentName startService(Intent service) {

        return super.startService(service);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        //Toast.makeText(this, "LocationListener Destroyed!", Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }


    }



}