package com.demo.takehometest.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * JourneyDatabase class for Room.
 */

@Database(entities = {Journey.class, LocationPoint.class}, version = 1)
public abstract class JourneyDatabase extends RoomDatabase {

    public abstract JourneyDao dao();
}