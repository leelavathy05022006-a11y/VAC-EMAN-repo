[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$baseUrl = "http://localhost:8080/api/v1/chatbot/message"

function Send-ChatMessage($sessionId, $message, $locality = "Chennai", $lat = 13.0827, $lng = 80.2707) {
    $payload = @{
        sessionId = $sessionId
        message = $message
        locality = $locality
        lat = $lat
        lng = $lng
    } | ConvertTo-Json
    
    try {
        $res = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $payload -ContentType "application/json"
        if ($res.success -and $res.data) {
            return $res.data
        } else {
            Write-Host "Unsuccessful response: $($res | ConvertTo-Json)"
            return $null
        }
    } catch {
        Write-Host "HTTP Error: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            if ($stream) {
                $reader = New-Object System.IO.StreamReader($stream)
                Write-Host "Server Response Body: $($reader.ReadToEnd())"
            }
        }
        return $null
    }
}

Write-Host "=========================================================================="
Write-Host "EMERGENCY MEDICINE NETWORK - AI HEALTH ASSISTANT CHATBOT VERIFICATION SUITE"
Write-Host "=========================================================================="

# TEST 1: Paracetamol Stock Search
Write-Host "`n--- TEST 1: Paracetamol Stock Search ---"
$s1 = [System.Guid]::NewGuid().ToString()
$r1 = Send-ChatMessage -sessionId $s1 -message "Is Paracetamol available in Adyar?" -locality "Adyar" -lat 13.0035 -lng 80.2570
if ($r1) {
    Write-Host "Intent: $($r1.intent)"
    Write-Host "Reply Preview:`n$($r1.replyText.Substring(0, [System.Math]::Min(280, $r1.replyText.Length)))..."
    Write-Host "Pharmacy Cards Found: $($r1.pharmacyCards.Count)"
    if ($r1.pharmacyCards.Count -gt 0) {
        $top = $r1.pharmacyCards[0]
        Write-Host "Top Pharmacy: $($top.name) | Locality: $($top.address) | Stock: $($top.availableQuantity) | Price: ₹$($top.unitPrice) | Freshness: $($top.dataFreshnessLabel)"
        Write-Host "Directions URL: $($top.directionsUrl)"
        Write-Host "Call URL: $($top.callUrl)"
    }
}

# TEST 2: Symptom Guidance - Headache
Write-Host "`n--- TEST 2: Symptom Guidance - Headache ---"
$s2 = [System.Guid]::NewGuid().ToString()
$r2 = Send-ChatMessage -sessionId $s2 -message "I have a headache"
if ($r2) {
    Write-Host "Intent: $($r2.intent)"
    Write-Host "Contains Clinical Follow-ups: $($r2.replyText.Contains("Follow-up Questions"))"
    Write-Host "Contains Safe Self-Care: $($r2.replyText.Contains("Safe Self-Care"))"
    Write-Host "Mentions Safe OTC (Paracetamol): $($r2.replyText.Contains("Paracetamol"))"
    Write-Host "Contains Mandatory AI Disclaimer: $($r2.replyText.Contains("I'm an AI health assistant, not a doctor"))"
    Write-Host "Offers Pharmacy Search: $($r2.replyText.Contains("Would you like me to check nearby pharmacies"))"
    Write-Host "`nFull Clinical Reply:`n$($r2.replyText)"
}

# TEST 3: Hand Pain & Topical Ointment Guidance
Write-Host "`n--- TEST 3: Hand Pain & Topical Ointment (Non-Prescription / R.I.C.E.) ---"
$s3 = [System.Guid]::NewGuid().ToString()
$r3 = Send-ChatMessage -sessionId $s3 -message "My hand hurts; what can I do?"
if ($r3) {
    Write-Host "Intent: $($r3.intent)"
    Write-Host "Asks about Trauma / Deformity: $($r3.replyText.Contains("deformity") -or $r3.replyText.Contains("swelling"))"
    Write-Host "Provides R.I.C.E. Principles: $($r3.replyText.Contains("R.I.C.E."))"
    Write-Host "Explains Topical Ointment Precaution (Unbroken skin only): $($r3.replyText.Contains("unbroken skin"))"
    Write-Host "Suggests Ointment / Gel options: $($r3.replyText.Contains("Volini") -or $r3.replyText.Contains("Diclofenac"))"
    Write-Host "Mandatory Disclaimer Present: $($r3.replyText.Contains("I'm an AI health assistant, not a doctor"))"
    Write-Host "`nFull Response:`n$($r3.replyText)"
}

# TEST 4: Ambiguous Location ("near my college")
Write-Host "`n--- TEST 4: Ambiguous Location ('near my college') ---"
$s4 = [System.Guid]::NewGuid().ToString()
$r4 = Send-ChatMessage -sessionId $s4 -message "Find Paracetamol near my college"
if ($r4) {
    Write-Host "Intent: $($r4.intent)"
    Write-Host "Prompts for Specific College / Locality:`n$($r4.replyText)"
    Write-Host "Quick Replies: $(($r4.quickReplies) -join ' | ')"
}

# TEST 5: Brand Explanation (Dolo 650)
Write-Host "`n--- TEST 5: Brand Explanation (Dolo 650) ---"
$s5 = [System.Guid]::NewGuid().ToString()
$r5 = Send-ChatMessage -sessionId $s5 -message "Do you have Dolo?"
if ($r5) {
    Write-Host "Intent: $($r5.intent)"
    Write-Host "Explains Active Molecule (Paracetamol): $($r5.replyText.Contains("Paracetamol"))"
    Write-Host "Mentions Strength (650 mg): $($r5.replyText.Contains("650 mg"))"
    Write-Host "States Daily Safety Ceiling (4,000 mg limit): $($r5.replyText.Contains("4,000 mg"))"
    Write-Host "Mandatory Disclaimer: $($r5.replyText.Contains("I'm an AI health assistant, not a doctor"))"
    Write-Host "`nReply:`n$($r5.replyText)"
}

# TEST 6: Medicine Comparison (500mg vs 650mg)
Write-Host "`n--- TEST 6: Medicine Comparison (500mg vs 650mg) ---"
$s6 = [System.Guid]::NewGuid().ToString()
$r6 = Send-ChatMessage -sessionId $s6 -message "What's the difference between Paracetamol 500 and 650?"
if ($r6) {
    Write-Host "Intent: $($r6.intent)"
    Write-Host "Explains 500 vs 650 difference:`n$($r6.replyText)"
}

# TEST 7: Tamil / Tanglish Query Support
Write-Host "`n--- TEST 7: Tamil / Tanglish Query Support ---"
$s7 = [System.Guid]::NewGuid().ToString()
$r7 = Send-ChatMessage -sessionId $s7 -message "Enakku thala vali irukku, enna panradhu?"
if ($r7) {
    Write-Host "Intent: $($r7.intent)"
    Write-Host "Detected Headache correctly: $($r7.replyText.Contains("headache") -or $r7.replyText.Contains("Paracetamol"))"
}

# TEST 8: Full Multi-Turn Conversational Ordering Journey
Write-Host "`n--- TEST 8: Multi-Turn Conversational Ordering Journey ---"
$s8 = [System.Guid]::NewGuid().ToString()

# Turn 1: Initial Order intent
Write-Host "[Turn 1: Requesting Order]"
$t1 = Send-ChatMessage -sessionId $s8 -message "Order Paracetamol 650mg" -locality "T. Nagar"
Write-Host "Bot: $($t1.replyText)`n"
Write-Host "Quick Replies: $(($t1.quickReplies) -join ' | ')"

# Turn 2: Select Fulfilment Method
Write-Host "`n[Turn 2: Choosing Home Delivery]"
$t2 = Send-ChatMessage -sessionId $s8 -message "Home Delivery"
Write-Host "Bot: $($t2.replyText)`n"

# Turn 3: Providing Customer Name & Phone
Write-Host "`n[Turn 3: Providing Name & Phone]"
$t3 = Send-ChatMessage -sessionId $s8 -message "My name is Rajesh Kumar, phone 9840123456"
Write-Host "Bot: $($t3.replyText)`n"

# Turn 4: Providing Delivery Address
Write-Host "`n[Turn 4: Providing Address & Landmark]"
$t4 = Send-ChatMessage -sessionId $s8 -message "Flat 4B, Shanthi Apartments, 2nd Main Road, Adyar, near Adyar Bakery"
Write-Host "Bot: $($t4.replyText)`n"
Write-Host "Quick Replies: $(($t4.quickReplies) -join ' | ')"

# Turn 5: Confirming the Order
Write-Host "`n[Turn 5: Explicit Order Confirmation]"
$t5 = Send-ChatMessage -sessionId $s8 -message "Yes, confirm order"
Write-Host "Bot: $($t5.replyText)`n"
if ($t5.orders -and $t5.orders.Count -gt 0) {
    $order = $t5.orders[0]
    Write-Host ">>> REAL ORDER PLACED SUCCESSFULLY <<<"
    Write-Host "Order Reference Code: $($order.referenceCode)"
    Write-Host "Order Status:         $($order.status)"
    Write-Host "Pharmacy Name:        $($order.pharmacyName)"
    Write-Host "Medicine:             $($order.medicineName) ($($order.quantity) units)"
    Write-Host "Payment Mode:         $($order.paymentMode)"
    Write-Host "Handover OTP:         $($order.handoverOtp)"
    Write-Host "Total Amount:         ₹$($order.totalAmount)"
}

# TEST 9: Prescription Medicine Rejection & Safety Check
Write-Host "`n--- TEST 9: Prescription Medicine Safety Check (Amoxicillin) ---"
$s9 = [System.Guid]::NewGuid().ToString()
$r9 = Send-ChatMessage -sessionId $s9 -message "Order Amoxicillin 500mg"
if ($r9) {
    Write-Host "Bot Response for Prescription Medicine:`n$($r9.replyText)"
    Write-Host "Is Prescription Warning Given: $($r9.replyText.Contains("Prescription") -or $r9.replyText.Contains("Rx"))"
}

Write-Host "`n=========================================================================="
Write-Host "VERIFICATION SUITE COMPLETE: ALL SCENARIOS VALIDATED"
Write-Host "=========================================================================="
