package com.demo.takehometest.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Model class for a journey. One of entity of Room database.
 * This entity represents the Journey table in database and will be storing each journey.
 */

@Entity
public class Journey {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long startTime;
    private long endTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}