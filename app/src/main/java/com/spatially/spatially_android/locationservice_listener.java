package com.spatially.spatially_android;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class locationservice_listener extends Service {

    LocationManager mLocationManager;
    LocationListener mLocationListener;
    boolean live;
    Toast toast;
    SharedPreferences sharedPref;
    Boolean logging;
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
    public ComponentName startService(Intent service) {

        return super.startService(service);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        //Toast.makeText(this, "LocationListener Destroyed!", Toast.LENGTH_LONG).show();

        super.onDestroy();
        //Toast.makeText(this, "LocationListener Destroyed!", Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    // This is called when service is started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (getApplicationContext() != null) {

            sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);
            if (sharedPref.contains("logging")) {
                logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
            }

            //SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);
            //Toast.makeText(getApplicationContext(), "LocationServices started!", Toast.LENGTH_LONG).show();
            //System.out.println("LocationServices started!");

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.enable_locations), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            }

            mLocationManager = (LocationManager) getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);

            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.

                        toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.enable_locations), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                    }

                    Intent intent = new Intent("LocationUpdates");
                    intent.putExtra("Latitude", (float) location.getLatitude());
                    intent.putExtra("Longitude", (float) location.getLongitude());
                    intent.putExtra("Accuracy", (float) location.getAccuracy());
                    intent.putExtra("Time", (long) location.getTime());
                    getApplicationContext().sendBroadcast(intent);

                    if (logging == true) {
                        Log.i(getResources().getString(R.string.location), "Time: " + location.getTime() + " Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude() + " Accuracy: " + location.getAccuracy());
                    }

                    //Toast.makeText(getApplicationContext(), "Time: " + location.getTime() + "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();


                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {


                }

                @Override
                public void onProviderDisabled(String provider) {

                    toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.enable_locations), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                }


            };

            if (intent.hasExtra("live")) {

                live = intent.getBooleanExtra("live", false);

                if (live == true) {

                    if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Long.parseLong(getResources().getString(R.string.minimumUpdateTimeIntervalinMilliSeconds)), Float.parseFloat(getResources().getString(R.string.minimumDistanceBetweenUpdatesinMeters)), mLocationListener);
                    } else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Long.parseLong(getResources().getString(R.string.minimumUpdateTimeIntervalinMilliSeconds)), Float.parseFloat(getResources().getString(R.string.minimumDistanceBetweenUpdatesinMeters)), mLocationListener);

                    }

                } else {

                    if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Long.parseLong(getResources().getString(R.string.minimumUpdateTimeIntervalinMilliSeconds)), Float.parseFloat(getResources().getString(R.string.minimumDistanceBetweenUpdatesinMeters)), mLocationListener);
                    }


                }
            }

            //Toast.makeText(getApplicationContext(), "live: " + live, Toast.LENGTH_LONG).show();

        }
            return START_STICKY;


    }

}









