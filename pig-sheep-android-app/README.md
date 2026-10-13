# 猪羊弟 Android 全屏封装（内部试玩版）

这是一个 Android WebView 壳应用，加载线上联机游戏：

- URL: https://pig-sheep-online.onrender.com/
- 启动后固定横屏
- 沉浸式全屏，隐藏系统状态栏和导航栏；从边缘滑动时系统栏可短暂出现
- 开启 JavaScript、DOM Storage 和 Cookie，以支持游戏大厅和 Socket.IO
- 网络错误时提供重新连接页面
- 不包含游戏服务器代码；游戏需要网络连接，Render 服务必须可用

## 推荐方式：使用 GitHub Actions 自动构建 APK

这个项目包含 `.github/workflows/build-apk.yml`。它会在代码 push 后自动构建 Android debug APK，不需要在本地安装 Android Studio/SDK。

1. 在 GitHub 创建一个新的空仓库，例如 `pig-sheep-android-app`。
2. 解压本项目 ZIP，把里面的全部文件和文件夹上传到仓库根目录（要保留 `.github/workflows/build-apk.yml`）。
3. 等待 GitHub 的 **Actions** 页面出现 `Build Pig Sheep Android APK` 工作流并完成。
4. 打开该次工作流运行记录，在页面底部 **Artifacts** 区域下载 `pig-sheep-debug-apk`。
5. 解压 Artifact，取得 `app-debug.apk`，传到 Android 手机并安装。首次安装时，手机可能要求允许该文件来源的应用安装 APK。

> 这是用于自测的 debug APK，不是正式商店发布包。公开发布前还需配置正式签名、隐私政策、商店资料，并进行不同设备上的测试。

## 通过 Android Studio 构建

也可以在安装 JDK 17、Android Studio 和 Android SDK Platform 35 后打开此工程并同步 Gradle，然后构建 `app` 的 debug APK。若 Android Studio 要求 Gradle Wrapper，可先使用项目的 GitHub Actions 工作流构建，或在本地安装兼容的 Gradle 8.7。

## 当前边界

- App 本身是网络网页封装，不会在完全离线时运行游戏。
- 全屏能隐藏浏览器和系统栏、释放更多空间；网页自身的 CSS 仍须适配不同屏幕比例。App 会设置横屏和 mobile viewport，但仍建议在实际手机上测试。
- 该项目只允许游戏域名 `pig-sheep-online.onrender.com` 留在应用内；其他链接会打开系统浏览器。
- 退出应用可能中断当前房间连接；断线重连能力取决于线上游戏服务端本身。
