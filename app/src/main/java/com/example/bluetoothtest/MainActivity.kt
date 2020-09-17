package com.example.bluetoothtest

import android.Manifest
import android.content.Intent
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

        connectBluetooth.setOnClickListener {
            startActivity(Intent(this, BtActivity::class.java))
        }
        tvPrintQrCodeTest.setOnClickListener {
            printQRCodeTest()
        }
        tvPrintStringTest.setOnClickListener {
            printStringTest()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Printer.closeBluetooth()
    }

    private fun printQRCodeTest() {
        lifecycleScope.launch(Dispatchers.IO) {
            Printer.openEndStatic(true)
            Printer.printAreaSize(0, 200, 200, 300, 1)
            Printer.printQR("BARCODE", 0, 0, 2, 10, "https://www.baidu.com")
            Printer.print()
            showPrintStatus()
        }
    }

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





























