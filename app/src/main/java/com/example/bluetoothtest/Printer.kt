package com.example.bluetoothtest

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * 打印工具类
 */
object Printer {

    private var mDevice: BluetoothDevice? = null
    private var mSocket: BluetoothSocket? = null
    private var mInputStream: InputStream? = null
    private var mOutputStream: OutputStream? = null

    fun connectBluetooth(
        bluetoothAdapter: BluetoothAdapter, macAddress: String, resultListener: (Boolean) -> Unit
    ) {
        try {
            if (BluetoothAdapter.checkBluetoothAddress(macAddress)) {
                mDevice = bluetoothAdapter.getRemoteDevice(macAddress)
                mSocket = mDevice?.createInsecureRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")//这个值是固定的
                )
                mSocket?.connect()
                mInputStream = mSocket?.inputStream
                mOutputStream = mSocket?.outputStream
                resultListener(mInputStream != null && mOutputStream != null)
            }
        } catch (var2: IOException) {
            var2.printStackTrace()
            resultListener(false)
        }
    }

    fun closeBluetooth() {
        mInputStream?.close()
        mOutputStream?.close()
        mSocket?.close()
    }

    /**
     * 页标签开始指令
     * @param offset 此值使所有字段将水平偏移指定的单位数量
     * @param horizontal 水平方向的 dpi。（根据打印机的 dpi 设置，200dpi 打印机：8px=1mm）
     * @param vertical 垂直方向的 dpi。（同上）
     * @param height 整个标签的高度。（单位：px）
     * @param qty 打印的次数
     */
    fun printAreaSize(offset: Int, horizontal: Int, vertical: Int, height: Int, qty: Int) {
        writeData(ByteUtils.getBytes("! $offset $horizontal $vertical $height $qty\r\n"))
    }

    /**
     * @param command 打印方向 BARCODE:水平方向  VBARCODE:垂直方向
     * @param x 二维码的起始横坐标
     * @param y 二维码的起始纵坐标
     * @param M QR 的类型：类型 1 和类型 2；类型 2 是增加了个别的符号，提供了额外的功 能
     * @param U 单位宽度/模块的单元高度。 范围是 1 到 32 默认为 6
     * @param data 二维码的数据
     */
    fun printQR(command: String, x: Int, y: Int, M: Int, U: Int, data: String) {
        writeData(ByteUtils.getBytes("$command QR $x $y M $M U $U\r\nMA,$data\r\nENDQR\r\n"))
    }

    /**
     * 开始打印
     */
    fun print() {
        writeData(ByteUtils.getBytes("PRINT\r\n"))
    }

    /**
     * 获取打印机状态
     * @param timeOut 获取状态超时时间，单位秒
     */
    fun getEndStatus(timeOut: Int): Int {
        var status = -1
        var readBytes: ByteArray? = null
        while (status == -1) {
            if (readData(timeOut).also { readBytes = it }.isEmpty()) {
                return -1
            }
            val readStr: String = byteToHex(readBytes)
            status = readStr.lastIndexOf("CC")
        }
        return readBytes!![status / 3 + 1].toInt()
    }

    /**
     * 设置字体对齐方式
     * @param align CENTER:居中   LEFT:居左     RIGHT:居右
     */
    fun align(align: String) {
        writeData(ByteUtils.getBytes("$align\r\n"))
    }

    /**
     * 打印文本
     * @param command 文字的方向 T：水平。 T90：逆时针旋转 90 度。 T180：逆时针旋转 180 度。 T270：逆时针旋转 270 度。
     * @param font 字体。
     *             0：24x24 或 12x24，视中英文而定。（泰语：24x48）
     *             1：7x19（英文），24x24（繁体）。
     *             3：20x20 或 10x20，视中英文而定。
     *             4：32x32 或 16x32，由 ID3 字体宽高各放大 2 倍。
     *             8：24x24 或 12x24，视中英文而定。
     *             55：16x16 或 8x16，视中英文而定。
     * @param x 起始点的横坐标。（单位：px）
     * @param y 起始点的纵坐标。（单位：px）
     * @param data 文本数据。
     */
    fun text(command: String, font: Int, x: Int, y: Int, data: String) {
        writeData(ByteUtils.getBytes("$command $font 0 $x $y $data\r\n"))
    }

    /**
     * 获取打印完成时状态开关
     */
    fun openEndStatic(canGetStatus: Boolean) {
        if (canGetStatus) {
            mOutputStream?.write(byteArrayOf(27, 27, 49, 1))
        } else {
            mOutputStream?.write(byteArrayOf(27, 27, 49, 0))
        }
    }

    /**
     * 读取数据
     * @param timeOut 获取数据超时时间，单位 秒
     */
    private fun readData(timeOut: Int): ByteArray {
        var retArr = ByteArray(0)
        if (mInputStream == null) return retArr
        try {
            var spendtime = 0
            while (spendtime < timeOut * 10) {
                var dataSize: Int
                if (mInputStream!!.available().also { dataSize = it } > 0) {
                    retArr = ByteArray(dataSize)
                    mInputStream!!.read(retArr)
                    spendtime = timeOut * 10 + 1
                } else {
                    Thread.sleep(100L)
                    ++spendtime
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return retArr
    }

    /**
     * 写入数据
     */
    private fun writeData(writeBytes: ByteArray) {
        if (mOutputStream == null) return
        try {
            val writeByteLength = writeBytes.size
            val tempBytes = ByteArray(10000)
            val needWriteTimes = writeByteLength / 10000
            var tempIndex: Int
            for (i in 0 until needWriteTimes) {
                tempIndex = i * 10000
                while (tempIndex < (i + 1) * 10000) {
                    tempBytes[tempIndex % 10000] = writeBytes[tempIndex]
                    ++tempIndex
                }
                mOutputStream!!.write(tempBytes, 0, tempBytes.size)
                mOutputStream!!.flush()
            }
            if (writeByteLength % 10000 != 0) {
                val endIndex = ByteArray(writeBytes.size - needWriteTimes * 10000)
                tempIndex = needWriteTimes * 10000
                while (tempIndex < writeBytes.size) {
                    endIndex[tempIndex - needWriteTimes * 10000] = writeBytes[tempIndex]
                    ++tempIndex
                }
                mOutputStream!!.write(endIndex, 0, endIndex.size)
                mOutputStream!!.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun byteToHex(data: ByteArray?): String {
        if (data == null) return ""
        val retArr = StringBuilder(data.size)
        for (element in data) {
            retArr.append(String.format("%02X ", element))
        }
        return retArr.toString()
    }

}



























