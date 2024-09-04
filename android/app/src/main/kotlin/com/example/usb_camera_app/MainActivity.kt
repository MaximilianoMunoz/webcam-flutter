package com.example.usb_camera_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.usb.*
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.nio.ByteBuffer

class MainActivity : FlutterActivity() {
    private val CHANNEL = "usb_camera_channel"
    private lateinit var usbManager: UsbManager
    private var usbDevice: UsbDevice? = null
    private var usbDeviceConnection: UsbDeviceConnection? = null
    private var mediaCodec: MediaCodec? = null
    private var surface: Surface? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "checkPermissions" -> checkPermissions(result)
                "connectUsbCamera" -> connectUsbCamera(result)
                else -> result.notImplemented()
            }
        }
    }

    private fun checkPermissions(result: MethodChannel.Result) {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            result.success(true)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            result.success(false)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Permission granted")
        } else {
            Log.d("MainActivity", "Permission denied")
        }
    }

    private fun connectUsbCamera(result: MethodChannel.Result) {
        usbManager.deviceList.values.forEach { device ->
            if (isCameraDevice(device)) {
                usbDevice = device
                usbDeviceConnection = usbManager.openDevice(device)
                // Initialize MediaCodec to handle video stream
                setupMediaCodec()
                result.success("USB Camera Connected")
                return
            }
        }
        result.success("No USB Camera Found")
    }

    private fun isCameraDevice(device: UsbDevice): Boolean {
        // Replace with actual camera detection logic
        return device.deviceClass == UsbConstants.USB_CLASS_VIDEO
    }

    private fun setupMediaCodec() {
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc") // or other codec types
            val format = MediaFormat.createVideoFormat("video/avc", 640, 480)
            mediaCodec?.configure(format, surface, null, 0)
            mediaCodec?.start()
            Log.d("MainActivity", "MediaCodec setup complete")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to setup MediaCodec", e)
        }
    }

    private fun startVideoStreaming() {
        // Implement video streaming logic here
        // Capture frames from the USB camera, decode them with MediaCodec, and display them on the Surface
        // Example code for capturing and processing frames
        mediaCodec?.let {
            val bufferInfo = MediaCodec.BufferInfo()
            while (true) {
                val inputBufferIndex = it.dequeueInputBuffer(-1)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = it.getInputBuffer(inputBufferIndex)
                    // Fill inputBuffer with camera frame data here
                    it.queueInputBuffer(inputBufferIndex, 0, inputBuffer?.limit() ?: 0, System.nanoTime(), 0)
                }
                val outputBufferIndex = it.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferIndex >= 0) {
                    it.releaseOutputBuffer(outputBufferIndex, true)
                }
            }
        }
    }
}
