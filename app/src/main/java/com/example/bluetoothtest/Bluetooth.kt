package com.example.bluetoothtest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions.RxPermissions

/**
 * Created by NO on 2018/7/24.
 */
class Bluetooth constructor(
    private val mActivity: AppCompatActivity, private val mBluetoothAdapter: BluetoothAdapter
) {

    var mListener: ((String, String) -> Unit)? = null

    fun doDiscovery() {
        registerBroadcast()
        val rxPermissions = RxPermissions(mActivity)
        rxPermissions.request(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).subscribe { aBoolean ->
            if (aBoolean) {
                if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()
                mBluetoothAdapter.startDiscovery()
            } else {
                Toast.makeText(
                    mActivity.applicationContext,
                    "no bluetooth permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun registerBroadcast() {
        val intent = IntentFilter()
        intent.addAction(BluetoothDevice.ACTION_FOUND) // 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        mActivity.registerReceiver(mReceiver, intent)
    }

    fun disReceiver() {
        mActivity.unregisterReceiver(mReceiver)
        if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            val device: BluetoothDevice?
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && device.bluetoothClass.majorDeviceClass == 1536) {
                        mListener?.apply {
                            this(
                                if (TextUtils.isEmpty(device.name)) "UnKnown" else device.name,
                                device.address
                            )
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        when (device.bondState) {
                            BluetoothDevice.BOND_BONDING -> Log.d("Print", "正在配对......")
                            BluetoothDevice.BOND_BONDED -> Log.d("Print", "完成配对")
                            BluetoothDevice.BOND_NONE -> Log.d("Print", "取消配对")
                            else -> {
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> Log.d("Print", "搜索完成")
            }
        }
    }

}















