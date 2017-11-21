package com.demo.takehometest.controller;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.demo.takehometest.database.JourneyDatabase;
import com.demo.takehometest.listener.JourneyItemClickListener;
import com.demo.takehometest.listener.JourneyQueryCallback;
import com.demo.takehometest.model.Journey;
import com.demo.takehometest.view.activity.DisplayJourneyActivity;

import java.util.List;

/**
 * Controller class for @{@link com.demo.takehometest.view.activity.JourneyListActivity}.
 */

public class JourneyListController implements JourneyItemClickListener {

    /**
     * Context of activity.
     */
    private Context context;

    /**
     * Database object for querying data.
     */
    private JourneyDatabase database;

    public JourneyListController(Context context) {
        this.context = context;
        //Build database object.
        database = Room.databaseBuilder(context, JourneyDatabase.class,
                JourneyDatabase.DATABASE_NAME)
                .build();
    }

    /**
     * Fetches list of journeys in background and returns data in callback.
     *
     * @param callback Callback for returning data.
     */
    public void fetchJourneyData(final JourneyQueryCallback callback) {
        new QueryJourneyData(callback).execute();
    }

    /**
     * Called when journey item is clicked.
     *
     * @param position Position of clicked item.
     * @param journey  Journey object for clicked item.
     */
    @Override
    public void onItemClicked(int position, Journey journey) {
        //Open journey path screen
        context.startActivity(new Intent(context, DisplayJourneyActivity.class)
                .putExtra(DisplayJourneyActivity.EXTRA_JOURNEY_ID, journey.getId()));
    }

    /**
     * Async task class used to fetch data in background.
     */
    class QueryJourneyData extends AsyncTask<Void, Void, List<Journey>> {
        JourneyQueryCallback callback;

        QueryJourneyData(JourneyQueryCallback callback) {
            this.callback = callback;
        }

        protected List<Journey> doInBackground(Void... v) {
            //Query database
            return database.dao().loadAllJourneys();
        }

        protected void onPostExecute(List<Journey> result) {
            //Return data in callback
            if (callback != null) {
                callback.onQuerySuccessful(result);
            }
        }
    }
}