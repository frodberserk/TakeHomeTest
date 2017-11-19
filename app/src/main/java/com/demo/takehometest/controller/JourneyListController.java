package com.demo.takehometest.controller;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import com.demo.takehometest.database.JourneyDatabase;
import com.demo.takehometest.listener.JourneyQueryCallback;
import com.demo.takehometest.model.Journey;

import java.util.List;

/**
 * Controller class for @{@link com.demo.takehometest.view.activity.JourneyListActivity}.
 */

public class JourneyListController {

    /**
     * Context of activity.
     */
    private Context context;

    /**
     * Database object for querying data.
     */
    private JourneyDatabase database;

    public JourneyListController(Context context) {
        //Build database object.
        database = Room.databaseBuilder(context, JourneyDatabase.class,
                JourneyDatabase.DATABASE_NAME)
                .build();
    }

    public void fetchJourneyData(final JourneyQueryCallback callback) {
        new QueryJourneyData(callback).execute();
    }

    class QueryJourneyData extends AsyncTask<Void, Void, List<Journey>> {
        JourneyQueryCallback callback;

        QueryJourneyData(JourneyQueryCallback callback) {
            this.callback = callback;
        }

        protected List<Journey> doInBackground(Void... v) {
            return database.dao().loadAllJourneys();
        }

        protected void onPostExecute(List<Journey> result) {
            callback.onQuerySuccessful(result);
        }
    }
}