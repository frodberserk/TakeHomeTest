package com.demo.takehometest.listener;

import android.location.Location;

/**
 * Interface class used to listen to location updates.
 */

public interface LocationUpdateListener {

    /**
     * Called when location is received.
     *
     * @param location The location parameter received.
     */
    void onLocationReceived(Location location);
}