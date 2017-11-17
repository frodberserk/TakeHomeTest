package com.demo.takehometest.service;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;

import org.greenrobot.eventbus.EventBus;

public class LocationService extends Service {

    /**
     * Simple Tag for service logs
     */
    private static String TAG = LocationService.class.getSimpleName();

    /**
     * Location request intervals.
     */
    private static final int INTERVAL = 10 * 1000;        //10 seconds
    private static final int FASTEST_INTERVAL = 5 * 1000; //5 seconds

    /**
     *Location requesting elements.
     */
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        buildLocationClient();
        requestLocationUpdates();
        startForeground(0, new Notification());
    }

    /**
     * Initializes the location client.
     */
    private void buildLocationClient() {
        mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * This method builds the location updates request.
     */
    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e(TAG, "Location Received: " + locationResult.getLastLocation().toString());
                super.onLocationResult(locationResult);
                EventBus.getDefault().post(locationResult.getLastLocation());
            }

        };
        Log.e(TAG, "Requested location updates");
        mFusedLocationProviderClient.requestLocationUpdates(LocationRequest.create(),
                mLocationCallback, Looper.myLooper()).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        //Remove location updates.
        Log.e(TAG, "Removed location updates");
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }
}