package com.gustavobarreto.instafollowtracker

import android.app.Application
import com.gustavobarreto.instafollowtracker.data.db.AppDatabase
import com.gustavobarreto.instafollowtracker.data.repository.FollowersRepository

class InstaTrackerApplication : Application() {

    lateinit var repository: FollowersRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = FollowersRepository(database)
    }
}
