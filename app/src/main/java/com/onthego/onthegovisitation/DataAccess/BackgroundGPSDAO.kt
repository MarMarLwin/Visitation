package com.onthego.onthegovisitation.DataAccess

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onthego.onthegovisitation.Models.BackgroundGPS

@Dao
interface BackgroundGPSDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(gpsPoint:BackgroundGPS)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(gpsPointList:List<BackgroundGPS>)

    @Query("DELETE FROM BackgroundGPS")
    fun deleteAll()

    @Query("SELECT * FROM BackgroundGPS ORDER BY `CapturedDateTime` Limit 20")
    fun getAllGPS(): List<BackgroundGPS>

    @Query("SELECT * FROM BackgroundGPS ORDER BY `CapturedDateTime` DESC Limit 1")
    fun getLastGPS(): BackgroundGPS
}