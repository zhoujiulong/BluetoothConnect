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
 * 蓝牙连接页面
 */
class BtActivity : AppCompatActivity() {

    private lateinit var mBluetoothAdapter: BluetoothAdapter

    private val mList: MutableList<PrintDeviceBean> = mutableListOf()
    private val mBluetoothDiscovery by lazy { BluetoothDiscovery(this, mBluetoothAdapter) }
    private val mAdapter by lazy {
        object :
            BaseQuickAdapter<PrintDeviceBean, BaseViewHolder>(android.R.layout.simple_list_item_2, mList) {
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

        initViewAndListener()

        val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            Toast.makeText(this, "蓝牙功能未开启", Toast.LENGTH_LONG).show()
            finish()
        } else {
            mBluetoothAdapter = adapter
            lifecycle.addObserver(mBluetoothDiscovery)
            if (!mBluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 2)
            } else {
                initBT()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (mBluetoothAdapter.isEnabled) initBT() else finish()
    }

    private fun initViewAndListener() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        recyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener { _, _, position ->
            mLoadingDialog.show()
            Printer.connectBluetooth(mBluetoothAdapter, mList[position].mac) {
                mLoadingDialog.dismiss()
                Toast.makeText(
                    applicationContext, if (it) "蓝牙连接成功" else "蓝牙连接失败", Toast.LENGTH_LONG
                ).show()
                if (it) finish()
            }
        }
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        swipeRefresh.setOnRefreshListener {
            mBluetoothDiscovery.disReceiver()
            initBT()
            if (swipeRefresh.isRefreshing) swipeRefresh.isRefreshing = false
        }
    }

    private fun initBT() {
        mList.clear()
        mAdapter.notifyDataSetChanged()
        mBluetoothDiscovery.mListener = { name, macAddress ->
            var isHas = false
            for (printBT in mList) {
                if (name == printBT.mac) {
                    isHas = true
                    break
                }
            }
            if (!isHas) {
                val printBT = PrintDeviceBean(name, macAddress)
                mList.add(printBT)
                mAdapter.notifyDataSetChanged()
            }
        }
        mBluetoothDiscovery.doDiscovery()
    }

}
































