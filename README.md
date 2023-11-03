# 规则禁锢者 - Rule Imprison for Android

本app提供一种思路，利用Android设备管理器Device Owner + 无障碍服务机制，一定程度上阻止摇一摇广告跳转某购物APP或采用WebView降级策略。适用于所有Android设备，无ROOT的广大Android用户。

## 背景

随着购物平台双十一打响，各大超大型app也没闲着，加之摇一摇广告盛行，使得购物APP频繁被唤醒。
单纯内部打开一个WebView显示广告无所谓，但是频繁唤醒手机里一个大型购物APP，是不可容忍的。

## 技术点

本APP分为3个主要进程：UI部分+DMS（Device Manager Service）服务+ALS（Activity Listener Service）服务。
注册为Device Owner后，APP拥有操控系统任意APP的权利。
允许APP为设备管理器后，APP拥有了禁用/启用app的权利。
允许APP注册为无障碍服务程序后，APP可以监听当前的页面变化。

思路也就很明确了，ALS在后台监听页面变化，并连接DMS服务，DMS服务根据配置的触发逻辑，结合设备管理器权限，完成APP禁用/启动逻辑。


例子：

假设我们要使用的APP为APP(use)。购物APP为APP(shopping)。

1. 当我们打开APP(use)时：规则禁锢者的ALS服务检测到APP被打开，通知DMS服务。
2. DMS接收到APP(use)被打开后，DMS服务匹配成功，将APP(shopping)暂时禁用
3. 当我们退出APP(use)或者至为后台时，ALS通知DMS，DMS将APP(shopping)解除禁用

## 如何编译APP

按需自行修改DMS服务逻辑后编译，不提供预编译安装包

## 如何配置APP

- 安装APP后，不要立即运行。
- 将APP注册为设备Owner，打开开发者选项USB调试，连接电脑，进入adb shell，输入：
```agsl
dpm set-active-admin com.imprison.rule_imprison_android/.receivers.ImprisonDeviceAdminReceiver
```
注册APP为设备Owner需要满足以下条件：

1. 当前没有其他accounts。

注意：注册很可能会失败，需要在`设置-账号与同步`内删除所有（对，所有）的同步账户，删除后继续。建议把SIM卡也拔掉。另外支付宝也需要卸载，其会干扰Device Owner的设置。

## 鸣谢

谷歌提供的设备管理器和管理者API，让我自己用上了没有购物APP跳转烦恼的安卓手机。