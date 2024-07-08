package com.streamwide.companiondevicescanner.activity

import android.Manifest
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.streamwide.companiondevicescanner.databinding.ActivityMainBinding
import com.streamwide.companiondevicescanner.scanner.STWCompanionDeviceScanError
import com.streamwide.companiondevicescanner.scanner.STWCompanionDeviceScanState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val companionDeviceManager: CompanionDeviceManager
        @RequiresApi(Build.VERSION_CODES.O)
        get() = getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

    @RequiresApi(api = Build.VERSION_CODES.O)
    private val companionScanLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->

            result.data?.takeIf { result.resultCode == RESULT_OK }?.let { data ->

                val scanDevice =
                    data.parcelableExtra<android.bluetooth.le.ScanResult>(CompanionDeviceManager.EXTRA_DEVICE)?.device
                Log.d(CLASS_NAME, "scanDevice = $scanDevice")
                val deviceName = if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    scanDevice?.name ?: scanDevice?.address
                } else {
                    scanDevice?.address
                }
                updateDeviceNameText(deviceName)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initActions()
    }

    private fun initActions() {
        binding.btnScabBleDevices.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.CREATED) {
                        viewModel.scanBleDevices(
                            companionDeviceManager,
                            companionScanLauncher
                        ).collect { scanState ->
                            updateLoader(scanState is STWCompanionDeviceScanState.Scanning)
                            when (scanState) {
                                is STWCompanionDeviceScanState.CompanionDeviceFound -> {
                                    val deviceName = if (ActivityCompat.checkSelfPermission(
                                            this@MainActivity,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        scanState.device?.name ?: scanState.device?.address
                                    } else {
                                        scanState.device?.address
                                    }
                                    updateDeviceNameText(deviceName)
                                }


                                is STWCompanionDeviceScanState.ScanCompanionFailed -> {
                                    when (scanState.reason) {
                                        STWCompanionDeviceScanError.SCAN_CANCELED -> updateDeviceNameText(
                                            "Scan canceled",
                                            true
                                        )

                                        STWCompanionDeviceScanError.BLUETOOTH_NOT_ENABLED -> updateDeviceNameText(
                                            "Bluetooth is not enabled",
                                            true
                                        )

                                        STWCompanionDeviceScanError.BLE_NOT_SUPPORTED -> updateDeviceNameText(
                                            "BLE is not supported",
                                            true
                                        )

                                        STWCompanionDeviceScanError.COMPANION_API_NOT_SUPPORTED -> updateDeviceNameText(
                                            "Device do not support companion scan",
                                            true
                                        )
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Device do not support Companion scanning.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateDeviceNameText(name: String?, isError: Boolean = false) {
        val builder = StringBuilder()
        if (isError) {
            binding.textCompanionDeviceName.setTextColor(Color.RED)
        } else {
            binding.textCompanionDeviceName.setTextColor(Color.BLACK)
            builder.append("Device found: ")
        }
        builder.append(name)
        binding.textCompanionDeviceName.text = builder
    }

    private fun updateLoader(isLoading: Boolean) {
        binding.loader.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private inline fun <reified T : Parcelable> Intent.parcelableExtra(key: String): T? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(
                    CompanionDeviceManager.EXTRA_DEVICE,
                    T::class.java
                )
            } else {
                getParcelableExtra(key) as? T
            }
        } catch (e: Exception) {
            Log.e(
                CLASS_NAME,
                "error = $e "
            )
            null
        }
    }

    companion object {
        private const val CLASS_NAME = "MainActivity"
    }
}