package com.example.gps_activity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

//import java.util.Date;

public class GPS_Service extends Service {

    //private LocationListener locListner;
    //private LocationManager locManager;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    //private int runOnce=0;
    private String TAG= "Debug";


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

        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations())
                {
                    Log.d(TAG, "onLocationResult: Latitude : "+String.valueOf(location.getLatitude())+
                            " \t Longitude : "+String.valueOf(location.getLongitude()));
                    Intent i = new Intent("location_update");

                    /*i.putExtra("coordinates",location.getLongitude()+"#"+location.getLatitude()+
                            "#"+new java.text.SimpleDateFormat("yyyy-MM-dd").format(location.getTime())+
                            "T"+new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(location.getTime())+
                            "#"+ String.valueOf(location.getSpeed()));*/
                    //String time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(location.getTime());

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
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }
        catch (Exception e)
        {
            Log.e(TAG, "onCreate: : error"+e.getMessage());
            e.printStackTrace();
        }

    }
/*@SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        Log.d("Debug", "onCreate: Inside oncreate service ");
        locListner = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d("Debug", "onLocationChanged: calling location Changed ");
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLongitude()+"#"+location.getLatitude()+
                        "#"+new java.text.SimpleDateFormat("yyyy-MM-dd").format(location.getTime())+
                        "T"+new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(location.getTime())+
                        "#"+ String.valueOf(location.getSpeed()));
                //String time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(location.getTime());
                sendBroadcast(i);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

                Log.d("Debug", "onStatusChanged: ");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Debug", "onProviderEnabled: ");

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Debug", "onProviderDisabled: ");
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        Log.d("Debug", "onCreate: here 1 ");
        locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        Log.d("Debug", "onCreate: here 2 ");

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0,locListner);

        //GpsStatus gps_stat;
        //Log.d("Debug", "onCreate: "+locManager.getGpsStatus(GpsStatus gps_stat));
        Log.d("Debug", "onCreate: "+locManager.getProviders(true));
        Log.d("Debug", "onCreate: "+locManager.getProvider("gps"));
        Log.d("Debug", "onCreate: "+locManager.getLastKnownLocation("network"));


    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*if(locManager!=null)
        {
            locManager.removeUpdates(locListner);
        }*/
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
