package com.example.bluetoothtest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * 项目中连接的蓝牙设备为汉印蓝牙打印机，型号为 HM-A300
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //请求权限，简单处理一下，实际项目开发要完善比如回调的处理
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH
            ),
            123
        )

        //蓝牙广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        registerReceiver(mBlueToothStateReceiver, intentFilter)

        //跳转到蓝牙连接页面
        connectBluetooth.setOnClickListener {
            startActivity(Intent(this, BtConnectActivity::class.java))
        }
        //测试打印二维码
        tvPrintQrCodeTest.setOnClickListener {
            printQRCodeTest()
        }
        //测试打印字符串
        tvPrintStringTest.setOnClickListener {
            printStringTest()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(mBlueToothStateReceiver)
        Printer.closeBluetooth()
        super.onDestroy()
    }

    //蓝牙广播
    private val mBlueToothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {//蓝牙设备断开
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.apply {
                    if (Printer.getDeviceAddress() == address) Printer.closeBluetooth()
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {//蓝牙状态改变
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 1000)) {
                    BluetoothAdapter.STATE_OFF -> {//蓝牙关闭
                        Printer.closeBluetooth()
                    }
                    BluetoothAdapter.STATE_ON -> {//蓝牙打开
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> {
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                    }
                }
            }
        }
    }

    /**
     * 发送的协议和字节码是基于 汉印HM-A300 打印设备的
     * 打印二维码，使用协程放到子线程中操作，同时绑定生命周期
     */
    private fun printQRCodeTest() {
        lifecycleScope.launch(Dispatchers.IO) {
            Printer.openEndStatic(true)
            Printer.printAreaSize(0, 200, 200, 300, 1)
            Printer.printQR("BARCODE", 0, 0, 2, 10, "https://www.baidu.com")
            Printer.print()
            showPrintStatus()
        }
    }

    /**
     * 发送的协议和字节码是基于 汉印HM-A300 打印设备的
     * 打印字符串，使用协程放到子线程中操作，同时绑定生命周期
     */
    private fun printStringTest() {
        lifecycleScope.launch(Dispatchers.IO) {
            Printer.openEndStatic(true)
            Printer.printAreaSize(0, 200, 200, 120, 1)
            Printer.align("CENTER")
            Printer.text("T", 8, 20, 0, "CODE128fasdf232342dafasdfasdf2342343")
            Printer.print()
            showPrintStatus()
        }
    }

    /**
     * 发送的协议和字节码是基于 汉印HM-A300 打印设备的
     * 获取打印结果，使用协程放到子线程中操作，同时绑定生命周期
     */
    private fun showPrintStatus() {
        val resultStatus = Printer.getEndStatus(10)
        Printer.openEndStatic(false)
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(
                applicationContext,
                "result：${if (resultStatus == 0) "success" else "fail$resultStatus"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}





























