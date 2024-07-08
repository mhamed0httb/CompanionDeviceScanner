package com.streamwide.companiondevicescanner.dependencymanager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.companion.CompanionDeviceManager
import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.streamwide.companiondevicescanner.manager.BleDeviceManager
import com.streamwide.companiondevicescanner.scanner.CompanionDeviceScanner

class BleDependencyProvider private constructor(context: Context) : DependencyProvider {

    private val deviceManager: BleDeviceManager by lazy {
        BleDeviceManager.instance(this)
    }

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager?.adapter
    }

    private val locationManager: LocationManager? by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    override val applicationContext: Context = context
    override fun provideBleDeviceManager(): BleDeviceManager = deviceManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun provideCompanionDeviceScanner(deviceManager: CompanionDeviceManager): CompanionDeviceScanner =
        CompanionDeviceScanner.instance(deviceManager)


    override fun provideBluetoothAdapter(): BluetoothAdapter? = bluetoothAdapter

    companion object {
        @Volatile
        private var instance: BleDependencyProvider? = null

        @JvmStatic
        fun instance(context: Context): BleDependencyProvider {
            if (instance == null) {
                synchronized(BleDependencyProvider::class.java) {
                    instance = BleDependencyProvider(context)
                }
            }
            return instance!!
        }
    }
}