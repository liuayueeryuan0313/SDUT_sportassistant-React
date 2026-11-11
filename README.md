# 运动助手

Android 运动记录与路线编辑工具。

- 真实跑步记录、轨迹查看、里程和步数统计。
- 地图选点、手绘路线、GPX 导入和预设路线管理。
- 本地运动模拟，可调整速度、步频、高度及路线偏移。
- Material 3 界面、主题取色和深浅色切换。[1]

## 构建

Android Studio，JDK 17 或更高版本，Android SDK 35；最低支持 Android 6.0。

```sh
./gradlew :app:assembleDebug
```

地图需要自行配置腾讯位置服务 Key，可在应用设置中填写，或构建时传入 `-PtencentMapKey=你的Key`。重新签名后需重新绑定包名和证书。[2]

## 依赖

[1] [Material Components / AndroidX](https://github.com/material-components/material-components-android) · Apache-2.0  
[2] [腾讯地图与定位 SDK](https://lbs.qq.com/) · 厂商许可  
[3] [coordtransform](https://github.com/wandergis/coordtransform) · MIT  
[4] [RxJava](https://github.com/ReactiveX/RxJava) · Apache-2.0  
[5] [OkHttp](https://github.com/square/okhttp) · Apache-2.0  
[6] [Gson](https://github.com/google/gson) · Apache-2.0  
[7] [Butter Knife](https://github.com/JakeWharton/butterknife) · Apache-2.0

## 许可

新增代码采用 [MIT](LICENSE)。原应用恢复代码、厂商 SDK 和运行库不在该授权范围内，详见 [第三方说明](THIRD_PARTY_NOTICES.md)。
