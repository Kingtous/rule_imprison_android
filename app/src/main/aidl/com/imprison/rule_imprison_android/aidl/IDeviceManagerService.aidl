// IDeviceManagerService.aidl
package com.imprison.rule_imprison_android.aidl;

interface IDeviceManagerService {
    void onCurrentActivityChanged(String packageName, int type);
}