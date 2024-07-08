package com.streamwide.companiondevicescanner.dependencymanager

import android.bluetooth.BluetoothAdapter
import android.companion.CompanionDeviceManager
import android.content.Context
import com.streamwide.companiondevicescanner.manager.BleDeviceManager
import com.streamwide.companiondevicescanner.scanner.CompanionDeviceScanner

interface DependencyProvider {

    val applicationContext: Context

    fun provideBleDeviceManager(): BleDeviceManager
    fun provideBluetoothAdapter(): BluetoothAdapter?
    fun provideCompanionDeviceScanner(deviceManager: CompanionDeviceManager): CompanionDeviceScanner
}