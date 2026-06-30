# core_base 网络库发布

`core_base` 已按网络依赖发布方式配置。推荐使用本地自动发版脚本：它会自动递增版本、构建校验、发布到 Maven Local、提交代码、打 tag 并推送，JitPack 根据 tag 构建网络依赖。

## 推荐：自动发版脚本

修改代码后优先执行：

```powershell
.\scripts\release-core-base.ps1 -AllowDirty
```

脚本默认读取本地和远端已有 tag，自动执行 patch +1。例如当前最高 tag 是 `v1.0.1`，会自动发布 `v1.0.2`。

脚本会自动：

1. 计算下一个版本号，也可通过 `-Version 1.2.3` 指定。
2. 更新 `core_base/version.properties` 和文档中的依赖版本。
3. 执行 `:core_base:compileDebugKotlin`。
4. 执行 `:app:assembleDebug`。
5. 执行 `:core_base:publishReleasePublicationToMavenLocal`。
6. 提交发布改动。
7. 创建 `vX.Y.Z` tag。
8. 推送 `main` 和 tag 到 GitHub。

只本地生成提交和 tag、不推送：

```powershell
.\scripts\release-core-base.ps1 -AllowDirty -SkipPush
```

指定递增方式：

```powershell
.\scripts\release-core-base.ps1 -Bump minor -AllowDirty
.\scripts\release-core-base.ps1 -Bump major -AllowDirty
```

手动指定版本：

```powershell
.\scripts\release-core-base.ps1 -Version 1.2.3 -AllowDirty
```

脚本不会覆盖已经存在的 tag。推送完成后打开：

```text
https://jitpack.io/#wukuiqing49/AndroidCoreBase/vX.Y.Z
```

## 当前坐标

```gradle
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation "com.github.wukuiqing49:AndroidCoreBase:v1.0.3"
}
```

版本号来自 Git tag，格式为 `vX.Y.Z`。当前版本记录在：

```text
core_base/version.properties
```

## 本地一键发版

Windows PowerShell：

```powershell
.\scripts\release-core-base.ps1 -AllowDirty
```

默认递增 `patch`，例如 `1.0.1` -> `1.0.2`。也可以指定递增方式：

```powershell
.\scripts\release-core-base.ps1 -Bump minor -AllowDirty
.\scripts\release-core-base.ps1 -Bump major -AllowDirty
```

脚本会自动：

1. 读取本地和远端已有 tag，计算下一个版本。
2. 更新 `core_base/version.properties`。
3. 执行 `:core_base:compileDebugKotlin`。
4. 执行 `:app:assembleDebug`。
5. 执行 `:core_base:publishReleasePublicationToMavenLocal` 验证发布产物。
6. 提交发布改动。
7. 创建并推送 `vX.Y.Z` tag。
8. 输出 JitPack 构建地址。

只想本地演练、不推送：

```powershell
.\scripts\release-core-base.ps1 -AllowDirty -SkipPush
```

## GitHub Actions 一键发版

打开 GitHub Actions，手动运行：

```text
Release core_base
```

选择递增方式：

- `patch`: `1.0.1` -> `1.0.2`
- `minor`: `1.0.1` -> `1.1.0`
- `major`: `1.0.1` -> `2.0.0`

workflow 会自动更新版本文件、验证 Maven 本地发布、上传 GitHub Packages、提交版本、创建并推送 tag。JitPack 会根据 tag 构建公开网络依赖。

GitHub Packages 坐标同样使用当前 tag 作为版本：

```gradle
dependencies {
    implementation "com.github.wukuiqing49:AndroidCoreBase:v1.0.3"
}
```

## 手动验证

```powershell
.\gradlew.bat :core_base:publishReleasePublicationToMavenLocal "-PPOM_GROUP_ID=com.github.wukuiqing49" "-PPOM_ARTIFACT_ID=AndroidCoreBase" "-PPOM_VERSION=v1.0.3" "-PGITHUB_REPOSITORY=wukuiqing49/AndroidCoreBase"
```

验证通过后，打开：

```text
https://jitpack.io/#wukuiqing49/AndroidCoreBase/v1.0.3
```
