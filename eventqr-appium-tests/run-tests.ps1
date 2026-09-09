param(
    [string]$Test = "",
    [string]$JdkPath = "C:\Program Files\Java\jdk-25",
    [string]$MavenPath = ""
)

$ErrorActionPreference = "Stop"
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$emu = "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$avd = "Medium_Phone_API_36"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not $MavenPath) {
    $mvnOnPath = Get-Command mvn -ErrorAction SilentlyContinue
    $candidates = @()
    if ($mvnOnPath) {
        $candidates += $mvnOnPath.Source
    }
    $candidates += "C:\Users\matth\AppData\Local\Temp\opencode\apache-maven-3.9.6\bin\mvn.cmd"
    $MavenPath = $candidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
    if (-not $MavenPath) {
        throw "Maven not found. Pass -MavenPath or install Maven 3.8+"
    }
}

$env:JAVA_HOME = $JdkPath
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk

function Wait-Booted {
    for ($i = 0; $i -lt 60; $i++) {
        try {
            $state = & $adb get-state 2>$null
            if ($state -eq "device") {
                $boot = (& $adb shell getprop sys.boot_completed 2>$null)
                if ($boot -and $boot.Trim() -eq "1") { return $true }
            }
        } catch {}
        Start-Sleep -Seconds 5
    }
    return $false
}

function Ensure-Emulator {
    $booted = Wait-Booted
    if ($booted) { return }
    Write-Host "Starting emulator $avd ..."
    Start-Process $emu -ArgumentList "-avd", $avd, "-no-snapshot-save" -WindowStyle Minimized
    if (-not (Wait-Booted)) {
        throw "Emulator did not boot within 5 minutes"
    }
}

function Ensure-Appium {
    $port = Get-NetTCPConnection -LocalPort 4723 -State Listen -ErrorAction SilentlyContinue
    if ($port) { Write-Host "Appium already running on :4723"; return }
    Write-Host "Starting Appium on :4723 ..."
    Start-Process cmd -ArgumentList "/c", ("set `"ANDROID_HOME=$sdk`" && set `"ANDROID_SDK_ROOT=$sdk`" && appium > `"$root\appium.log`" 2>&1") -WindowStyle Hidden
    for ($i = 0; $i -lt 30; $i++) {
        if (Get-NetTCPConnection -LocalPort 4723 -State Listen -ErrorAction SilentlyContinue) {
            Write-Host "Appium ready"
            return
        }
        Start-Sleep -Seconds 2
    }
    throw "Appium did not start. See $root\appium.log"
}

Ensure-Emulator
Ensure-Appium

$args = "test"
if ($Test) { $args += " `"-Dtest=$Test`"" }

Write-Host "Running: mvn $args"
Set-Location $root
& $MavenPath $args
exit $LASTEXITCODE