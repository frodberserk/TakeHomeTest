package com.demo.takehometest.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * JourneyDatabase class for Room.
 */

@Database(entities = {Journey.class, LocationPoint.class}, version = 1)
public abstract class JourneyDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "db_journey";

    public abstract JourneyDao dao();
}