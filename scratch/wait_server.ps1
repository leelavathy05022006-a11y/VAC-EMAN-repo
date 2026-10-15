$started = $false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 2
    try {
        $res = Invoke-WebRequest -Uri 'http://localhost:8080/index.html' -UseBasicParsing -TimeoutSec 2
        if ($res.StatusCode -eq 200) {
            Write-Host "Server is UP and READY on http://localhost:8080!"
            $started = $true
            break
        }
    } catch {
        Write-Host "Waiting for server ($($i * 2)s elapsed)..."
    }
}
if (-not $started) {
    Write-Host "Server did not respond within 60s"
}
