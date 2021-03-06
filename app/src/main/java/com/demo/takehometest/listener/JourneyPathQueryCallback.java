package com.demo.takehometest.listener;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Interface class used for sending journey path data back when retrieved from database.
 */

public interface JourneyPathQueryCallback {

    /**
     * Called when query is finished and need to send back data.
     *
     * @param points List containing journey points.
     */
    void onQuerySuccessful(List<LatLng> points);
}