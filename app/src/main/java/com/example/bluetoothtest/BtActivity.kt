package com.example.bluetoothtest

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.activity_bt.*

/**
 * 蓝牙连接
 */
class BtActivity : AppCompatActivity() {

    private lateinit var mBluetoothAdapter: BluetoothAdapter

    private val mList: MutableList<PrintBT> = mutableListOf()
    private val mBluetooth by lazy { Bluetooth(this, mBluetoothAdapter) }
    private val mAdapter by lazy {
        object :
            BaseQuickAdapter<PrintBT, BaseViewHolder>(android.R.layout.simple_list_item_2, mList) {
            override fun convert(holder: BaseViewHolder, item: PrintBT) {
                holder.setText(android.R.id.text1, item.bTname)
                holder.setText(android.R.id.text2, item.bTmac)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bt)

        val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            Toast.makeText(this, "没有找到蓝牙适配器", Toast.LENGTH_LONG).show()
            finish()
        } else {
            mBluetoothAdapter = adapter

            listBluetoothDevice()
            initBT()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBluetooth.disReceiver()
    }

    private fun listBluetoothDevice() {
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 2)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        recyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener { _, _, position ->
            Printer.connectBluetooth(mBluetoothAdapter, mList[position].bTmac) {
                Toast.makeText(
                    applicationContext,
                    if (it) "蓝牙连接成功" else "蓝牙连接失败",
                    Toast.LENGTH_LONG
                ).show()
                if (it) finish()
            }
        }
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        swipeRefresh.setOnRefreshListener {
            mBluetooth.disReceiver()
            initBT()
            if (swipeRefresh.isRefreshing) swipeRefresh.isRefreshing = false
        }
    }

    private fun initBT() {
        mList.clear()
        mAdapter.notifyDataSetChanged()
        mBluetooth.doDiscovery()
        mBluetooth.mListener = { name, macAddress ->
            var isHas = false
            for (printBT in mList) {
                if (name == printBT.bTmac) {
                    isHas = true
                    break
                }
            }
            if (!isHas) {
                val printBT = PrintBT(name, macAddress)
                mList.add(printBT)
                mAdapter.notifyDataSetChanged()
            }
        }
    }

}
































