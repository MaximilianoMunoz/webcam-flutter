package com.example.usb_camera_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.usb_camera_app/usb"
    private val CAMERA_PERMISSION_CODE = 1001
    private var cameraConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Registra los receptores para conexiones y desconexiones de USB
        registerReceiver(usbReceiver, IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED))
        registerReceiver(usbReceiver, IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))

        // Verifica el estado inicial de los dispositivos USB conectados
        checkInitialUsbDevices()
    }

    private fun checkInitialUsbDevices() {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        val devices = deviceList.values
        cameraConnected = devices.any { isCameraDevice(it) }
        
        // Actualiza la interfaz de usuario en Flutter
        val engine = flutterEngine ?: return
        val channel = MethodChannel(engine.dartExecutor.binaryMessenger, CHANNEL)
        channel.invokeMethod("updateCameraStatus", cameraConnected)
    }

    private fun isCameraDevice(device: UsbDevice): Boolean {
        // Aquí puedes adaptar la lógica para identificar si el dispositivo es una cámara.
        // Esto es un ejemplo simple que verifica si el nombre del producto contiene "Camera".
        return device.productName?.contains("Camera", ignoreCase = true) == true
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        val methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)

        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "checkCameraStatus" -> {
                    result.success(cameraConnected)
                }
                "requestCameraPermission" -> {
                    requestCameraPermission()
                    result.success("Permission requested")
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE)
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (device != null) {
                        cameraConnected = true
                        Toast.makeText(context, "USB Device Attached: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                    }
                    // Actualiza la interfaz de usuario en Flutter
                    val engine = flutterEngine ?: return
                    val channel = MethodChannel(engine.dartExecutor.binaryMessenger, CHANNEL)
                    channel.invokeMethod("updateCameraStatus", cameraConnected)
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (device != null) {
                        cameraConnected = false
                        Toast.makeText(context, "USB Device Detached: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                    }
                    // Actualiza la interfaz de usuario en Flutter
                    val engine = flutterEngine ?: return
                    val channel = MethodChannel(engine.dartExecutor.binaryMessenger, CHANNEL)
                    channel.invokeMethod("updateCameraStatus", cameraConnected)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                // Permiso denegado
                Toast.makeText(this, "Camera permission is required to use the USB camera", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
