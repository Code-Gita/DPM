package com.example.gps_activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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

import android.provider.Settings.Secure;

public class MainActivity extends AppCompatActivity {

    private Button startActivity, stopActivity;
    private TextView latitude,longitude,speed,gpsTime,imeiNumber,requestView,responseView;
    private BroadcastReceiver broadcastReciever;
    private RequestQueue reqQueue;
    private StringRequest strRequest;
    private StringBuilder websiteURL = new StringBuilder(" ");
    private static final int REQUEST_CODE=1000;

    @Override
    protected void onResume() {
        super.onResume();

        if(broadcastReciever == null)
        {
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

    private void sendRequestGetResponse(Intent i) {
        reqQueue = Volley.newRequestQueue(getApplicationContext());
        String cordinates = (String) i.getExtras().get("coordinates");
        String usrLat = cordinates.split("#")[0];
        String usrLong = cordinates.split("#")[1];
        String usrGpsDateTime = cordinates.split("#")[2];
        String usrSpeed = cordinates.split("#")[3];
        Log.d("Debug", "sendRequestGetResponse: "+usrLat+ " usrLong : "+usrLong );

        String tempUrl = "?aid="+Secure.getString(this.getContentResolver(), Secure.ANDROID_ID) +
                "&lat="+usrLat+"&longitude="+usrLong+"&time="+usrGpsDateTime+"&s="+usrSpeed;
        Log.d("Debug", "sendRequestGetResponse: tmpurl :"+tempUrl);
        latitude.setText(usrLat);
        longitude.setText(usrLong);
        speed.setText(usrSpeed);
        gpsTime.setText(usrGpsDateTime);

        websiteURL.append("http://thetrackme.com/gps_logger.php");
        websiteURL.append(tempUrl);
        Log.d("Request", websiteURL.toString());

        requestView.setText(websiteURL);
        strRequest = new StringRequest(Request.Method.GET, websiteURL.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Response", "onResponse:  "+response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERROR", "onErrorResponse: "+error.toString());

            }
        });

        reqQueue.add(strRequest);
        websiteURL.setLength(0);
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
        startActivity = (Button) findViewById(R.id.startServiceBtn);
        stopActivity = (Button) findViewById(R.id.stopServiceBtn);
        latitude = (TextView) findViewById(R.id.t2);
        longitude = (TextView) findViewById(R.id.t);
        speed = (TextView) findViewById(R.id.t3);
        gpsTime = (TextView) findViewById(R.id.t4);
        requestView = (TextView) findViewById(R.id.RequestView);
        responseView = (TextView) findViewById(R.id.ResponseView);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
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
                responseView.setText("");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Debug", "onRequestPermissionsResult: requestCode : " +requestCode);
        switch(requestCode)
        {
            case REQUEST_CODE:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
                    }
                    enableButton();
                }
                else if(grantResults[0] == PackageManager.PERMISSION_DENIED)
                {

                }
            }
        }
    }
}
