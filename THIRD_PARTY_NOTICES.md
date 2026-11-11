# 第三方说明

MIT 许可只适用于本项目新增代码及新增修改，不改变原应用或第三方组件的授权。

本工程包含从原应用恢复的 Java 代码、资源及兼容运行库，未附有可确认的原应用开源许可。相关权利仍归原权利人；本仓库不授予这些部分额外的使用或再分发权利。

`app/libs/relocated-legacy-runtime.jar` 包含原运行时依赖及重定位的 AndroidX 类；`app/src/main/jniLibs` 包含腾讯地图、定位、友盟及崩溃统计组件，分别遵循原厂商条款。保留这些二进制文件用于兼容原工程。

Material Components、AndroidX、RxJava、OkHttp、Gson、Butter Knife 采用 Apache-2.0。许可证正文见 `third-party-notices/Apache-2.0.txt`。coordtransform 的 MIT 许可见 `third-party-notices/coordtransform-LICENSE.txt`。
