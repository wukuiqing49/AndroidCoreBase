# core_base 网络版本发布手册

这份文档用于后续把 `core_base` 发布成 GitHub 网络依赖，方便其他 Android 项目直接引用。

## 推荐：自动发版脚本

修改代码后优先使用自动发版脚本。脚本会读取本地和远端已有 tag，默认自动执行 patch +1，并完成版本替换、构建校验、本地 Maven 发布、提交、打 tag 和推送：

```powershell
.\scripts\release-core-base.ps1 -AllowDirty
```

脚本会自动执行：

- 更新 `README.md`、发布手册和网络发布说明里的 `v1.0.x` 引用。
- 更新 `core_base/version.properties`。
- 执行 `:core_base:compileDebugKotlin`。
- 执行 `:app:assembleDebug`。
- 执行 `:core_base:publishReleasePublicationToMavenLocal`。
- `git commit -m "release core_base x.y.z"`。
- `git tag vx.y.z`。
- `git push origin main` 和 `git push origin vx.y.z`。

如果希望功能代码和发布提交分开，先手动提交功能代码，再执行：

```powershell
.\scripts\release-core-base.ps1
```

如果要把当前未提交改动一起放进发布提交，使用：

```powershell
.\scripts\release-core-base.ps1 -AllowDirty
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
https://jitpack.io/#wukuiqing49/AndroidCoreBase/vx.y.z
```

## 结论

- 公开给别人用，优先走 JitPack。
- JitPack 只需要 GitHub 仓库和 Git tag，使用方不需要 token。
- GitHub Packages 适合私有库或公司内部库，但使用方也要配置 GitHub token，门槛更高。
- 本工程内部开发仍然使用 `implementation project(':core_base')`。

## 当前发布配置

已配置文件：

```text
core_base/build.gradle
jitpack.yml
.github/workflows/release-core-base.yml
scripts/release-core-base.ps1
core_base/docs/core_base_publish.md
docs/core_base_network_release.md
README.md
```

`core_base` 已支持：

- release AAR 发布。
- sources jar 发布。
- consumer ProGuard 规则随 AAR 传递。
- JitPack 网络引用。
- GitHub Packages 发布。

## 发版前检查

每次发布前先确认：

```bash
./gradlew :core_base:compileDebugKotlin
```

建议再验证一次本地 Maven 发布：

```bash
./gradlew :core_base:publishReleasePublicationToMavenLocal "-PPOM_GROUP_ID=com.github.local" "-PPOM_VERSION=v1.0.3"
```

Windows PowerShell：

```powershell
.\gradlew.bat :core_base:compileDebugKotlin
.\gradlew.bat :core_base:publishReleasePublicationToMavenLocal "-PPOM_GROUP_ID=com.github.local" "-PPOM_VERSION=v1.0.3"
```

如果这两步失败，不要打 tag 发布。

## 推荐发布方式：JitPack

### 1. 提交代码

JitPack 只读取 GitHub 上的代码，本地未提交内容不会发布。

```bash
git status
git add .
git commit -m "release core_base 1.0.3"
git push origin master
```

### 2. 打版本 tag

示例发布 `v1.0.1`：

```bash
git tag v1.0.1
git push origin v1.0.1
```

如果 tag 打错了，不建议覆盖已经给别人用过的版本。还没公开使用时可以删除重打：

```bash
git tag -d v1.0.1
git push origin :refs/tags/v1.0.1
git tag v1.0.1
git push origin v1.0.1
```

### 3. 到 JitPack 构建

打开：

```text
https://jitpack.io/#wukuiqing49/AndroidCoreBase
```

输入 tag：

```text
v1.0.1
```

点击 `Get it`，等待构建成功。

### 4. 使用方引用

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

注意：版本号要和 Git tag 完全一致，例如 tag 是 `v1.0.2`，依赖里也写 `v1.0.2`。

## GitHub Packages 发布

这套方式已经配置，但不推荐公开库优先使用，因为使用方需要 token。

### 自动发布

已配置 workflow：

```text
.github/workflows/release-core-base.yml
```

触发方式：

- 在 GitHub Actions 页面手动运行 `Release core_base` workflow，并选择 `patch`、`minor` 或 `major`。
- workflow 会自动递增 `core_base/version.properties`、验证本地 Maven 发布、上传 GitHub Packages、提交版本文件、创建并推送 `vX.Y.Z` tag。
- JitPack 会根据推送的 tag 构建公开网络依赖。

### 本地一键发布到 JitPack

Windows PowerShell：

```powershell
.\scripts\release-core-base.ps1 -AllowDirty
```

默认递增 `patch`。也可以指定：

```powershell
.\scripts\release-core-base.ps1 -Bump minor -AllowDirty
.\scripts\release-core-base.ps1 -Bump major -AllowDirty
```

脚本会先验证 `:core_base:compileDebugKotlin`、`:app:assembleDebug` 和 `:core_base:publishReleasePublicationToMavenLocal`，再提交版本文件、创建 tag 并推送。推送成功后打开脚本输出的 JitPack 地址点击 `Get it`。

### 本地手动发布

需要在本机 `~/.gradle/gradle.properties` 配置：

```properties
gpr.user=GitHub用户名
gpr.key=GitHub Personal Access Token
GITHUB_REPOSITORY=wukuiqing49/AndroidCoreBase
```

不要把 token 提交到 Git。

发布命令：

```bash
./gradlew :core_base:publishReleasePublicationToGitHubPackagesRepository "-PPOM_GROUP_ID=com.github.wukuiqing49" "-PPOM_ARTIFACT_ID=AndroidCoreBase" "-PPOM_VERSION=v1.0.3"
```

Windows PowerShell：

```powershell
.\gradlew.bat :core_base:publishReleasePublicationToGitHubPackagesRepository "-PPOM_GROUP_ID=com.github.wukuiqing49" "-PPOM_ARTIFACT_ID=AndroidCoreBase" "-PPOM_VERSION=v1.0.3"
```

### 使用方引用 GitHub Packages

使用方需要配置仓库和凭证：

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/wukuiqing49/AndroidCoreBase")
            credentials {
                username = providers.gradleProperty("gpr.user").get()
                password = providers.gradleProperty("gpr.key").get()
            }
        }
    }
}
```

依赖：

```gradle
dependencies {
    implementation "com.github.wukuiqing49:AndroidCoreBase:v1.0.3"
}
```

## 版本号规则

推荐使用语义化版本：

```text
v主版本.次版本.修复版本
```

示例：

```text
v1.0.0
v1.0.1
v1.1.0
v2.0.0
v1.1.0-beta01
```

规则建议：

- 修 bug：升修复版本，例如 `v1.0.0` -> `v1.0.1`。
- 新增兼容能力：升次版本，例如 `v1.0.1` -> `v1.1.0`。
- 删除类、改方法签名、改包名、破坏旧用法：升主版本，例如 `v1.x.x` -> `v2.0.0`。

## 发布后检查

JitPack 构建成功后，建议新建或打开一个外部测试项目验证：

```gradle
implementation "com.github.wukuiqing49:AndroidCoreBase:v1.0.3"
```

至少验证：

- Gradle Sync 是否成功。
- 能否继承 `BaseActivity`。
- 能否使用 `BaseViewModel`。
- release 混淆打包是否成功。

## 混淆说明

`core_base` 已配置：

```gradle
consumerProguardFiles "consumer-rules.pro"
```

使用方通过 JitPack 或 GitHub Packages 引用时，consumer rules 会随 AAR 传递。正常开启 R8 不需要每个业务项目再单独写一份 `core_base` 专属混淆规则。

如果后续新增以下能力，需要同步检查 `consumer-rules.pro`：

- 反射创建类。
- WebView JSBridge。
- 序列化模型。
- 注解扫描。
- Native / so。
- 需要特殊 keep 的三方库。

## 常见问题

### JitPack 构建失败

先本地执行：

```bash
./gradlew :core_base:publishReleasePublicationToMavenLocal "-PPOM_GROUP_ID=com.github.local" "-PPOM_VERSION=v1.0.3"
```

本地都失败，先修本地构建。

### 使用方找不到依赖

检查：

- 使用方是否加了 `maven { url = "https://jitpack.io" }`。
- 依赖版本是否和 Git tag 完全一致。
- JitPack 页面对应 tag 是否构建成功。
- 坐标是否写成 `com.github.wukuiqing49:AndroidCoreBase:v1.0.3`。

### 使用方编译缺 AndroidX 类

检查 `core_base` 的 public API 是否新增了外部类型。如果新增类型出现在 public 方法、父类、泛型、返回值里，对应依赖要用 `api`，不能只用 `implementation`。

### 使用方混淆后崩溃

先确认使用的是最新版本。`core_base` 当前 consumer rules 会传递，正常不需要额外配置。如果新增了反射入口或三方库特殊规则，要更新 `core_base/consumer-rules.pro` 后重新发版。

## 快速发版命令

发布当前版本的完整命令：

```bash
.\scripts\release-core-base.ps1 -AllowDirty
```

然后打开：

```text
https://jitpack.io/#wukuiqing49/AndroidCoreBase
```

点击 `Get it`。
