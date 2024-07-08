package com.streamwide.companiondevicescanner.manager

import android.bluetooth.BluetoothAdapter
import android.companion.CompanionDeviceManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import com.streamwide.companiondevicescanner.dependencymanager.DependencyProvider
import com.streamwide.companiondevicescanner.scanner.STWCompanionDeviceScanError
import com.streamwide.companiondevicescanner.scanner.STWCompanionDeviceScanState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BleDeviceManager private constructor(private val dependencyProvider: DependencyProvider) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun scanBleDevices(
        deviceManager: CompanionDeviceManager,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        deviceAddress: String? = null,
    ): Flow<STWCompanionDeviceScanState> {
        if (!hasFeatureCompanionSetup()) return flow {
            emit(
                STWCompanionDeviceScanState.ScanCompanionFailed(
                    STWCompanionDeviceScanError.COMPANION_API_NOT_SUPPORTED
                )
            )
        }
        if (!isBleSupported()) return flow {
            emit(
                STWCompanionDeviceScanState.ScanCompanionFailed(
                    STWCompanionDeviceScanError.BLE_NOT_SUPPORTED
                )
            )
        }
        if (!isBluetoothActive()) return flow {
            emit(
                STWCompanionDeviceScanState.ScanCompanionFailed(
                    STWCompanionDeviceScanError.BLUETOOTH_NOT_ENABLED
                )
            )
        }

        return dependencyProvider.provideCompanionDeviceScanner(deviceManager)
            .scan(launcher, deviceAddress)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hasFeatureCompanionSetup(): Boolean {
        return dependencyProvider.applicationContext.packageManager.hasSystemFeature(
            PackageManager.FEATURE_COMPANION_DEVICE_SETUP
        )
    }

    fun isBleSupported(): Boolean {
        return if (dependencyProvider.applicationContext.packageManager.hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE
            )
        ) {
            Log.d(
                CLASS_NAME,
                " isBLESupported : system has Bluetooth low energy Feature"
            )
            true
        } else {
            Log.d(
                CLASS_NAME,
                "isBLESupported : Bluetooth low energy Feature not supported"
            )
            false
        }
    }

    fun isBluetoothActive(): Boolean =
        dependencyProvider.provideBluetoothAdapter()?.let {
            it.isEnabled && it.state == BluetoothAdapter.STATE_ON
        } ?: kotlin.run { false }

    companion object {
        @Volatile
        private var instance: BleDeviceManager? = null

        @JvmStatic
        fun instance(dependencyProvider: DependencyProvider): BleDeviceManager {
            if (instance == null) {
                synchronized(BleDeviceManager::class.java) {
                    instance = BleDeviceManager(dependencyProvider)
                }
            }
            return instance!!
        }

        private const val CLASS_NAME = "BleDeviceManager"

        private const val MINUS_SEPARATOR = " - "
    }
}