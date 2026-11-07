# 萌宠记账 PawLedger 🐾

一款**本地优先**的治愈系记账 App，融合「记账 + 存钱目标 + 猫咪养成 + 旅行青蛙式异步旅行」。

- 技术栈：Kotlin + Jetpack Compose + Material 3 + Room + WorkManager + DataStore
- 所有数据仅存本地，**不上传任何服务器**
- `minSdk 26` / `targetSdk 35`
- 美术风格：旅行青蛙式日系治愈手绘

---

## 一、如何把代码推送到 GitHub，拿到 APK

> 本仓库不含 APK 二进制，APK 由 GitHub Actions 自动构建。你只需 push 代码即可。

### 1. 创建 GitHub 仓库

1. 打开 <https://github.com/new>，仓库名随意（如 `PawLedger`），设为 **Public 或 Private** 均可，不要勾选 "Add a README"（避免冲突）。
2. 创建后复制仓库地址，形如 `https://github.com/你的用户名/PawLedger.git`。

### 2. 推送代码

在本项目根目录（`PawLedger/`）执行：

```bash
git init
git add .
git commit -m "init: PawLedger 萌宠记账"
git branch -M main
git remote add origin https://github.com/你的用户名/PawLedger.git
git push -u origin main
```

> 也可用 GitHub Desktop / VS Code 等图形化工具推送。

### 3. 触发构建

推送成功后，GitHub 会自动开始构建（触发条件：`push` 到 `main` 分支，或手动触发）。

---

## 二、如何在 Actions 页面下载 APK

1. 打开仓库页面，点击顶部 **Actions** 标签。
2. 左侧选择工作流 **"Build Debug APK"**，点击最近一次成功运行的记录（绿色 ✓）。
3. 页面底部 **Artifacts** 区域，点击 **`PawLedger-debug-apk`** 下载压缩包。
4. 解压后得到 `app-debug.apk`，发送到手机安装即可。

> 手动触发：Actions 页面右侧 **"Run workflow"** 按钮 → 选择 `main` 分支 → 运行。

### 安装提示

- 首次安装需在手机「设置 → 安全」中允许「安装未知来源应用」。
- 本 APK 为 debug 签名，仅供自用；正式分发请配置 release 签名。

---

## 三、首次安装后需要开启的权限

首次打开 App 会进入**权限引导页**，逐项开启（返回后自动重新检测）：

| 权限 | 用途 | 开启位置 |
|---|---|---|
| ① 通知使用权 | 自动识别支付宝/微信支付通知 | 设置 → 通知 → 通知访问权限（或 应用 → 特殊应用权限） |
| ② 悬浮窗权限 | 支付时弹出底部记账窗 | 设置 → 应用 → 特殊应用权限 → 在其他应用上层显示（各品牌路径略不同） |
| ③ 无障碍服务（**可选**） | 通知监听的补充手段 | 设置 → 无障碍 → 已下载的服务 |
| ④ 电池优化白名单 | 防止后台被系统清理 | 弹窗引导（关闭对本 App 的电池优化） |

> 其中 ① ② ④ 为必选，③ 可选、不影响核心功能。

---

## 四、Actions 构建失败：常见原因与排查

| 现象 | 可能原因 | 排查 |
|---|---|---|
| `Could not find com.android.tools.build:gradle` / 依赖下载失败 | 网络或仓库缓存问题 | 重跑一次工作流；确认 `libs.versions.toml` 版本号存在 |
| `SDK Platform android-35 not found` | 未安装 SDK 35 | 确认工作流含 `sdkmanager "platforms;android-35"` 步骤 |
| `Unsupported Java` / `Invalid Gradle JDK` | JDK 版本不对 | 确认 `setup-java` 使用 `temurin` + `java-version: 17` |
| `Could not determine java version from '17'` | Gradle 版本过低 | 工作流已固定 `gradle-version: 8.9`，勿改低 |
| `KSP / Room` 相关编译错误 | Kotlin 与 KSP 版本不匹配 | 保持 `kotlin=2.0.21` 与 `ksp=2.0.21-1.0.28` 一致 |
| `Artifact 下载为 0 字节` | 构建未产出 APK | 查看日志中 `assembleDebug` 是否成功 |

---

## 五、功能速览

- **首页**：总预算 / 余额 / 本月已花 / 剩余天数 / 分类占比 / 小屋伙伴 / 陪伴气泡
- **记账**：手动记一笔、待分类草稿、搜索筛选、退款关联、账户分类
- **伙伴**：猫「团子」/ 狗「豆包」/ 狐狸「小狐」，各 5 款皮肤；养成 + 旅行青蛙式异步旅行 + 积分商城 + 家具装点小屋
- **积分**：首次赠送 300；签到 +3/天；日/周/月存钱目标 +5/50/100；总目标达成额外奖励 10%–20%
- **日程**：日程表 + 备忘录（置顶 / 搜索 / 转日程）
- **我的**：签到、提醒开关、数据导出 CSV/JSON、备份恢复、权限管理

---

## 六、本地开发（可选）

用 Android Studio 打开项目（会自动生成 Gradle Wrapper），选择 `app` 运行即可。

- JDK 17、AGP 8.5.2、Gradle 8.9
- 若用命令行构建：`gradle :app:assembleDebug`
