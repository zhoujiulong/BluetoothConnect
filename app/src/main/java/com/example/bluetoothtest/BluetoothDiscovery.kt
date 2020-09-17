package com.example.bluetoothtest

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * 蓝牙搜索
 */
class BluetoothDiscovery constructor(
    private val mActivity: AppCompatActivity, private val mBluetoothAdapter: BluetoothAdapter
) : LifecycleObserver {

    var mListener: ((String, String) -> Unit)? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val intent = IntentFilter()
        intent.addAction(BluetoothDevice.ACTION_FOUND) // 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        mActivity.registerReceiver(mReceiver, intent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disReceiver()
        mActivity.unregisterReceiver(mReceiver)
    }

    fun doDiscovery() {
        if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()
        mBluetoothAdapter.startDiscovery()
    }

    fun disReceiver() {
        if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            val device: BluetoothDevice?
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
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
                            BluetoothDevice.BOND_BONDING -> {
                                Toast.makeText(mActivity, "正在配对......", Toast.LENGTH_SHORT).show()
                            }
                            BluetoothDevice.BOND_BONDED -> {
                                Toast.makeText(mActivity, "完成配对......", Toast.LENGTH_SHORT).show()
                            }
                            BluetoothDevice.BOND_NONE -> {
                                Toast.makeText(mActivity, "取消配对......", Toast.LENGTH_SHORT).show()
                            }
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















