$baseUrl = "http://localhost:8080/api/v1/auth/login"

$accounts = @(
    @{ Role = "ADMIN";    Email = "admin@demo.in";    Password = "Admin@123" },
    @{ Role = "PATIENT";  Email = "patient@demo.in";  Password = "Patient@123" },
    @{ Role = "HOSPITAL"; Email = "hospital@demo.in"; Password = "Hospital@123" },
    @{ Role = "PHARMACY"; Email = "pharmacy@demo.in"; Password = "Pharmacy@123" },
    @{ Role = "DELIVERY"; Email = "delivery@demo.in"; Password = "Delivery@123" }
)

Write-Host "=========================================================================="
Write-Host "  VERIFYING DEFAULT CREDENTIALS ACROSS ALL 5 ROLES"
Write-Host "=========================================================================="

foreach ($acc in $accounts) {
    $payload = @{
        email = $acc.Email
        password = $acc.Password
    } | ConvertTo-Json

    try {
        $res = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $payload -ContentType "application/json"
        if ($res.success -and $res.data) {
            Write-Host "[OK] $($acc.Role.PadRight(10)) -> User: $($res.data.fullName.PadRight(32)) | Role: $($res.data.role.PadRight(15)) | Email: $($res.data.email)"
        } else {
            Write-Host "[FAILED] $($acc.Role) -> $($res.message)"
        }
    } catch {
        Write-Host "[ERROR] $($acc.Role) -> $($_.Exception.Message)"
    }
}

Write-Host "=========================================================================="
