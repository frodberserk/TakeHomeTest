package com.demo.takehometest.listener;

import com.demo.takehometest.model.LocationPoint;

import java.util.List;

/**
 * Interface class used to request current journey data
 * from @{@link com.demo.takehometest.service.LocationUpdatesService}.
 */
public interface RequestJourneyCallback {

    /**
     * Runs when request is complete and sends data back.
     *
     * @param data List of journey location points.
     */
    void onRequestComplete(List<LocationPoint> data);
}