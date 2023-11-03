package com.imprison.rule_imprison_android.services

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.imprison.rule_imprison_android.aidl.IDeviceManagerService
import com.imprison.rule_imprison_android.receivers.ImprisonDeviceAdminReceiver

class DeviceManagerService: Service() {

    companion object {
        val binder = DeviceManagerBinder()
        val shopApps = arrayOf<String>("com.jingdong.app.mall", "com.taobao.taobao")
        val triggerApps = arrayOf("tv.danmaku.bili")
        val TAG = "IMPRISION_DEVICE_MANAGER_SERVIE"
    }

    private var dmp: DevicePolicyManager? = null
    private lateinit var mComponentName: ComponentName
    private var isHidden: Boolean = true

    override fun onCreate() {
        super.onCreate()
        mComponentName = ImprisonDeviceAdminReceiver.getComponentName(this)
        binder.setOnCurrentActivityChanged { pkg, ty ->
            handleForegroundActivityChanged(pkg, ty)
        }
        dmp = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        Log.d(TAG, "DMS服务启动")
    }

    private fun hideApp(packageName: String, hidden: Boolean) {
        if (isHidden == hidden) {
            return
        }
        val mDmp = dmp
        if (mDmp == null || !mDmp.isAdminActive(mComponentName)) {
            return
        }
        try {
            val res = mDmp.setApplicationHidden(mComponentName, packageName, hidden)
            if (!res) {
                Log.e(TAG, "设置 $packageName 为 $hidden 失败")
            } else {
                Log.e(TAG, "设置 $packageName 为 $hidden 成功")
            }
        } catch (e: Exception) {
            Log.e(TAG, "设置 $packageName 为 $hidden 失败Exception: $e")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 支持自启
        Log.d(TAG, "配置DMS自启")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun handleForegroundActivityChanged(pkg: String, ty: Int) {
        Log.d(TAG, "收到ALS请求：$pkg")
        if (dmp == null || !dmp!!.isAdminActive(mComponentName)) {
            dmp = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            Log.e(TAG, "设备管理器未就绪")
            return
        }
        val isHide = pkg in triggerApps
        for (app in shopApps) {
            hideApp(app, isHide)
        }
        isHidden = isHide
    }
}

class DeviceManagerBinder: IDeviceManagerService.Stub() {

    private var onCurrentActivityChangedCallback: ((String, Int) -> Unit)? = null

    fun setOnCurrentActivityChanged(cb: ((String, Int) -> Unit)) {
        onCurrentActivityChangedCallback = cb
    }

    override fun onCurrentActivityChanged(packageName: String?, type: Int) {
        if (packageName == null) {
            return
        }
        onCurrentActivityChangedCallback?.let {
            it(packageName, type)
        }
    }
}