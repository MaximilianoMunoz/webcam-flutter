package com.example.usb_camera_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.TextView
import android.widget.Toast

class UsbReceiver(private val usbStatusTextView: TextView) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                usbStatusTextView.text = "Dispositivo USB conectado"
                Toast.makeText(context, "Dispositivo USB conectado", Toast.LENGTH_SHORT).show()
            }
            android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                usbStatusTextView.text = "Dispositivo USB desconectado"
                Toast.makeText(context, "Dispositivo USB desconectado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
