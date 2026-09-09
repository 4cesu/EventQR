param([string]$Device)

$adb = if ($Device) { $Device } else { "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" }

$count = 0
do {
    & $adb shell uiautomator dump /sdcard/ui.xml *> $null
    $xml = (& $adb shell cat /sdcard/ui.xml) -join "`n"
    $count++
} while (-not $xml -and $count -lt 3)

if (-not $xml) { throw "Could not dump UI. Is the emulator booted?" }

Write-Host "`n=== RESOURCE-IDS ==="
[regex]::Matches($xml, 'resource-id="(com\.thedavelopers\.eventqr:id/[^"]+)"') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique | ForEach-Object { Write-Host $_ }

Write-Host "`n=== TEXT ==="
[regex]::Matches($xml, 'text="([^"]+)"') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique | ForEach-Object { Write-Host "[$_]" }