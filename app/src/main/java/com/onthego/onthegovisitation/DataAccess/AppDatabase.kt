package com.onthego.onthegovisitation.DataAccess

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.onthego.onthegovisitation.Models.BackgroundGPS

@Database(entities = [BackgroundGPS::class],version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun backgroundGPSDAO(): BackgroundGPSDAO
    companion object
    {
        var INSTANCE : AppDatabase? = null
        fun getAppDatabase(context : Context) : AppDatabase?
        {
            if (INSTANCE == null)
            {
                synchronized(AppDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                            context.applicationContext, AppDatabase::class.java,
                            context.applicationInfo.dataDir + "//databases//OnTheGoVisitation.db"
                    )
                            .allowMainThreadQueries().fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE
        }
    }
}