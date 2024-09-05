# Keep the Camera2 API
-keep class android.hardware.camera2.** { *; }
-keep class android.hardware.camera2.CameraDevice$StateCallback { *; }
-keep class android.hardware.camera2.CameraCaptureSession$StateCallback { *; }
-keep class android.hardware.camera2.CameraManager$CameraAccessException { *; }
-keep class android.hardware.camera2.CameraManager$CameraDeviceStateCallback { *; }
-keep class android.hardware.camera2.CameraManager$CameraManagerAvailableCallbacks { *; }
-keep class android.hardware.camera2.params.** { *; }

# Keep the UsbManager and related classes
-keep class android.hardware.usb.** { *; }