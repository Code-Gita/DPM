package com.example.gps_activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.telephony.TelephonyManager;

import android.provider.Settings.Secure;
import android.widget.Toast;
import java.util.Arrays;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    private Button startActivity, stopActivity;
    private TextView latitude, longitude, speed, gpsTime, imeiNumber, phoneNumber, requestView, responseView;
    private BroadcastReceiver broadcastReciever;
    private RequestQueue reqQueue;
    private StringRequest strRequest;
    private String ImeiNumber,mobNumber;
    private StringBuilder websiteURL = new StringBuilder(" ");
    private static final int REQUEST_CODE_LOCATION = 1000;
    private static final int REQUEST_CODE_DEVICE_INFO = 2000;
    private static final int REQUEST_CODE_INTERNET = 3000;
    private LocationManager locationManager;
    private boolean isGPS=false;
    private String TAG ="debug";
    private Intent intent1;

    @Override
    protected void onResume() {
        super.onResume();

        if (broadcastReciever == null) {
            broadcastReciever = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d("Debug", "onReceive: cordinates:  " + intent.getExtras().get("coordinates"));

                    sendRequestGetResponse(intent);

                }
            };
        }
        registerReceiver(broadcastReciever, new IntentFilter("location_update"));
    }


    @SuppressLint("MissingPermission")
    private void sendRequestGetResponse(Intent i) {
        if(((String) i.getExtras().get("coordinates")).equalsIgnoreCase( "LOCATION_STOPPED")==Boolean.TRUE)
        {
            Log.d("Debug", "sendRequestGetResponse: location stopped");

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                Log.d(TAG, "sendRequestGetResponse: GPS provider enabled");
            }
            else
            {
                Log.d(TAG, "sendRequestGetResponse: GPS provider disabled");
                try
                {
                    intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent1);
                }
                catch( Exception e)
                {
                    Log.d(TAG, "sendRequestGetResponse: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        else {
            Log.d("Debug", "sendRequestGetResponse: location else :  "+(String)i.getExtras().get("coordinates"));

            reqQueue = Volley.newRequestQueue(getApplicationContext());
            String cordinates = (String) i.getExtras().get("coordinates");
            String usrLat = cordinates.split("#")[0];
            String usrLong = cordinates.split("#")[1];
            String usrGpsDateTime = cordinates.split("#")[2];
            String usrSpeed = cordinates.split("#")[3];
            Log.d("Debug", "sendRequestGetResponse: " + ImeiNumber + "\n phone number : " + mobNumber);

            Log.d("Debug", "sendRequestGetResponse: " + usrLat + " usrLong : " + usrLong);

            String tempUrl = "?aid=" + Secure.getString(this.getContentResolver(), Secure.ANDROID_ID) +
                    "&lat=" + usrLat + "&longitude=" + usrLong + "&time=" + usrGpsDateTime + "&s=" + usrSpeed + "&Imei=" + ImeiNumber + "&mob=" + mobNumber;

            Log.d("Debug", "sendRequestGetResponse: tmpurl :" + tempUrl);
            latitude.setText(usrLat);
            longitude.setText(usrLong);
            speed.setText(usrSpeed);
            gpsTime.setText(usrGpsDateTime);

            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager != null) {
                ImeiNumber = telephonyManager.getImei();
            }
            mobNumber = telephonyManager.getLine1Number();

            imeiNumber.setText(ImeiNumber);
            phoneNumber.setText(mobNumber);

            websiteURL.append("http://thetrackme.com/gps_logger.php");
            websiteURL.append(tempUrl);
            Log.d("Request", websiteURL.toString());

            requestView.setText(websiteURL);
            strRequest = new StringRequest(Request.Method.GET, websiteURL.toString(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("Response", "onResponse:  " + response.toString());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("ERROR", "onErrorResponse: " + error.toString());

                }
            });

            reqQueue.add(strRequest);
            websiteURL.setLength(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReciever != null)
            unregisterReceiver(broadcastReciever);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Debug", "onCreate: In onCreate ");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        startActivity = (Button) findViewById(R.id.startServiceBtn);
        stopActivity = (Button) findViewById(R.id.stopServiceBtn);
        latitude = (TextView) findViewById(R.id.t2);
        longitude = (TextView) findViewById(R.id.t);
        speed = (TextView) findViewById(R.id.t3);
        gpsTime = (TextView) findViewById(R.id.t4);
        requestView = (TextView) findViewById(R.id.RequestView);
        responseView = (TextView) findViewById(R.id.ResponseView);
        imeiNumber = (TextView) findViewById(R.id.Imei_txt5);
        phoneNumber = (TextView) findViewById((R.id.txt6));

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("Debug", "onCreate:  granting internet permission");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, REQUEST_CODE_INTERNET);
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("Debug", "onCreate:  granting internet permission");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_DEVICE_INFO);
        }

        enableButton();

        startActivity.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),GPS_Service.class);
                startActivity.setEnabled(false);
                startService(i);

                Log.d("Debug", "onClick: Inside onclick started service... ");
                stopActivity.setEnabled(true);
            }
        });

        stopActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Debug", "onClick: Stopping Service");

                Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                stopService(i);
                latitude.setText("");
                longitude.setText("");
                speed.setText("");
                gpsTime.setText("");
                imeiNumber.setText("");
                responseView.setText("");
                phoneNumber.setText("");
                startActivity.setEnabled(true);
                stopActivity.setEnabled(false);
            }
        });
    }

    private void enableButton() {
        Log.d("Debug", "enableButton: Start ");
        startActivity.setEnabled(true);
        stopActivity.setEnabled(false);
    }

    private void disableButton() {
        Log.d("Debug", "disableButton: Start ");
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        stopService(i);
        startActivity.setEnabled(true);
        stopActivity.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Debug", "onRequestPermissionsResult: requestCode : " +requestCode + " \n grant results : "+ Arrays.toString(grantResults));
        switch(requestCode)
        {
            case REQUEST_CODE_LOCATION:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d("Debug", "onRequestPermissionsResult: permission not granted");
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_LOCATION);
                    }
                    else
                    {
                        /*Log.d("debug", "onRequestPermissionsResult: location permision denied 1");
                        Toast.makeText(getApplicationContext(),"Please switch on the location servies to start the service again 1",Toast.LENGTH_LONG).show();
                        disableButton();*/
                    }
                    enableButton();
                }
                else if(grantResults[0] == PackageManager.PERMISSION_DENIED)
                {
                    Log.d("debug", "onRequestPermissionsResult: location permision denied");
                    Toast.makeText(getApplicationContext(),"Please switch on the location servies to start the service again",Toast.LENGTH_LONG).show();
                    disableButton();

                }
                break;
            }
            case REQUEST_CODE_INTERNET:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED )
                    {
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},REQUEST_CODE_INTERNET);
                    }
                    enableButton();
                }
                else if(grantResults[0] == PackageManager.PERMISSION_DENIED)
                {

                }
                break;
            }
            case REQUEST_CODE_DEVICE_INFO:
            {
                Log.d("Debug", "onRequestPermissionsResult: here XX");
                {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.

                    } else {

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                REQUEST_CODE_DEVICE_INFO);

                        intent1 = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                        startActivity(intent1);

                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return;
                }
                //break;
            }

            default:
                break;

        }
    }
}
