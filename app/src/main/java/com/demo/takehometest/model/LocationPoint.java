package com.demo.takehometest.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

/**
 * Model class representing a point in journey. One of entity of Room database.
 * This entity represents the LocationPoint table in database and will be storing location points
 * travelled.
 * <p>
 * All points of a particular journey will be associated with its id.
 */

@Entity
public class LocationPoint {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ForeignKey(entity = Journey.class, parentColumns = "id", childColumns = "journeyId")
    private long journeyId;

    private double latitude;
    private double longitude;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(long journeyId) {
        this.journeyId = journeyId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}