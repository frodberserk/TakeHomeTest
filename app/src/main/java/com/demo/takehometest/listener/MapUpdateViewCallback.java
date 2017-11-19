package com.demo.takehometest.listener;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Interface class used to update Map UI from controller.
 */

public interface MapUpdateViewCallback {

    /**
     * Called when controller needs to update UI.
     *
     * @param data List of journey points.
     */
    void updateView(ArrayList<LatLng> data);
}