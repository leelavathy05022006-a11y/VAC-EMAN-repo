$body = '{"message":"hello","locality":"Chennai"}'
try {
    $res = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/chatbot/message' -Method Post -Body $body -ContentType 'application/json'
    Write-Output ($res | ConvertTo-Json -Depth 6)
} catch {
    Write-Host "STATUS CODE:" $_.Exception.Response.StatusCode
    $stream = $_.Exception.Response.GetResponseStream()
    if ($stream) {
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Host "RESPONSE BODY:" $reader.ReadToEnd()
    }
}
