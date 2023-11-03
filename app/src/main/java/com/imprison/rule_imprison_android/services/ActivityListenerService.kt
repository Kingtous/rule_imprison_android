package com.imprison.rule_imprison_android.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.IntentService
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.imprison.rule_imprison_android.aidl.IDeviceManagerService

class ActivityListenerService: AccessibilityService(), ServiceConnection {

    companion object {
        val TAG = "IMPRISONER_ALS"
    }
    private var deviceManagerServiceBinder: IDeviceManagerService? = null
    private var lastPkgName = ""
    private var lastEvtType = -1

    override fun onCreate() {
        super.onCreate()
        requestAlsService()
    }

    private fun requestAlsService() {
        val intent = Intent(this, DeviceManagerService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onServiceConnected() {
        serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
//            notificationTimeout = 100
        }
        super.onServiceConnected()
        Log.d(TAG, "服务开启")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName == null) {
            return
        }
        val pkgName = event.packageName.toString()
        if (pkgName == "com.android.systemui" || pkgName.contains("home")) {
            return
        }
//        Log.d(TAG, "$pkgName APP触发：${event}")
        if (lastPkgName == pkgName && event.eventType == lastEvtType) {
            return
        }
        lastPkgName = pkgName
        lastEvtType = event.eventType
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d(TAG, "ALS服务: $pkgName 触发 TYPE_WINDOW_STATE_CHANGED")
                try {
                    deviceManagerServiceBinder?.onCurrentActivityChanged(pkgName, AccessibilityEvent.TYPE_WINDOWS_CHANGED)
                } catch (exception: RemoteException) {
                    Log.e(TAG, "向DMS发送消息失败：${exception.toString()}")
                } catch (exception: SecurityException) {
                    Log.e(TAG, "向DMS发送消息失败Security：${exception.toString()}")
                }
            }
            else -> {}
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "服务关闭");
        TODO("Not yet implemented")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        deviceManagerServiceBinder = IDeviceManagerService.Stub.asInterface(service)
        Log.d(TAG, "DMS连接成功")
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d(TAG, "DMS服务退出，1s后尝试连接")
        deviceManagerServiceBinder = null
        Thread.sleep(1000)
        // 服务中断，重新请求一下
        requestAlsService()
    }
}