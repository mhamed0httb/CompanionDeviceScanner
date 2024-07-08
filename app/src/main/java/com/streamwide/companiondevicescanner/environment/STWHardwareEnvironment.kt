package com.streamwide.companiondevicescanner.environment

import android.content.Context
import com.streamwide.companiondevicescanner.dependencymanager.DependencyProviderManager

object STWHardwareEnvironment {
    fun prepareEnvironment(context: Context) = DependencyProviderManager.initProvider(context)
}