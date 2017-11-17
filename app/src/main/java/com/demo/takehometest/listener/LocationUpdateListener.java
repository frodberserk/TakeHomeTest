package com.demo.takehometest.listener;

import android.location.Location;

/**
 * Created by frodberserk on 11/16/2017.
 */

public interface LocationUpdateListener {

    void onLocationReceived(Location location);
}