# core_base 网络库发布

`core_base` 已按网络依赖发布方式配置。发布时会推送 `vX.Y.Z` tag 给 JitPack 构建，同时 GitHub Actions 也会把 AAR 上传到 GitHub Packages。

## 当前坐标

```gradle
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation "com.github.wukuiqing49.AndroidCoreBase:core_base:v1.0.1"
}
```

版本号来自 Git tag，格式为 `vX.Y.Z`。当前版本记录在：

```text
core_base/version.properties
```

## 本地一键发版

Windows PowerShell：

```powershell
.\scripts\release-core-base.ps1
```

默认递增 `patch`，例如 `1.0.1` -> `1.0.2`。也可以指定递增方式：

```powershell
.\scripts\release-core-base.ps1 -Bump minor
.\scripts\release-core-base.ps1 -Bump major
```

脚本会自动：

1. 检查 Git 工作区是否干净。
2. 读取并递增 `core_base/version.properties`。
3. 执行 `:core_base:publishReleasePublicationToMavenLocal` 验证发布产物。
4. 提交版本文件。
5. 创建并推送 `vX.Y.Z` tag。
6. 输出 JitPack 构建地址。

只想本地演练、不推送：

```powershell
.\scripts\release-core-base.ps1 -NoPush
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
    implementation "com.github.wukuiqing49.AndroidCoreBase:core_base:v1.0.1"
}
```

## 手动验证

```powershell
.\gradlew.bat :core_base:publishReleasePublicationToMavenLocal "-PPOM_GROUP_ID=com.github.wukuiqing49.AndroidCoreBase" "-PPOM_VERSION=v1.0.1" "-PGITHUB_REPOSITORY=wukuiqing49/AndroidCoreBase"
```

验证通过后，打开：

```text
https://jitpack.io/#wukuiqing49/AndroidCoreBase/v1.0.1
```
