# YunWu Phone V1.0

Android 原生 Kotlin 电话应用项目，浅色主题，GitHub Actions 自动构建 APK。

## 当前功能
- 拨号界面、电话拨出（通过 Android Telecom 系统）
- 读取并搜索系统通讯录，点击联系人拨号
- 申请成为 Android 默认电话应用
- InCallService 接入系统通话生命周期
- 接听、挂断、免提路由、保持通话基础控件
- 接通后自动尝试使用 `VOICE_CALL` 音源录音
- 录音目录：应用专属外部文件目录 `CallRecordings`

## 重要：通话录音兼容性
Android 对第三方应用录制蜂窝通话有严格限制。`VOICE_CALL` 音源通常要求系统/特权权限，普通 Play/侧载应用在许多原生 Android 设备上无法访问。成为默认拨号器也不会自动获得 `CAPTURE_AUDIO_OUTPUT` 权限。

本版本不会静默切换到麦克风录音并声称录到了双方声音。若系统拒绝录音，会在通话界面显示失败原因。不同厂商 ROM 可能有额外限制或厂商级支持。请遵守当地法律，并在录音前告知通话参与者、取得必要同意。

## 构建
1. 将 ZIP 解压。
2. 上传整个项目到 GitHub 仓库（包含隐藏的 `.github` 文件夹）。
3. 打开仓库 Actions，运行 `Build YunWu Phone APK`。
4. 在成功的 workflow run 底部下载 `YunWu-Phone-V1.0-APK` artifact。

工作流会生成 Debug APK 和 Release APK。当前 Release APK 使用默认调试签名，不适合长期正式更新；正式分发前应配置专属 keystore 和 GitHub Secrets，保证后续版本使用同一签名。

## 最低要求
- Android 8.0（API 26）及以上
- 具备蜂窝电话功能的设备
- 需要用户授予通讯录、电话、麦克风等运行时权限
- 需要用户主动将 YunWu Phone 设为默认电话应用，系统才会交由它处理来电界面

## V1.0 已知限制
- 录音能否启动取决于设备系统权限；多数标准 Android 系统会拒绝第三方 `VOICE_CALL` 录音。
- 通话记录页面、录音文件浏览/播放/导出、联系人编辑、完整通话计时、双 SIM 选择和更完整的音频路由将在后续版本完善。
- 本项目是可构建的 V1.0 起始实现，建议先在备用设备测试通话流程。
