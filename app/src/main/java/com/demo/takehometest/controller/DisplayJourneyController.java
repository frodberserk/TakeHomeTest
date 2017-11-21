package com.demo.takehometest.controller;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import com.commonsware.cwac.saferoom.SafeHelperFactory;
import com.demo.takehometest.database.JourneyDatabase;
import com.demo.takehometest.listener.JourneyPathQueryCallback;
import com.demo.takehometest.model.LocationPoint;
import com.demo.takehometest.util.PreferencesUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for @{@link com.demo.takehometest.view.activity.DisplayJourneyActivity}.
 */

public class DisplayJourneyController {

    /**
     * Context of activity.
     */
    private Context context;

    /**
     * Database object for querying data.
     */
    private JourneyDatabase database;

    public DisplayJourneyController(Context context) {
        //Build database object.
        database = Room.databaseBuilder(context, JourneyDatabase.class,
                JourneyDatabase.DATABASE_NAME)
                .openHelperFactory(new SafeHelperFactory(
                        new PreferencesUtil(context).getKeySafeRoom()))
                .build();
    }

    /**
     * Fetch journey points in background thread and returns data in callback.
     *
     * @param journeyId Id of journey.
     * @param callback  Callback to return data.
     */
    public void fetchJourneyPath(long journeyId, final JourneyPathQueryCallback callback) {
        new QueryJourneyPathData(journeyId, callback).execute();
    }

    /**
     * Async task class used to fetch data in background.
     */
    class QueryJourneyPathData extends AsyncTask<Void, Void, List<LatLng>> {
        long journeyId;
        JourneyPathQueryCallback callback;

        QueryJourneyPathData(long journeyId, JourneyPathQueryCallback callback) {
            this.journeyId = journeyId;
            this.callback = callback;
        }

        protected List<LatLng> doInBackground(Void... v) {
            List<LocationPoint> locationPointList = database.dao().loadPointsOfJourney(journeyId);
            List<LatLng> latLngList = new ArrayList<>();
            for (int i = 0; i < locationPointList.size(); i++) {
                latLngList.add(new LatLng(locationPointList.get(i).getLatitude(),
                        locationPointList.get(i).getLongitude()));
            }
            return latLngList;
        }

        protected void onPostExecute(List<LatLng> result) {
            if (callback != null) {
                callback.onQuerySuccessful(result);
            }
        }
    }
}