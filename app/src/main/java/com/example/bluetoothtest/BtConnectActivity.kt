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

    //搜索到的蓝牙设备列表
    private lateinit var mBluetoothAdapter: BluetoothAdapter

    //蓝牙搜索类
    private val mBluetoothDiscovery by lazy { BluetoothDiscovery(this, mBluetoothAdapter) }

    //蓝牙设备列表适配器
    private val mAdapter by lazy {
        object :
            BaseQuickAdapter<PrintDeviceBean, BaseViewHolder>(
                android.R.layout.simple_list_item_2, null
            ) {
            override fun convert(holder: BaseViewHolder, item: PrintDeviceBean) {
                holder.setText(android.R.id.text1, item.name)
                holder.setText(android.R.id.text2, item.mac)
            }
        }
    }

    //加载弹窗
    private val mLoadingDialog by lazy { LoadingDialog.build(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bt)

        initView()
        initListener()

        val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        //判断蓝牙功能是否开启，如果没有开启的话就跳转到开启蓝牙页面
        if (adapter == null || !adapter.isEnabled) {
            Toast.makeText(this, "蓝牙功能未开启，请开启蓝牙功能", Toast.LENGTH_LONG).show()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 2)
            finish()
        } else {
            mBluetoothAdapter = adapter
            //将搜索蓝牙的添加到LifeCycle里面进行生命周期自动管理
            lifecycle.addObserver(mBluetoothDiscovery)
            //监听搜索到的蓝牙设备
            initBluFindListener()
            //开始搜索蓝牙设备
            startSearchBtDevices()
        }
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
            swipeRefresh.isRefreshing = false
            startSearchBtDevices()
        }
        mAdapter.setOnItemClickListener { _, _, position ->
            //点击了列表的蓝牙设备，连接蓝牙设备，由于是耗时操作，使用协程放到子线程中操作
            lifecycleScope.launch(Dispatchers.Main) {
                connectBtDevice(mAdapter.data[position])
            }
        }
    }

    /**
     * 设置蓝牙搜索监听
     */
    private fun initBluFindListener() {
        mBluetoothDiscovery.mListener = { name, macAddress ->
            //发现了蓝牙设备，判断列表中是否有，没有就添加到列表
            var isHas = false
            mAdapter.data.forEach {
                if (name == it.mac) {
                    isHas = true
                    return@forEach
                }
            }
            if (!isHas) mAdapter.addData(PrintDeviceBean(name, macAddress))
        }
    }

    /**
     * 开始搜索蓝牙设备
     */
    private fun startSearchBtDevices() {
        mAdapter.setNewInstance(null)
        mBluetoothDiscovery.doDiscovery()
    }

    /**
     * 连接蓝牙设备
     */
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
































