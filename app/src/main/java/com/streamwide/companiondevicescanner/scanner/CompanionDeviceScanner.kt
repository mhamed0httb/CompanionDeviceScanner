package com.streamwide.companiondevicescanner.scanner

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanFilter
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothLeDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@RequiresApi(Build.VERSION_CODES.O)
class CompanionDeviceScanner private constructor(private val deviceManager: CompanionDeviceManager) {

    fun scan(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        deviceAddress: String? = null,
    ): Flow<STWCompanionDeviceScanState> {
        Log.d(CLASS_NAME, "start scan")

        val bluetoothFilter = BluetoothLeDeviceFilter.Builder().build()

        val pairingRequest: AssociationRequest = deviceAddress?.let { address ->
            val scanFilter = ScanFilter.Builder().setDeviceAddress(address).build()
            val deviceFilter: BluetoothLeDeviceFilter = BluetoothLeDeviceFilter.Builder()
                .setScanFilter(scanFilter)
                .build()

            AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .addDeviceFilter(bluetoothFilter)
                .setSingleDevice(true)
                .build()
        } ?: kotlin.run {
            AssociationRequest.Builder().addDeviceFilter(bluetoothFilter).setSingleDevice(false)
                .build()
        }

        return startScan(pairingRequest, launcher)
    }


    private fun startScan(
        pairingRequest: AssociationRequest,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) = callbackFlow {
        trySend(STWCompanionDeviceScanState.Scanning)
        deviceManager.associate(
            pairingRequest,
            object : CompanionDeviceManager.Callback() {

                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    Log.d(CLASS_NAME, "startScan::onDeviceFound")
                    launcher.launch(IntentSenderRequest.Builder(chooserLauncher).build())
                }

                override fun onAssociationPending(intentSender: IntentSender) {
                    Log.d(CLASS_NAME, "startScan::onAssociationPending")
                    launcher.launch(IntentSenderRequest.Builder(intentSender).build())
                }

                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    Log.d(
                        CLASS_NAME,
                        "startScan::onAssociationCreated associationInfo = $associationInfo"
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val deviceFound: BluetoothDevice? =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                associationInfo.associatedDevice?.bleDevice?.device
                            } else {
                                null
                            }
                        Log.d(
                            CLASS_NAME,
                            "startScan::onAssociationCreated deviceFound = $deviceFound"
                        )
                        trySend(STWCompanionDeviceScanState.CompanionDeviceFound(deviceFound))
                    }

                }

                override fun onFailure(error: CharSequence?) {
                    Log.e(CLASS_NAME, "startScan::onFailure error = $error")
                    trySend(
                        STWCompanionDeviceScanState.ScanCompanionFailed(
                            STWCompanionDeviceScanError.SCAN_CANCELED
                        )
                    )
                }

            }, null
        )
        awaitClose()
    }

    companion object {

        @Volatile
        private var instance: CompanionDeviceScanner? = null

        @JvmStatic
        fun instance(deviceManager: CompanionDeviceManager): CompanionDeviceScanner {
            if (instance == null) {
                synchronized(CompanionDeviceScanner::class.java) {
                    instance = CompanionDeviceScanner(deviceManager)
                }
            }
            return instance!!
        }

        private const val CLASS_NAME = "CompanionDeviceScanner"

    }
}