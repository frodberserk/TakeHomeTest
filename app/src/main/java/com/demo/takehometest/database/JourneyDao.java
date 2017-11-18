package com.demo.takehometest.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * DAO class for Room.
 * It is used to write all queries and save/retrieve data.
 */

@Dao
public interface JourneyDao {

    /**
     * Returns all journeys saved.
     *
     * @return List containing all journeys.
     */
    @Query("SELECT * FROM Journey")
    public List<Journey> loadAllJourneys();

    /**
     * Returns all location points associated with a journey.
     *
     * @param journeyId Id of journey.
     * @return List containing all location points.
     */
    @Query("SELECT * from LocationPoint where journeyId = :journeyId")
    public List<LocationPoint> loadPointsOfJourney(int journeyId);

    /**
     * Insert a journey into database.
     *
     * @param journey @{@link Journey} object to be inserted.
     * @return id of the row created for the journey.
     */
    @Insert
    public long addJourney(Journey journey);

    /**
     * Adds a location point into database.
     *
     * @param locationPoint @{@link LocationPoint} object to be inserted.
     */
    @Insert
    public void addLocationPoint(LocationPoint locationPoint);

    /**
     * Updates a journey.
     *
     * @param journey @{@link Journey} object need to be updated.
     */
    @Update
    public void updateJourney(Journey journey);

}