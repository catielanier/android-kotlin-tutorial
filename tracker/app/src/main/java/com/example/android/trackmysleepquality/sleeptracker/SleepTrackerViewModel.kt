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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    //coroutine
    private var viewModelJob = Job()

    // cancel all coroutines
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    // UI thread
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // hold current night
    private var tonight = MutableLiveData<SleepNight?>()

    // get all nights from dao
    private val nights = database.getAllNights()

    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    val startButtonVisible = Transformations.map(tonight) {
        null == it
    }

    val stopButtonVisible = Transformations.map(tonight) {
        null != it
    }

    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    init {
        // get current night on init
        initializeTonight()
    }

    private fun initializeTonight() {
        // launch a coroutine without blocking the current thread
        uiScope.launch {
            // run function to get the current night from the DB
            tonight.value = getTonightFromDatabase()
        }
    }

    // call suspend function to run from within coroutine and not block
    private suspend fun getTonightFromDatabase(): SleepNight? {
        //run from IO thread
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }

    // function to start tracking a night of sleep
    fun onStartTracking() {
        // start function on UI thread because this will ultimately update the UI
        uiScope.launch {
            // create new instance of SleepNight db model
            val newNight = SleepNight()

            // use dao to insert
            insert(newNight)

            // change tonight to newly created night
            tonight.value = getTonightFromDatabase()
        }
    }

    // function to add nights to db
    private suspend fun insert(night: SleepNight) {
        // run from IO thread
        withContext(Dispatchers.IO) {
            // use dao to insert into db
            database.insert(night)
        }
    }

    // function to stop tracking a night of sleep
    fun onStopTracking() {
        // start function on UI thread because this will ultimately update the UI
        uiScope.launch {
            // grab current value of tonight,
            val oldNight = tonight.value ?: return@launch

            // assign current time to end time
            oldNight.endTimeMilli = System.currentTimeMillis()

            // update the db
            update(oldNight)

            // set item to navigate to
            _navigateToSleepQuality.value = oldNight
        }
    }

    // function to update night
    private suspend fun update(night: SleepNight) {
        // run from IO thread
        withContext(Dispatchers.IO) {
            // use dao to update entity in db
            database.update(night)
        }
    }

    // function to clear all entries
    fun onClear() {
        // start function on UI thread because this will ultimately update the UI
        uiScope.launch {
            clear()
            tonight.value = null
            _showSnackbarEvent.value = true
        }
    }

    // IO function to clear entities
    private suspend fun clear() {
        // run from IO thread
        withContext(Dispatchers.IO) {
            // clear the db
            database.clear()
        }
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }
}

