# AndroidCoreBase

`AndroidCoreBase` 是一个 Android 页面基础库，沉淀了常用 `Activity`、`Fragment`、`ViewModel`、列表、Adapter、弹框、权限、空布局、标题栏和系统栏适配能力。

仓库内包含：

- `core_base`：可发布的基础库模块。
- `app`：基础能力 Demo。

## 推荐发版方式

修改代码后优先使用自动发版脚本。脚本会读取本地和远端已有 tag，默认自动执行 patch +1，并完成版本替换、构建校验、本地 Maven 发布、提交、打 tag 和推送：

```powershell
.\scripts\release-core-base.ps1 -AllowDirty
```

如果希望先手动提交功能代码，再只让脚本生成发布提交和 tag，可以先提交代码后执行：

```powershell
.\scripts\release-core-base.ps1
```

只本地生成提交和 tag、不推送：

```powershell
.\scripts\release-core-base.ps1 -AllowDirty -SkipPush
```

指定 minor、major 或固定版本：

```powershell
.\scripts\release-core-base.ps1 -Bump minor -AllowDirty
.\scripts\release-core-base.ps1 -Bump major -AllowDirty
.\scripts\release-core-base.ps1 -Version 1.2.3 -AllowDirty
```

脚本不会覆盖已经存在的 tag。推送完成后打开 JitPack 页面触发构建：

```text
https://jitpack.io/#wukuiqing49/AndroidCoreBase/v1.0.3
```

完整说明见 [`core_base/docs/core_base_publish.md`](core_base/docs/core_base_publish.md)。

## 引用方式

### 本工程内引用

```gradle
dependencies {
    implementation project(":core_base")
}
```

### JitPack 引用

使用方项目的 `settings.gradle`：

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = "https://jitpack.io" }
    }
}
```

使用方业务模块的 `build.gradle`：

```gradle
dependencies {
    implementation "com.github.wukuiqing49:AndroidCoreBase:v1.0.3"
}
```

`v1.0.1` 需要替换成实际 Git tag。JitPack 版本号必须和 tag 完全一致。

## 发布检查

发版前脚本会自动执行：

```powershell
.\gradlew.bat :core_base:compileDebugKotlin
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :core_base:publishReleasePublicationToMavenLocal "-PPOM_GROUP_ID=com.github.wukuiqing49" "-PPOM_ARTIFACT_ID=AndroidCoreBase" "-PPOM_VERSION=v1.0.3" "-PGITHUB_REPOSITORY=wukuiqing49/AndroidCoreBase"
```

## 使用说明

基础能力、基类选择和组件用法见：

```text
core_base/docs/core_base_usage.md
```
