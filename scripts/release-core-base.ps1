param(
    [ValidateSet("patch", "minor", "major")]
    [string]$Bump = "patch",

    [string]$Repository = "wukuiqing49/AndroidCoreBase",

    [string]$GroupId = "com.github.wukuiqing49.AndroidCoreBase",

    [switch]$NoPush
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$versionFile = Join-Path $repoRoot "core_base/version.properties"
$gradlew = Join-Path $repoRoot "gradlew.bat"

function Invoke-Git {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Args)
    & git @Args
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Args -join ' ') failed."
    }
}

function Invoke-Gradle {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Args)
    & $gradlew @Args
    if ($LASTEXITCODE -ne 0) {
        throw "gradlew $($Args -join ' ') failed."
    }
}

Push-Location $repoRoot
try {
    $status = (& git status --porcelain)
    if ($LASTEXITCODE -ne 0) {
        throw "git status failed."
    }
    if ($status) {
        throw "Working tree is not clean. Commit or stash local changes before releasing."
    }

    $versionLine = Get-Content -LiteralPath $versionFile | Where-Object { $_ -match '^VERSION_NAME=' } | Select-Object -First 1
    if (-not $versionLine) {
        throw "VERSION_NAME not found in $versionFile"
    }

    $current = ($versionLine -replace '^VERSION_NAME=', '').Trim().TrimStart('v')
    $parts = $current.Split('.')
    if ($parts.Count -ne 3) {
        throw "VERSION_NAME must use X.Y.Z format. Current value: $current"
    }

    [int]$major = $parts[0]
    [int]$minor = $parts[1]
    [int]$patch = $parts[2]

    switch ($Bump) {
        "major" {
            $major += 1
            $minor = 0
            $patch = 0
        }
        "minor" {
            $minor += 1
            $patch = 0
        }
        "patch" {
            $patch += 1
        }
    }

    $next = "$major.$minor.$patch"
    $tag = "v$next"

    $existingTag = (& git tag --list $tag)
    if ($LASTEXITCODE -ne 0) {
        throw "git tag lookup failed."
    }
    if ($existingTag) {
        throw "Tag $tag already exists."
    }

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($versionFile, "VERSION_NAME=$next`n", $utf8NoBom)

    Invoke-Gradle ":core_base:publishReleasePublicationToMavenLocal" "-PPOM_GROUP_ID=$GroupId" "-PPOM_VERSION=$tag" "-PGITHUB_REPOSITORY=$Repository"

    Invoke-Git add "core_base/version.properties"
    Invoke-Git commit -m "release core_base $tag"
    Invoke-Git tag $tag

    if (-not $NoPush) {
        Invoke-Git push origin HEAD
        Invoke-Git push origin $tag
    }

    Write-Host "Released core_base $tag"
    Write-Host "JitPack: https://jitpack.io/#$Repository/$tag"
}
finally {
    Pop-Location
}
