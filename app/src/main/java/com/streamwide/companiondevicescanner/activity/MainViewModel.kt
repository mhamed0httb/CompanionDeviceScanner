package com.streamwide.companiondevicescanner.activity

import android.companion.CompanionDeviceManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamwide.companiondevicescanner.dependencymanager.DependencyProviderManager
import com.streamwide.companiondevicescanner.scanner.STWCompanionDeviceScanState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainViewModel : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    fun scanBleDevices(
        deviceManager: CompanionDeviceManager,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        deviceAddress: String? = null,
    ) = DependencyProviderManager.getDependencyProvider().provideBleDeviceManager()
        .scanBleDevices(deviceManager, launcher, deviceAddress)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            STWCompanionDeviceScanState.Idle
        )
}