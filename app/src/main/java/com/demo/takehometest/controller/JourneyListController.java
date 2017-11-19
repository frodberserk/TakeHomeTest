package com.demo.takehometest.controller;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.demo.takehometest.database.JourneyDatabase;
import com.demo.takehometest.listener.FetchJourneyCallback;

/**
 * Controller class for @{@link com.demo.takehometest.view.activity.JourneyListActivity}.
 */

public class JourneyListController {

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

    public void fetchJourneyData(final FetchJourneyCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.onQuerySuccessful(database.dao().loadAllJourneys());
            }
        }).start();
    }

}