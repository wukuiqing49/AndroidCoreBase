param(
    [ValidatePattern('^\d+\.\d+\.\d+$')]
    [string]$Version,

    [ValidateSet("patch", "minor", "major")]
    [string]$Bump = "patch",

    [string]$Remote = "origin",
    [string]$Branch = "main",
    [switch]$SkipPush,
    [switch]$AllowDirty
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

$versionFile = "core_base/version.properties"
$groupId = "com.github.wukuiqing49"
$artifactId = "AndroidCoreBase"
$githubRepository = "wukuiqing49/AndroidCoreBase"

function Get-VersionFromTag($tagName) {
    if ($tagName -match '^v?(\d+)\.(\d+)\.(\d+)$') {
        return [version]"$($matches[1]).$($matches[2]).$($matches[3])"
    }
    return $null
}

function Get-VersionFromFile($path) {
    if (-not (Test-Path $path)) {
        return $null
    }
    $line = Get-Content -LiteralPath $path -Encoding UTF8 |
        Where-Object { $_ -match '^VERSION_NAME=' } |
        Select-Object -First 1
    if (-not $line) {
        return $null
    }
    return Get-VersionFromTag (($line -replace '^VERSION_NAME=', '').Trim())
}

function Get-NextVersion([string]$bump) {
    $tagVersions = New-Object System.Collections.Generic.List[version]

    git tag --list "v*" | ForEach-Object {
        $parsed = Get-VersionFromTag $_
        if ($parsed) {
            $tagVersions.Add($parsed)
        }
    }

    git ls-remote --tags $Remote "refs/tags/v*" | ForEach-Object {
        $parts = $_ -split "\s+"
        if ($parts.Count -ge 2) {
            $name = ($parts[1] -replace '^refs/tags/', '') -replace '\^\{\}$', ''
            $parsed = Get-VersionFromTag $name
            if ($parsed) {
                $tagVersions.Add($parsed)
            }
        }
    }

    $fileVersion = Get-VersionFromFile $versionFile
    if ($fileVersion) {
        $latestTagVersion = $null
        if ($tagVersions.Count -gt 0) {
            $latestTagVersion = $tagVersions | Sort-Object -Descending | Select-Object -First 1
        }

        $fileTag = "v$fileVersion"
        $localFileTag = git tag --list $fileTag
        $remoteFileTag = git ls-remote --tags $Remote "refs/tags/$fileTag"
        if (-not $localFileTag -and -not $remoteFileTag -and
            (-not $latestTagVersion -or $fileVersion -gt $latestTagVersion)) {
            return "$fileVersion"
        }
    }

    if ($tagVersions.Count -eq 0) {
        return "1.0.0"
    }

    $latest = $tagVersions | Sort-Object -Descending | Select-Object -First 1
    switch ($bump) {
        "major" { return "$($latest.Major + 1).0.0" }
        "minor" { return "$($latest.Major).$($latest.Minor + 1).0" }
        default { return "$($latest.Major).$($latest.Minor).$($latest.Build + 1)" }
    }
}

function Run($command) {
    Write-Host ">> $command" -ForegroundColor Cyan
    Invoke-Expression $command
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed: $command"
    }
}

function Replace-InFile($path, [scriptblock]$replace) {
    if (-not (Test-Path $path)) {
        return
    }
    $content = Get-Content -LiteralPath $path -Raw -Encoding UTF8
    $updated = & $replace $content
    if ($updated -ne $content) {
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText((Resolve-Path $path), $updated, $utf8NoBom)
        Write-Host "updated $path"
    }
}

function Update-PublishCommand([string]$content) {
    $content = $content `
        -replace '"-PPOM_GROUP_ID=com\.github\.wukuiqing49\.AndroidCoreBase"', '"-PPOM_GROUP_ID=com.github.wukuiqing49"' `
        -replace '"-PPOM_GROUP_ID=com\.github\.wukuiqing49"', '"-PPOM_GROUP_ID=com.github.wukuiqing49"'

    if ($content -notmatch 'POM_ARTIFACT_ID=AndroidCoreBase') {
        $content = $content -replace '("-PPOM_GROUP_ID=com\.github\.wukuiqing49")', '$1 "-PPOM_ARTIFACT_ID=AndroidCoreBase"'
    }
    return $content
}

if ([string]::IsNullOrWhiteSpace($Version)) {
    $Version = Get-NextVersion $Bump
    Write-Host "Auto version: $Version ($Bump bump)" -ForegroundColor Green
} else {
    Write-Host "Manual version: $Version" -ForegroundColor Green
}

$tag = "v$Version"
$jitpackDependency = "$groupId`:$artifactId`:$tag"

$existingTag = git tag --list $tag
if ($existingTag) {
    throw "Tag $tag already exists locally. Choose a new version or delete the tag intentionally."
}

$remoteTag = git ls-remote --tags $Remote "refs/tags/$tag"
if ($remoteTag) {
    throw "Tag $tag already exists on $Remote. Choose a new version."
}

$statusBefore = git status --porcelain
if ($statusBefore -and -not $AllowDirty) {
    Write-Host "Working tree has uncommitted changes:" -ForegroundColor Yellow
    git status --short
    throw "Re-run with -AllowDirty to include current changes in the release commit."
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText((Resolve-Path $versionFile), "VERSION_NAME=$Version`n", $utf8NoBom)

Replace-InFile "README.md" {
    param($content)
    $content = $content `
        -replace 'com\.github\.wukuiqing49:AndroidCoreBase:v\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49:AndroidCoreBase:\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49\.AndroidCoreBase:core_base:v\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49\.AndroidCoreBase:core_base:\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49\.AndroidCoreBase:core_base:vx\.y\.z', $jitpackDependency `
        -replace 'AndroidCoreBase/v\d+\.\d+\.\d+', "AndroidCoreBase/$tag" `
        -replace 'POM_GROUP_ID=com\.github\.wukuiqing49\.AndroidCoreBase', 'POM_GROUP_ID=com.github.wukuiqing49' `
        -replace 'POM_VERSION=v?\d+\.\d+\.\d+', "POM_VERSION=$tag"
    Update-PublishCommand $content
}

Replace-InFile "docs/core_base_network_release.md" {
    param($content)
    $content = $content `
        -replace 'com\.github\.wukuiqing49:AndroidCoreBase:v\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49:AndroidCoreBase:\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49\.AndroidCoreBase:core_base:v\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49\.AndroidCoreBase:core_base:\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49\.AndroidCoreBase:core_base:vx\.y\.z', $jitpackDependency `
        -replace 'AndroidCoreBase/v\d+\.\d+\.\d+', "AndroidCoreBase/$tag" `
        -replace 'POM_GROUP_ID=com\.github\.wukuiqing49\.AndroidCoreBase', 'POM_GROUP_ID=com.github.wukuiqing49' `
        -replace 'POM_VERSION=v?\d+\.\d+\.\d+', "POM_VERSION=$tag"
    Update-PublishCommand $content
}

Replace-InFile "core_base/docs/core_base_publish.md" {
    param($content)
    $content = $content `
        -replace 'com\.github\.wukuiqing49:AndroidCoreBase:v\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49:AndroidCoreBase:\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49\.AndroidCoreBase:core_base:v\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49\.AndroidCoreBase:core_base:\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'com\.github\.wukuiqing49:core_base:v?\d+\.\d+\.\d+', $jitpackDependency `
        -replace 'AndroidCoreBase/v\d+\.\d+\.\d+', "AndroidCoreBase/$tag" `
        -replace 'POM_GROUP_ID=com\.github\.wukuiqing49\.AndroidCoreBase', 'POM_GROUP_ID=com.github.wukuiqing49' `
        -replace 'POM_VERSION=v?\d+\.\d+\.\d+', "POM_VERSION=$tag" `
        -replace 'release core_base v?\d+\.\d+\.\d+', "release core_base $Version"
    Update-PublishCommand $content
}

Replace-InFile "app/build.gradle" {
    param($content)
    $content = $content `
        -replace 'implementation\s+["'']com\.github\.wukuiqing49(?:\.AndroidCoreBase)?:core_base:v?\d+\.\d+\.\d+["'']\s*(\r?\n)?', '' `
        -replace 'implementation\s+["'']com\.github\.wukuiqing49:AndroidCoreBase:v?\d+\.\d+\.\d+["'']\s*(\r?\n)?', '' `
        -replace '(?m)^\s*//\s*implementation\s+project\(":core_base"\)\s*$', '    implementation project(":core_base")'

    if ($content -notmatch 'implementation\s+project\(":core_base"\)') {
        $content = $content -replace '(dependencies\s*\{\s*)', "`$1`r`n    implementation project(`":core_base`")"
    }
    return $content
}

Run ".\gradlew.bat :core_base:compileDebugKotlin"
Run ".\gradlew.bat :app:assembleDebug"
Run ".\gradlew.bat :core_base:publishReleasePublicationToMavenLocal `"-PPOM_GROUP_ID=$groupId`" `"-PPOM_ARTIFACT_ID=$artifactId`" `"-PPOM_VERSION=$tag`" `"-PGITHUB_REPOSITORY=$githubRepository`""

$statusAfter = git status --porcelain
if (-not $statusAfter) {
    throw "No changes to release."
}

Run "git add README.md docs/core_base_network_release.md core_base/version.properties core_base/build.gradle core_base/docs/core_base_publish.md core_base/docs/core_base_usage.md app/build.gradle gradle/libs.versions.toml jitpack.yml scripts/release-core-base.ps1"
Run "git add core_base/src/main"
Run "git commit -m `"release core_base $Version`""
Run "git tag $tag"

if (-not $SkipPush) {
    Run "git push $Remote $Branch"
    Run "git push $Remote $tag"
}

Write-Host ""
Write-Host "Release prepared: $tag" -ForegroundColor Green
Write-Host "JitPack: https://jitpack.io/#$githubRepository/$tag"
Write-Host "Dependency: implementation `"$jitpackDependency`""
