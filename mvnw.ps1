$ErrorActionPreference = "Continue"
$env:JAVA_HOME = (Get-ChildItem "$env:USERPROFILE\tools\jdk" -Directory | Select-Object -First 1).FullName
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
$mvnHome = (Get-ChildItem "$env:USERPROFILE\tools\maven" -Directory | Select-Object -First 1).FullName
$mvn = Join-Path $mvnHome "bin\mvn.cmd"
& $mvn @args
exit $LASTEXITCODE
