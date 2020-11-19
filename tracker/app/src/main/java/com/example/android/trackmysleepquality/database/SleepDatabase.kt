/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// instantiates the database, entities consume a list of tables
@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {
    // calls upon the DAO for CRUD operations
    abstract val sleepDatabaseDao: SleepDatabaseDao

    // create a companion object to call methods and fields without instantiating the class
    companion object {
        // annotate with volatile to make writes to this field immediately visible to other threads
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        // return a reference to the db, requires a context
        fun getInstance(context: Context) : SleepDatabase {
            // create a synchronized lock to only allow single threads from accessing, allows the instance to be init once
            synchronized(this) {
                // get current value of instance (mutable)
                var instance = INSTANCE

                // check if there's already a database, if not, create it.
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                    "sleep_history_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build()
                    // assign INSTANCE to created db
                    INSTANCE = instance
                }

                // return the database
                return instance
            }
        }
    }
}