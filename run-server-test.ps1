$ErrorActionPreference = "Stop"
$srv = "$env:USERPROFILE\tools\server\test1211"
$jdk21 = "$env:USERPROFILE\tools\jdk\jdk-21.0.13+9"

$p = New-Object System.Diagnostics.Process
$p.StartInfo.FileName = "$jdk21\bin\java.exe"
$p.StartInfo.Arguments = "-Xms1G -Xmx2G -Dfile.encoding=UTF-8 -jar paper.jar nogui"
$p.StartInfo.WorkingDirectory = $srv
$p.StartInfo.UseShellExecute = $false
$p.StartInfo.RedirectStandardOutput = $true
$p.StartInfo.RedirectStandardError = $true
$p.StartInfo.RedirectStandardInput = $true
$p.StartInfo.StandardOutputEncoding = [System.Text.Encoding]::UTF8
$p.StartInfo.StandardErrorEncoding = [System.Text.Encoding]::UTF8

$log = New-Object System.Collections.Concurrent.ConcurrentQueue[string]
$sw = [System.Diagnostics.Stopwatch]::StartNew()
$p.Start() | Out-Null

$outTask = $p.StandardOutput.ReadToEndAsync()
$errTask = $p.StandardError.ReadToEndAsync()

# Send console commands over time
$cmds = @{
  25  = "blogin help"
  30  = "plugins"
  35  = "blogin reload"
  40  = "stop"
}
$sent = @{}
while ($sw.Elapsed.TotalSeconds -lt 120) {
  Start-Sleep -Milliseconds 500
  $t = $sw.Elapsed.TotalSeconds
  foreach ($k in $cmds.Keys) {
    if (-not $sent.ContainsKey($k) -and $t -ge $k) {
      $sent[$k] = $true
      try { $p.StandardInput.WriteLine($cmds[$k]); $p.StandardInput.Flush() } catch {}
    }
  }
  if ($p.HasExited) { break }
}
Start-Sleep -Seconds 5
if (-not $p.HasExited) { try { $p.StandardInput.WriteLine("stop"); $p.StandardInput.Flush() } catch {} }
$p.WaitForExit(60000) | Out-Null
if (-not $p.HasExited) { $p.Kill() }

$out = $p.StandardOutput.ReadToEnd()
$err = $p.StandardError.ReadToEnd()
$all = $out + "`n===STDERR===`n" + $err
$all | Set-Content "$srv\run.log" -Encoding UTF8
Write-Output "Exit code: $($p.ExitCode)"
Write-Output "Log length: $($all.Length)"
