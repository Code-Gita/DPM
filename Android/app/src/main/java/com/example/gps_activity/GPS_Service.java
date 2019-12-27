package com.example.gps_activity;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.os.Build;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationAvailability;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;
import android.text.TextUtils;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class GPS_Service extends Service {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    LocationManager locationManager;
    private String TAG= "Debug";
    private static final int REQUEST_CODE_LOCATION = 1000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate() {
        super.onCreate();
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(50);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        if(isLocationServiceAvailbale()==false)
        {
            Intent i = new Intent("location_update");
            String locationStopped ="LOCATION_STOPPED";
            i.putExtra("coordinates",locationStopped);
            sendBroadcast(i);
        }

        locationCallback = new LocationCallback()
        {
            Intent i = new Intent("location_update");
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                if (locationAvailability.isLocationAvailable() == false) {
                    String locationStopped ="LOCATION_STOPPED";
                    i.putExtra("coordinates",locationStopped);
                    sendBroadcast(i);
                }
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations())
                {
                    Log.d(TAG, "onCreate: location service availalble 2: "+isLocationServiceAvailbale());
                    Log.d(TAG, "onLocationResult: Latitude : "+String.valueOf(location.getLatitude())+
                            " \t Longitude : "+String.valueOf(location.getLongitude()));


                    i.putExtra("coordinates",location.getLongitude()+"#"+location.getLatitude()+
                            "#"+sdf.format(location.getTime())+
                            "#"+ String.valueOf(location.getSpeed()));


                    Log.d(TAG, location.getTime()+ "    --- onLocationResult: time : " +sdf.format(location.getTime())+" ----");
                    sendBroadcast(i);
                }
            }
        };

        try
        {
            //Log.d(TAG, "onCreate: location service availalble 9: "+isLocationServiceAvailbale());
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            //Log.d(TAG, "onCreate: location service availalble 10: "+isLocationServiceAvailbale());
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }
        catch (Exception e)
        {
            Log.e(TAG, "onCreate: : error"+e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public boolean isLocationServiceAvailbale()
    {
        String locationProviders;
        boolean isAvailable = false;
        int locationMode = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            isAvailable = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } else {
            locationProviders = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isAvailable = !TextUtils.isEmpty(locationProviders);
        }
        boolean coarsePermissionCheck = (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean finePermissionCheck = (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        return isAvailable && (coarsePermissionCheck || finePermissionCheck);
    }

}
