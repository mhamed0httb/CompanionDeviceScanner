package com.streamwide.companiondevicescanner.dependencymanager

import android.content.Context

object DependencyProviderManager {

    private lateinit var dependencyProvider: DependencyProvider

    fun initProvider(context: Context) {
        dependencyProvider = BleDependencyProvider.instance(context)
    }

    fun getDependencyProvider(): DependencyProvider = try {
        dependencyProvider
    } catch (e: UninitializedPropertyAccessException) {
        throw Exception("Hardware must be initialized, call STWHardwareEnvironment.initProvider in Application's onCreate.")
    }
}