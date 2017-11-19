package com.demo.takehometest.listener;

import com.demo.takehometest.model.Journey;

import java.util.List;

/**
 * Interface class used for sending journey data back when retrieved from database.
 */

public interface JourneyQueryCallback {

    /**
     * Called when query is finished and need to send back data.
     *
     * @param journeys List containing journey data.
     */
    void onQuerySuccessful(List<Journey> journeys);
}