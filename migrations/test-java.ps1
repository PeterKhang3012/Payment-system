param([ValidateSet('tuition-management','payment-service','User')][string]$Service)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
# Load values into this process only; never echo credentials.
foreach ($line in Get-Content -LiteralPath (Join-Path $taskRoot '.env')) {
    if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2].Trim().Trim('"').Trim("'"), 'Process')
    }
}
$database = switch ($Service) { 'User' {'UserDB'} 'tuition-management' {'TuitionDB'} 'payment-service' {'PaymentDB'} }
$env:SPRING_DATASOURCE_URL = "jdbc:sqlserver://localhost:1434;databaseName=$database;encrypt=true;trustServerCertificate=true"
$env:JWT_PUBLIC_KEY_PATH = (Join-Path $taskRoot 'keys/public.pem').Replace('\','/')
$env:SPRING_DATA_REDIS_HOST = 'localhost'
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'validate'
Push-Location (Join-Path $taskRoot "Backend/$Service")
try { & mvn -q test; $taskExit = $LASTEXITCODE } finally { Pop-Location }
exit $taskExit
