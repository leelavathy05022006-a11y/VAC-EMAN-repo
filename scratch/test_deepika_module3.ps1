$baseUrl = 'http://localhost:8080/api/v1'

Write-Host '=========================================================================='
Write-Host '  MODULE 3 (DEEPIKA) - MEDICINE SEARCH AND AVAILABILITY VERIFICATION'
Write-Host '=========================================================================='

# STEP 1: Medicine Catalogue Lookup & Autocomplete
Write-Host "`n[TEST 1] Medicine Search and Autocomplete (GET /api/v1/medicines)"
$medId = 11
$medName = 'Paracetamol 500mg'
try {
    $medUrl = $baseUrl + '/medicines?q=Paracetamol'
    $medRes = Invoke-RestMethod -Uri $medUrl -Method Get
    if ($medRes.success -and $medRes.data.content.Count -gt 0) {
        $med = $medRes.data.content[0]
        $medId = $med.id
        $medName = $med.name
        Write-Host "-> Found Medicine ID: $($med.id) | Name: $($med.name) | Generic: $($med.genericName)"
        Write-Host "   Strength: $($med.strength) | Form: $($med.dosageForm) | Prescription Required: $($med.requiresPrescription)"
    } else {
        Write-Host '-> Direct match not returned, defaulting to Medicine ID 11'
    }
} catch {
    Write-Host "Error in Test 1: $($_.Exception.Message)"
}

# STEP 2: Proximity Search with Haversine Distance (Adyar, Chennai: 13.0035, 80.2570)
Write-Host "`n[TEST 2] Proximity Search with Haversine Distance Calculation (Radius: 10 km)"
$pharmacyIds = @()
try {
    $searchUrl = $baseUrl + '/search/pharmacies?medicineId=' + $medId + '&quantity=2&lat=13.0035&lng=80.2570&radiusKm=10&sort=distance'
    $searchRes = Invoke-RestMethod -Uri $searchUrl -Method Get
    if ($searchRes.success -and $searchRes.data.content.Count -gt 0) {
        $matches = $searchRes.data.content
        Write-Host "-> Successfully matched $($matches.Count) pharmacies within 10km radius:"
        for ($i = 0; $i -lt [System.Math]::Min(3, $matches.Count); $i++) {
            $p = $matches[$i]
            $pharmacyIds += $p.pharmacyId
            Write-Host "   #$($i+1) [$($p.name)] (ID: $($p.pharmacyId))"
            Write-Host "       Distance: $($p.distanceKm) km | Available Stock: $($p.availableQuantity) units | Unit Price: Rs.$($p.unitPrice)"
            Write-Host "       Locality: $($p.address)"
            Write-Host "       Delivery Available: $($p.delivery.available) (Fee: Rs.$($p.delivery.deliveryFee)) | 24x7 Open: $($p.opens24x7)"
            Write-Host "       Google Maps: $($p.navigationUrl)"
        }
    } else {
        Write-Host "-> No matches found. Response: $($searchRes.message)"
    }
} catch {
    Write-Host "Error in Test 2: $($_.Exception.Message)"
}

# STEP 3: Filter by Delivery Only
Write-Host "`n[TEST 3] Search Filtering by Delivery Only (deliveryOnly=true)"
try {
    $deliverySearchUrl = $baseUrl + '/search/pharmacies?medicineId=' + $medId + '&quantity=1&lat=13.0035&lng=80.2570&deliveryOnly=true'
    $delRes = Invoke-RestMethod -Uri $deliverySearchUrl -Method Get
    if ($delRes.success -and $delRes.data.content) {
        $delMatches = $delRes.data.content
        Write-Host "-> Found $($delMatches.Count) pharmacies offering home delivery."
        foreach ($dm in $delMatches | Select-Object -First 2) {
            Write-Host "   - $($dm.name) - Distance: $($dm.distanceKm) km | Delivery Fee: Rs.$($dm.delivery.deliveryFee)"
        }
    }
} catch {
    Write-Host "Error in Test 3: $($_.Exception.Message)"
}

# STEP 4: Pharmacy Details & Operating Hours
if ($pharmacyIds.Count -gt 0) {
    $targetPharmId = $pharmacyIds[0]
    Write-Host "`n[TEST 4] Detailed Pharmacy Profile and Operating Hours (Pharmacy ID: $targetPharmId)"
    try {
        $detailUrl = $baseUrl + '/pharmacies/' + $targetPharmId + '/details?medicineId=' + $medId + '&quantity=1&lat=13.0035&lng=80.2570'
        $detailRes = Invoke-RestMethod -Uri $detailUrl -Method Get
        if ($detailRes.success -and $detailRes.data) {
            $d = $detailRes.data
            Write-Host "-> Pharmacy Name:      $($d.name)"
            Write-Host "   Address:            $($d.address)"
            Write-Host "   Phone Number:       $($d.contact.phone)"
            Write-Host "   Operating Hours:    $($d.openingTime) - $($d.closingTime)"
            Write-Host "   Currently Open:     $($d.openNow)"
            Write-Host "   Stock Status:       $($d.stockStatus) ($($d.availableQuantity) units available)"
            Write-Host "   Unit Price:         Rs.$($d.unitPrice)"
            Write-Host "   Call Link:          tel:$($d.contact.phone)"
            Write-Host "   Google Maps Link:   $($d.navigationUrl)"
        }
    } catch {
        Write-Host "Error in Test 4: $($_.Exception.Message)"
    }
}

# STEP 5: Multi-Pharmacy Side-by-Side Comparison Matrix
if ($pharmacyIds.Count -ge 2) {
    $pId1 = $pharmacyIds[0]
    $pId2 = $pharmacyIds[1]
    Write-Host "`n[TEST 5] Multi-Pharmacy Comparison Matrix (IDs: $pId1, $pId2)"
    try {
        $compareUrl = $baseUrl + '/search/compare?pharmacyIds=' + $pId1 + ',' + $pId2 + '&medicineId=' + $medId + '&quantity=2&lat=13.0035&lng=80.2570'
        $compRes = Invoke-RestMethod -Uri $compareUrl -Method Get
        if ($compRes.success -and $compRes.data) {
            $cd = $compRes.data
            Write-Host "-> Compared Medicine ID: $($cd.medicineId)"
            Write-Host "   Requested Quantity:   $($cd.quantity) units"
            Write-Host '   Side-by-Side Comparison Cards:'
            foreach ($card in $cd.pharmacies) {
                Write-Host '   --------------------------------------------------------'
                Write-Host "   Pharmacy Name:   $($card.name) (ID: $($card.pharmacyId))"
                Write-Host "   Distance:        $($card.distanceKm) km"
                Write-Host "   Stock Available: $($card.availableQuantity) units (Status: $($card.stockStatus))"
                Write-Host "   Unit Price:      Rs.$($card.unitPrice) | Total for 2 units: Rs.$($card.totalPrice)"
                Write-Host "   Delivery:        Fee Rs.$($card.delivery.deliveryFee) (Available: $($card.delivery.available))"
            }
        }
    } catch {
        Write-Host "Error in Test 5: $($_.Exception.Message)"
    }
}

# STEP 6: Zero Direct Match Fallback & Alternative Suggestions
Write-Host "`n[TEST 6] Zero Match Fallback and Automated Alternatives (High Qty: 900 units)"
try {
    $zeroUrl = $baseUrl + '/search/pharmacies?medicineId=' + $medId + '&quantity=900&lat=13.0035&lng=80.2570&radiusKm=2.0'
    $zeroRes = Invoke-RestMethod -Uri $zeroUrl -Method Get
    if ($zeroRes.success) {
        Write-Host '-> Response Status: 200 OK (Graceful zero-match fallback)'
        Write-Host "-> Message: $($zeroRes.message)"
        if ($zeroRes.data.widerRadiusMatches) {
            Write-Host "   Wider Radius Suggestions Found: $($zeroRes.data.widerRadiusMatches.Count) pharmacies"
            foreach ($wm in $zeroRes.data.widerRadiusMatches | Select-Object -First 2) {
                Write-Host "   - $($wm.name) - Distance: $($wm.distanceKm) km | Available: $($wm.availableQuantity) units"
            }
        }
        if ($zeroRes.data.partialStockMatches) {
            Write-Host "   Partial Stock Suggestions Found: $($zeroRes.data.partialStockMatches.Count) pharmacies"
            foreach ($pm in $zeroRes.data.partialStockMatches | Select-Object -First 2) {
                Write-Host "   - $($pm.name) - Distance: $($pm.distanceKm) km | Partial Stock: $($pm.availableQuantity) units"
            }
        }
    }
} catch {
    Write-Host "Error in Test 6: $($_.Exception.Message)"
}

Write-Host "`n=========================================================================="
Write-Host '  MODULE 3 (DEEPIKA) - ALL SEARCH AND AVAILABILITY TESTS COMPLETED'
Write-Host "=========================================================================="
