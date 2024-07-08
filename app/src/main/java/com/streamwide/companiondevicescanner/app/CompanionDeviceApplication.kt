package com.streamwide.companiondevicescanner.app

import android.app.Application
import com.streamwide.companiondevicescanner.environment.STWHardwareEnvironment

class CompanionDeviceApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        STWHardwareEnvironment.prepareEnvironment(this)
    }
}