param(
    [string]$Test = "",
    [string]$JdkPath = "C:\Program Files\Java\jdk-24",
    [string]$MavenPath = ""
)

$ErrorActionPreference = "Stop"
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not $MavenPath) {
    $mvnOnPath = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvnOnPath) { $MavenPath = $mvnOnPath.Source }
    if (-not $MavenPath -or -not (Test-Path $MavenPath)) {
        throw "Maven not found. Pass -MavenPath or install Maven 3.8+"
    }
}

$env:JAVA_HOME = $JdkPath
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk

function Get-AdbState {
    try { (& $adb devices 2>$null | Select-String "device$") -ne $null } catch { $false }
}

if (-not (Get-AdbState)) {
    throw "No Android device/emulator connected. Start your emulator in Android Studio, wait for full boot, then rerun."
}

Write-Host "Using connected device:"
& $adb devices

function Ensure-Appium {
    $port = Get-NetTCPConnection -LocalPort 4723 -State Listen -ErrorAction SilentlyContinue
    if ($port) { Write-Host "Appium already running on :4723"; return }
    $appiumCmd = Get-Command appium.cmd, appium -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $appiumCmd) {
        Write-Host "Appium not installed. Installing..."
        npm install -g appium 2>$null
    }
    Write-Host "Starting Appium on :4723 ..."
    $launcher = "appium"
    $argsline = ""
    if ($appiumCmd) { $launcher = $appiumCmd.Source }
    $startArgs = "/c", "`"$launcher`" > `"$root\appium.log`" 2>&1"
    Start-Process cmd -ArgumentList $startArgs -WindowStyle Hidden
    for ($i = 0; $i -lt 30; $i++) {
        if (Get-NetTCPConnection -LocalPort 4723 -State Listen -ErrorAction SilentlyContinue) {
            Write-Host "Appium ready"
            return
        }
        Start-Sleep -Seconds 2
    }
    throw "Appium did not start. See $root\appium.log"
}

Ensure-Appium

$mvnArgs = @("test")
if ($Test) { $mvnArgs += "-Dtest=$Test" }

Write-Host "Running: mvn $($mvnArgs -join ' ')"
& $MavenPath $mvnArgs
exit $LASTEXITCODE