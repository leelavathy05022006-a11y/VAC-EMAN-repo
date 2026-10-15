$body = @{ email = "patient@demo.in"; password = "Patient@123" } | ConvertTo-Json
$res = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -Body $body -ContentType "application/json" -SessionVariable websess

Write-Host "1. Login API Call:"
Write-Host "   Success:     $($res.success)"
Write-Host "   User:        $($res.data.fullName)"
Write-Host "   Role:        $($res.data.role)"
Write-Host "   RedirectUrl: $($res.data.redirectUrl)"

$me = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/me" -Method Get -WebSession $websess
Write-Host "`n2. Authenticated Session Check (/api/v1/auth/me):"
Write-Host "   Logged In User: $($me.data.fullName)"
Write-Host "   Active Role:    $($me.data.role)"

$dash = Invoke-WebRequest -Uri "http://localhost:8080/dashboard/patient.html" -Method Get -WebSession $websess -UseBasicParsing
Write-Host "`n3. Accessing Target Dashboard (/dashboard/patient.html):"
Write-Host "   HTTP Status Code: $($dash.StatusCode)"
Write-Host "   Content Size:     $($dash.RawContentLength) bytes"
