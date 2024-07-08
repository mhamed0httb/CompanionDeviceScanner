package com.streamwide.companiondevicescanner.scanner

enum class STWCompanionDeviceScanError {
    /**
     * The scan operation was canceled by the user or application.
     */
    SCAN_CANCELED,

    /**
     * Bluetooth is not enabled on the device. Scanning requires Bluetooth to be turned on.
     */
    BLUETOOTH_NOT_ENABLED,

    /**
     * The device does not support Bluetooth Low Energy (BLE).
     */
    BLE_NOT_SUPPORTED,

    /**
     * The device does not support Companion Setup feature.
     */
    COMPANION_API_NOT_SUPPORTED,
}