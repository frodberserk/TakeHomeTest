package com.demo.takehometest.listener;

import com.demo.takehometest.database.Journey;

import java.util.List;

/**
 * Interface class used for sending journey data back when retrieved from database.
 */

public interface FetchJourneyCallback {

    /**
     * Called when query is finished and need to send back data.
     *
     * @param journeys List containing journey data.
     */
    void onQuerySuccessful(List<Journey> journeys);
}