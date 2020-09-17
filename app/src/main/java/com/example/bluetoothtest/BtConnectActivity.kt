package com.example.bluetoothtest

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.activity_bt.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 蓝牙连接页面
 */
class BtConnectActivity : AppCompatActivity() {

    private lateinit var mBluetoothAdapter: BluetoothAdapter

    private val mBtDeviceList: MutableList<PrintDeviceBean> = mutableListOf()
    private val mBluetoothDiscovery by lazy { BluetoothDiscovery(this, mBluetoothAdapter) }
    private val mAdapter by lazy {
        object :
            BaseQuickAdapter<PrintDeviceBean, BaseViewHolder>(
                android.R.layout.simple_list_item_2, mBtDeviceList
            ) {
            override fun convert(holder: BaseViewHolder, item: PrintDeviceBean) {
                holder.setText(android.R.id.text1, item.name)
                holder.setText(android.R.id.text2, item.mac)
            }
        }
    }
    private val mLoadingDialog by lazy { LoadingDialog.build(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bt)

        initView()
        initListener()

        val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            Toast.makeText(this, "蓝牙功能未开启", Toast.LENGTH_LONG).show()
            finish()
        } else {
            mBluetoothAdapter = adapter
            initBlu()
            lifecycle.addObserver(mBluetoothDiscovery)
            if (!mBluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 2)
            } else {
                startSearchBtDevices()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (mBluetoothAdapter.isEnabled) startSearchBtDevices() else finish()
    }

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        recyclerView.adapter = mAdapter
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun initListener() {
        swipeRefresh.setOnRefreshListener {
            mBluetoothDiscovery.disReceiver()
            startSearchBtDevices()
            if (swipeRefresh.isRefreshing) swipeRefresh.isRefreshing = false
        }
        mAdapter.setOnItemClickListener { _, _, position ->
            lifecycleScope.launch(Dispatchers.Main) {
                connectBtDevice(mBtDeviceList[position])
            }
        }
    }

    private fun initBlu() {
        mBluetoothDiscovery.mListener = { name, macAddress ->
            var isHas = false
            for (printBT in mBtDeviceList) {
                if (name == printBT.mac) {
                    isHas = true
                    break
                }
            }
            if (!isHas) {
                val printBT = PrintDeviceBean(name, macAddress)
                mBtDeviceList.add(printBT)
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun startSearchBtDevices() {
        mBtDeviceList.clear()
        mAdapter.notifyDataSetChanged()
        mBluetoothDiscovery.doDiscovery()
    }

    private suspend fun connectBtDevice(device: PrintDeviceBean) {
        mLoadingDialog.show()
        val connectResult = withContext(Dispatchers.IO) {
            Printer.connectBluetooth(mBluetoothAdapter, device.mac)
        }
        mLoadingDialog.dismiss()
        Toast.makeText(
            applicationContext, if (connectResult) "蓝牙连接成功" else "蓝牙连接失败", Toast.LENGTH_LONG
        ).show()
        if (connectResult) finish()
    }

}
































