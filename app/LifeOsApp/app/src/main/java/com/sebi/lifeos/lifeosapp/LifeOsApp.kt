package com.sebi.lifeos.lifeosapp

import android.app.Application
import com.sebi.lifeos.lifeosapp.util.AppPrefs
import com.sebi.lifeos.lifeosapp.worker.DailyUsageScheduler

class LifeOsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DailyUsageScheduler.scheduleNext(this)
        DailyUsageScheduler.setRealtimeEnabled(this, AppPrefs.isRealtimeTrackingEnabled(this))
    }
}
