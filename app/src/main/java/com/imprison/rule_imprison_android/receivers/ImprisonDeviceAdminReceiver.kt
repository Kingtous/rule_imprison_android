package com.imprison.rule_imprison_android.receivers

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context

class ImprisonDeviceAdminReceiver: DeviceAdminReceiver() {
    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext,
                ImprisonDeviceAdminReceiver::class.java
            )
        }
    }
}