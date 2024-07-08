package com.streamwide.companiondevicescanner.scanner

import android.bluetooth.BluetoothDevice

sealed interface STWCompanionDeviceScanState {
    /**
     * Indicates the initial state before any BLE operations have been performed.
     */
    object Idle : STWCompanionDeviceScanState

    /**
     * Indicates that a BLE scan is currently in progress.
     */
    object Scanning : STWCompanionDeviceScanState

    /**
     * Represents a failed BLE scan attempt.
     *
     * @property reason The specific error encountered during the scan process.
     */
    data class ScanCompanionFailed(val reason: STWCompanionDeviceScanError) :
        STWCompanionDeviceScanState

    /**
     * Represents a BLE device that has been discovered during a scan.
     *
     * @property device The discovered BluetoothDevice object, which may be null if device information
     *                 is unavailable. You can use this object to access details like device address
     *                 and name (if available).
     */
    data class CompanionDeviceFound(val device: BluetoothDevice?) : STWCompanionDeviceScanState

}