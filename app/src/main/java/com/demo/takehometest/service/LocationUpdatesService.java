/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demo.takehometest.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.demo.takehometest.R;
import com.demo.takehometest.database.JourneyDatabase;
import com.demo.takehometest.listener.RequestJourneyCallback;
import com.demo.takehometest.model.Journey;
import com.demo.takehometest.model.LocationPoint;
import com.demo.takehometest.util.PreferencesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Service class used to track user's location periodically.
 * <p>
 * If the view goes to background, the service goes into foreground state.
 */
public class LocationUpdatesService extends Service {

    /**
     * Simple tag for logs.
     */
    private static final String TAG = LocationUpdatesService.class.getSimpleName();

    /**
     * Smallest distance in metres to which we will receive location updates.
     */
    private static final float SMALLEST_DISPLACEMENT = 10;


    /**
     * Interval in millis for location updates.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * Fastest interval in millis for location updates.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Notification id used for the foreground notification.
     */
    private static final int NOTIFICATION_ID = 123;


    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderClient}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;

    /**
     * Contains tracking on/off info.
     */
    private PreferencesUtil mPreferencesUtil;

    /**
     * Local binder object for this service.
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * Database object. We will use it to store journey data into database.
     */
    private JourneyDatabase journeyDatabase;
    private Journey currentJourney;

    private boolean updatesOn = false;

    @Override
    public void onCreate() {
        mPreferencesUtil = new PreferencesUtil(this);

        //Build the database object.
        journeyDatabase = Room.databaseBuilder(getApplicationContext(), JourneyDatabase.class,
                JourneyDatabase.DATABASE_NAME)
//                .openHelperFactory(new SafeHelperFactory(mPreferencesUtil.getKeySafeRoom()))
                .build();

        //Initialize location client.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Write Location updates callback.
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                addLocationToDatabase(locationResult.getLastLocation());
                sendLocationEvent(locationResult.getLastLocation());
            }
        };
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        createLocationRequest();
//        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "View unbinds");

        // Called when activity unbinds to the service. We make service foreground here.
        if (mPreferencesUtil.isTrackingOn()) {
            Log.i(TAG, "Starting foreground service");
            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Makes a request for location updates.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        mPreferencesUtil.setTracking(true);
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
            updatesOn = true;
        } catch (SecurityException exception) {
            mPreferencesUtil.setTracking(false);
            Log.e(TAG, "Permission revoked: " + exception);
        }
    }

    /**
     * Removes location updates.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mPreferencesUtil.setTracking(false);
            endJourney();
            updatesOn = false;
            stopSelf();
        } catch (SecurityException exception) {
            mPreferencesUtil.setTracking(true);
            Log.e(TAG, "Permission revoked, cannot disable: " + exception);
        }
    }

    /**
     * Returns the {@link Notification} used as part of the foreground service.
     */
    private Notification getNotification() {
        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_message))
                .setSmallIcon(R.mipmap.ic_launcher).build();
    }

    /**
     * Retrieve last known location.
     */
    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                sendLocationEvent(mLocation);
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void sendLocationEvent(Location location) {
        Log.i(TAG, "Location received: " + location);

        mLocation = location;

        EventBus.getDefault().post(location);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
    }

    /**
     * Class used for the client Binder.
     */
    public class LocalBinder extends Binder {
        public LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Update end time of journey into database.
     */
    private void endJourney() {
        //Checks if journey is not null(unlikely to happen)
        if (currentJourney != null) {
            //Background thread for Room database
            new Thread(new Runnable() {
                @Override
                public void run() {
                    currentJourney.setEndTime(System.currentTimeMillis());
                    journeyDatabase.dao().updateJourney(currentJourney);
                    currentJourney = null;
                }
            }).start();
        }
    }

    private void addLocationToDatabase(final Location location) {
        //Background thread for Room database
        new Thread(new Runnable() {
            @Override
            public void run() {
                //If starting point of journey, create a @Journey object and save to database.
                if (currentJourney == null) {
                    currentJourney = new Journey();
                    currentJourney.setStartTime(System.currentTimeMillis());
                    currentJourney.setId(journeyDatabase.dao().addJourney(currentJourney));
                }

                //Save location in database corresponding to id of current journey.
                LocationPoint locationPoint = new LocationPoint();

                locationPoint.setJourneyId(currentJourney.getId());
                locationPoint.setLatitude(location.getLatitude());
                locationPoint.setLongitude(location.getLongitude());

                journeyDatabase.dao().addLocationPoint(locationPoint);
            }
        }).start();
    }

    /**
     * Checks if location updates are on.
     *
     * @return True if on, false otherwise.
     */
    public boolean isUpdateOn() {
        return updatesOn;
    }

    /**
     * Query current journey locations from database and send back into callback.
     *
     * @param requestJourneyCallback Callback to send data back.
     */
    public void requestCurrentJourneyData(final RequestJourneyCallback requestJourneyCallback) {
        //Background thread for Room database, then return control back.
        new QueryJourneyData(requestJourneyCallback).execute();
    }

    /**
     * AsyncTask to query data on background thread.
     */
    class QueryJourneyData extends AsyncTask<Void, Void, List<LocationPoint>> {
        RequestJourneyCallback callback;

        QueryJourneyData(RequestJourneyCallback callback) {
            this.callback = callback;
        }

        protected List<LocationPoint> doInBackground(Void... v) {
            return journeyDatabase.dao().loadPointsOfJourney(currentJourney.getId());
        }

        protected void onPostExecute(List<LocationPoint> result) {
            if (callback != null) {
                callback.onRequestComplete(result);
            }
        }
    }
}