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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// instantiating the interface as a DAO to communicate with sql
@Dao
interface SleepDatabaseDao {
    // create an entity in the db
    @Insert
    fun insert(night: SleepNight)

    // update an entity in the db
    @Update
    fun update(night: SleepNight)

    // read a single entity from the db, may be null if id doesn't exist
    @Query("SELECT * FROM daily_sleep_quality_tracker WHERE nightId = :key")
    fun get(key: Long): SleepNight?

    // destroy all entities from the db
    @Query("DELETE FROM daily_sleep_quality_tracker")
    fun clear()

    // read all entities from the db, return a immutable live list of nights
    @Query("SELECT * FROM daily_sleep_quality_tracker ORDER BY nightId DESC")
    fun getAllNights(): LiveData<List<SleepNight>>

    // read most recent entity from db
    @Query("SELECT * FROM daily_sleep_quality_tracker ORDER BY nightId DESC LIMIT 1")
    fun getTonight(): SleepNight?
}
