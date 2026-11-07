#Requires -Version 5.1
<#
.SYNOPSIS
    Wrapper Maven cho Blogin - tu dong tim Maven & JDK tren may.

.DESCRIPTION
    Script nay giup build plugin ma khong can cai dat Maven thu cong.
    Ban co the dung truc tiep 'mvn clean package' neu da co Maven trong PATH.

.EXAMPLE
    .\mvnw.ps1 clean package
#>

$ErrorActionPreference = "Stop"

# --- Tim Maven ---
function Find-Maven {
    # 1) Maven wrapper chuan (mvnw.cmd / .mvn/wrapper)
    $wrapper = Join-Path $PSScriptRoot "mvnw.cmd"
    if (Test-Path $wrapper) {
        return @{ Type = "Wrapper"; Path = $wrapper }
    }

    # 2) Maven trong PATH
    $cmd = Get-Command mvn -ErrorAction SilentlyContinue
    if ($cmd) {
        return @{ Type = "Path"; Path = $cmd.Source }
    }

    # 3) Cac thu muc Maven thong dung tren Windows
    $candidates = @(
        "$env:USERPROFILE\tools\maven",
        "$env:USERPROFILE\scoop\apps\maven",
        "$env:ProgramFiles\Apache\maven",
        "${env:ProgramFiles(x86)}\Apache\maven",
        "C:\Program Files\Apache\maven",
        "C:\ProgramData\chocolatey\lib\maven"
    )
    foreach ($dir in $candidates) {
        if (-not $dir) { continue }
        if (Test-Path $dir) {
            $found = Get-ChildItem $dir -Directory -ErrorAction SilentlyContinue |
                     Where-Object { Test-Path (Join-Path $_.FullName "bin\mvn.cmd") } |
                     Select-Object -First 1
            if (-not $found -and (Test-Path (Join-Path $dir "bin\mvn.cmd"))) {
                return @{ Type = "Dir"; Path = (Join-Path $dir "bin\mvn.cmd") }
            }
            if ($found) {
                return @{ Type = "Dir"; Path = (Join-Path $found.FullName "bin\mvn.cmd") }
            }
        }
    }
    return $null
}

# --- Tim JDK 17+ ---
function Find-JavaHome {
    if ($env:JAVA_HOME -and (Test-Path $env:JAVA_HOME)) {
        return $env:JAVA_HOME
    }

    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) {
        # java.exe -> ...\bin\java.exe -> JAVA_HOME = ...
        $binDir = Split-Path $cmd.Source -Parent
        if ((Split-Path $binDir -Parent) -and (Test-Path (Join-Path (Split-Path $binDir -Parent) "bin\java.exe"))) {
            return (Split-Path $binDir -Parent)
        }
    }

    $candidates = @(
        "$env:USERPROFILE\tools\jdk",
        "$env:ProgramFiles\Java",
        "$env:ProgramFiles\Eclipse Adoptium",
        "$env:ProgramFiles\Microsoft\jdk",
        "C:\Program Files\Java"
    )
    foreach ($dir in $candidates) {
        if (-not $dir -or -not (Test-Path $dir)) { continue }
        $found = Get-ChildItem $dir -Directory -ErrorAction SilentlyContinue |
                 Where-Object { $_.Name -match "^(jdk|java|temurin|zulu|corretto)-?(\d+)?$" } |
                 Sort-Object { [int]($_.Name -replace '\D', '') } -Descending |
                 Select-Object -First 1
        if ($found) { return $found.FullName }
    }
    return $null
}

# --- Chuan bi moi truong ---
$maven = Find-Maven
if (-not $maven) {
    Write-Host "✖ Khong tim thay Maven." -ForegroundColor Red
    Write-Host "  Cai Maven 3.9+ hoac them 'mvn' vao PATH, roi chay lai."
    exit 1
}

$javaHome = Find-JavaHome
if ($javaHome) {
    $env:JAVA_HOME = $javaHome
    $env:PATH = "$javaHome\bin;" + $env:PATH
    Write-Host "✔ JAVA_HOME = $javaHome" -ForegroundColor DarkGray
} else {
    Write-Host "⚠ Khong tim thay JAVA_HOME - dung he thong mac dinh." -ForegroundColor Yellow
}

Write-Host "✔ Maven = $($maven.Path)" -ForegroundColor DarkGray

# --- Chay Maven ---
if ($maven.Type -eq "Wrapper") {
    & $maven.Path @args
} else {
    & $maven.Path @args
}
exit $LASTEXITCODE
