/**
 * Module 3: Medicine Search & Availability - Main Search Page Controller (search.js)
 * Implements typeahead, search execution, filters, result cards, alternatives, and contact logging.
 */

let selectedMedicine = null;
let currentSearchData = null;
let searchDebounceTimer = null;

document.addEventListener('DOMContentLoaded', () => {
  // Setup UI event listeners immediately (non-blocking)
  setupTypeahead();
  setupQuantityStepper();
  setupFilters();
  loadRecentSearches();

  // Initialize Geolocation in background so it never blocks UI responsiveness
  if (window.Geo && typeof Geo.resolveLocation === 'function') {
    Geo.resolveLocation().catch(err => {
      console.warn('Geolocation initialization notice:', err);
    });
  }

  // Handle URL query parameters (e.g., from catalogue hand-off: search.html?medicineId=ID)
  const urlParams = new URLSearchParams(window.location.search);
  const paramMedId = urlParams.get('medicineId');
  if (paramMedId) {
    autoLoadMedicine(paramMedId);
  }
});

/**
 * Safely unwrap list data from Spring Data PageResponse or ApiResponse envelope
 */
function extractListFromResponse(res) {
  if (!res) return [];
  if (Array.isArray(res)) return res;
  if (res.content && Array.isArray(res.content)) return res.content;
  if (res.data) {
    if (Array.isArray(res.data)) return res.data;
    if (res.data.content && Array.isArray(res.data.content)) return res.data.content;
  }
  return [];
}

/**
 * Auto-selects medicine if passed via URL parameter
 */
async function autoLoadMedicine(medId) {
  try {
    const res = await Api.get('/medicines', { size: 50 });
    const items = extractListFromResponse(res);
    if (items.length > 0) {
      const match = items.find(m => String(m.id) === String(medId));
      if (match) {
        selectMedicineItem(match);
        executeSearch();
      }
    }
  } catch (err) {
    console.error('Failed to auto-load medicine:', err);
  }
}

/**
 * Setup debounced Medicine Typeahead with keyboard navigation
 */
function setupTypeahead() {
  const input = document.getElementById('medicine-search-input');
  const dropdown = document.getElementById('typeahead-dropdown');
  if (!input || !dropdown) return;

  let activeIndex = -1;

  async function performSearch(query) {
    if (!query) {
      dropdown.style.display = 'none';
      dropdown.innerHTML = '';
      return;
    }

    try {
      const res = await Api.get('/medicines', { q: query, size: 10 });
      const items = extractListFromResponse(res);

      if (items.length > 0) {
        renderTypeaheadDropdown(items);
      } else {
        dropdown.innerHTML = `
          <div class="p-3 text-muted text-center">
            <i class="bi bi-search me-1"></i> No medicines found matching "<strong>${escapeHtml(query)}</strong>"
          </div>`;
        dropdown.style.display = 'block';
      }
    } catch (err) {
      console.error('Typeahead search failed:', err);
    }
  }

  input.addEventListener('input', (e) => {
    const query = e.target.value.trim();
    clearTimeout(searchDebounceTimer);
    activeIndex = -1;

    // Reset selectedMedicine if user manually modifies the text away from selected
    if (selectedMedicine && selectedMedicine.name !== e.target.value) {
      selectedMedicine = null;
      const rxBanner = document.getElementById('rx-selected-banner');
      if (rxBanner) rxBanner.classList.add('d-none');
    }

    if (query.length === 0) {
      dropdown.style.display = 'none';
      dropdown.innerHTML = '';
      selectedMedicine = null;
      return;
    }

    searchDebounceTimer = setTimeout(() => {
      performSearch(query);
    }, 200);
  });

  // Re-open if input already has text on focus
  input.addEventListener('focus', () => {
    const query = input.value.trim();
    if (query.length > 0 && dropdown.children.length > 0) {
      dropdown.style.display = 'block';
    } else if (query.length > 0) {
      performSearch(query);
    }
  });

  // Keyboard navigation: Arrow Down, Arrow Up, Enter, Escape
  input.addEventListener('keydown', (e) => {
    const items = dropdown.querySelectorAll('.typeahead-item');
    if (!items.length || dropdown.style.display === 'none') {
      if (e.key === 'Enter') {
        e.preventDefault();
        executeSearch();
      }
      return;
    }

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      activeIndex = (activeIndex + 1) % items.length;
      updateActiveItem(items);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      activeIndex = (activeIndex - 1 + items.length) % items.length;
      updateActiveItem(items);
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (activeIndex >= 0 && activeIndex < items.length) {
        items[activeIndex].click();
      } else {
        dropdown.style.display = 'none';
        executeSearch();
      }
    } else if (e.key === 'Escape') {
      dropdown.style.display = 'none';
    }
  });

  function updateActiveItem(items) {
    items.forEach((item, idx) => {
      if (idx === activeIndex) {
        item.classList.add('active');
        item.scrollIntoView({ block: 'nearest' });
      } else {
        item.classList.remove('active');
      }
    });
  }

  // Close dropdown on outside click
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.typeahead-wrapper')) {
      dropdown.style.display = 'none';
    }
  });
}

function renderTypeaheadDropdown(items) {
  const dropdown = document.getElementById('typeahead-dropdown');
  if (!dropdown) return;
  dropdown.innerHTML = '';

  items.forEach(item => {
    const div = document.createElement('div');
    div.className = 'typeahead-item';
    div.innerHTML = `
      <div>
        <div class="med-title">${escapeHtml(item.name)}</div>
        <div class="med-subtitle">${escapeHtml(item.genericName || '')} • ${escapeHtml(item.dosageForm || 'Dosage')} • ${escapeHtml(item.strength || '')} ${item.manufacturer ? '• ' + escapeHtml(item.manufacturer) : ''}</div>
      </div>
      <div>
        ${item.requiresPrescription ? '<span class="badge bg-danger">Rx Required</span>' : '<span class="badge bg-success">OTC</span>'}
      </div>
    `;

    div.addEventListener('click', () => {
      selectMedicineItem(item);
      dropdown.style.display = 'none';
    });

    dropdown.appendChild(div);
  });

  dropdown.style.display = 'block';
}

function selectMedicineItem(item) {
  selectedMedicine = item;
  const input = document.getElementById('medicine-search-input');
  if (input) input.value = item.name;

  const rxBanner = document.getElementById('rx-selected-banner');
  if (rxBanner) {
    if (item.requiresPrescription) {
      rxBanner.classList.remove('d-none');
    } else {
      rxBanner.classList.add('d-none');
    }
  }
}

/**
 * Setup Quantity Stepper (+ / -)
 */
function setupQuantityStepper() {
  const qtyInput = document.getElementById('search-quantity');
  const btnMinus = document.getElementById('btn-qty-minus');
  const btnPlus = document.getElementById('btn-qty-plus');
  if (!qtyInput || !btnMinus || !btnPlus) return;

  btnMinus.addEventListener('click', () => {
    let val = parseInt(qtyInput.value) || 1;
    if (val > 1) qtyInput.value = val - 1;
  });

  btnPlus.addEventListener('click', () => {
    let val = parseInt(qtyInput.value) || 1;
    if (val < 1000) qtyInput.value = val + 1;
  });
}

/**
 * Setup Filters & Search Button
 */
function setupFilters() {
  const searchForm = document.getElementById('search-form');
  if (searchForm) {
    searchForm.addEventListener('submit', (e) => {
      e.preventDefault();
      executeSearch();
    });
  }

  document.getElementById('btn-use-location')?.addEventListener('click', async () => {
    if (window.Geo && typeof Geo.resolveLocation === 'function') {
      await Geo.resolveLocation();
      UI.showToast('Updated coordinates from device GPS.', 'success');
    }
  });

  document.getElementById('btn-save-manual-location')?.addEventListener('click', () => {
    const lat = parseFloat(document.getElementById('manual-lat')?.value);
    const lng = parseFloat(document.getElementById('manual-lng')?.value);
    if (!isNaN(lat) && !isNaN(lng) && window.Geo) {
      Geo.setManualLocation(lat, lng);
      UI.showToast('Manual coordinates updated.', 'success');
      executeSearch();
    }
  });

  document.getElementById('sort-select')?.addEventListener('change', () => {
    if (selectedMedicine) executeSearch();
  });

  document.getElementById('delivery-only-switch')?.addEventListener('change', () => {
    if (selectedMedicine) executeSearch();
  });

  document.getElementById('radius-select')?.addEventListener('change', () => {
    if (selectedMedicine) executeSearch();
  });
}

/**
 * Main Search Execution
 */
async function executeSearch() {
  const inputEl = document.getElementById('medicine-search-input');
  const inputQuery = inputEl ? inputEl.value.trim() : '';

  // Auto-resolve typed medicine if user hasn't clicked a dropdown item yet
  if (!selectedMedicine && inputQuery) {
    try {
      const res = await Api.get('/medicines', { q: inputQuery, size: 5 });
      const items = extractListFromResponse(res);
      if (items.length > 0) {
        const match = items.find(m => m.name.toLowerCase() === inputQuery.toLowerCase()) || items[0];
        selectMedicineItem(match);
      }
    } catch (err) {
      console.warn('Auto-resolving medicine failed:', err);
    }
  }

  if (!selectedMedicine) {
    UI.showToast('Please type a medicine name and select one from the suggestions.', 'warning');
    if (inputEl) inputEl.focus();
    return;
  }

  const quantity = parseInt(document.getElementById('search-quantity')?.value) || 1;
  const radiusKm = parseFloat(document.getElementById('radius-select')?.value) || 10.0;
  const sort = document.getElementById('sort-select')?.value || 'distance';
  const deliveryOnly = document.getElementById('delivery-only-switch')?.checked || false;

  // Use Geo location or fallback to Chennai default
  let loc = (window.Geo && Geo.currentLocation) ? Geo.currentLocation : null;
  if (!loc || !loc.lat || !loc.lng) {
    loc = { lat: 13.0035, lng: 80.2570, source: 'default' };
    if (window.Geo) Geo.currentLocation = loc;
  }

  const resultsContainer = document.getElementById('results-container');
  if (resultsContainer) {
    resultsContainer.innerHTML = renderSkeletonLoaders(3);
  }

  try {
    const res = await Api.get('/search/pharmacies', {
      medicineId: selectedMedicine.id,
      quantity,
      lat: loc.lat,
      lng: loc.lng,
      radiusKm,
      sort,
      deliveryOnly,
      page: 0,
      size: 20
    });

    currentSearchData = res;
    loadRecentSearches();

    // Check if matches or alternatives returned
    const matches = extractListFromResponse(res);

    if (matches && matches.length > 0) {
      renderResultCards(matches, quantity);
    } else if (res && (res.widerRadiusMatches || res.partialStockMatches || res.genericSubstitutes)) {
      renderAlternatives(res, quantity);
    } else {
      if (resultsContainer) {
        resultsContainer.innerHTML = `
          <div class="card-surface p-5 text-center">
            <i class="bi bi-geo-alt-fill text-muted" style="font-size: 3rem;"></i>
            <h4 class="mt-3 font-weight-bold">No Pharmacies Found</h4>
            <p class="text-muted">No approved pharmacies within ${radiusKm} km hold stock for ${escapeHtml(selectedMedicine.name)} (Qty: ${quantity}).</p>
            <button class="btn btn-teal mt-2" onclick="document.getElementById('radius-select').value='35'; executeSearch();">
              <i class="bi bi-arrows-angle-expand me-1"></i> Expand Radius to 35 km
            </button>
          </div>
        `;
      }
    }
  } catch (err) {
    if (resultsContainer) {
      resultsContainer.innerHTML = `
        <div class="card-surface p-4 text-center border-danger">
          <i class="bi bi-exclamation-triangle-fill text-danger fs-1"></i>
          <h5 class="text-danger mt-2">Search Failed</h5>
          <p class="text-muted">${escapeHtml(err.message || 'Unable to perform pharmacy search')}</p>
        </div>
      `;
    }
    UI.showToast(err.message || 'Search failed', 'danger');
  }
}

/**
 * Render Normal Search Matches
 */
function renderResultCards(matches, requestedQty) {
  const container = document.getElementById('results-container');
  if (!container) return;

  if (!matches || matches.length === 0) {
    container.innerHTML = `
      <div class="card-surface p-5 text-center">
        <h4>No Direct Matches</h4>
        <p class="text-muted">Try increasing your search radius or unchecking Delivery-only.</p>
      </div>
    `;
    return;
  }

  const isChecked = (id) => (window.Compare && Compare.selectedPharmacies && Compare.selectedPharmacies.has(id));

  let html = `
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h5 class="font-weight-bold mb-0 text-navy">
        Nearby Approved Pharmacies (${matches.length})
      </h5>
      <span class="text-muted small">Updated in real-time</span>
    </div>
  `;

  matches.forEach(item => {
    const delivery = item.delivery || {};
    const deliveryStatus = delivery.status || 'UNAVAILABLE';
    const isDeliveryFeasible = (deliveryStatus === 'AVAILABLE' || deliveryStatus === 'BUSY');
    const deliveryBadgeClass = deliveryStatus === 'AVAILABLE' ? 'badge-delivery-available' : (deliveryStatus === 'BUSY' ? 'badge-delivery-busy' : 'badge-delivery-unavailable');

    html += `
      <div class="result-card" id="pharmacy-card-${item.pharmacyId}">
        <div class="result-card-header">
          <div>
            <div class="d-flex align-items-center gap-2">
              <h4 class="pharmacy-title">${escapeHtml(item.name)}</h4>
              <span class="${item.dataSource === 'LIVE' ? 'badge-live' : 'badge-demo'}">${escapeHtml(item.dataSource || 'SAMPLE')}</span>
            </div>
            <div class="pharmacy-address">
              <i class="bi bi-geo-alt"></i> ${escapeHtml(item.address)}
            </div>
          </div>
          <div class="price-tag">
            <div class="total-price">${UI.formatCurrency(item.totalPrice)}</div>
            <div class="unit-price">${UI.formatCurrency(item.unitPrice)} / unit</div>
          </div>
        </div>

        ${item.requiresPrescription ? `
          <div class="prescription-warning">
            <i class="bi bi-exclamation-octagon-fill"></i> Prescription Required: A valid medical prescription must be verified before dispensing.
          </div>
        ` : ''}

        <div class="meta-grid">
          <div class="meta-item">
            <div class="meta-label">Distance / Travel</div>
            <div class="meta-val">${UI.formatDistance(item.distanceKm)} (${UI.formatEta(item.etaMinutes)})</div>
          </div>
          <div class="meta-item">
            <div class="meta-label">Available Stock</div>
            <div class="meta-val text-success font-weight-bold">${item.availableQuantity} units</div>
          </div>
          <div class="meta-item">
            <div class="meta-label">Operating Hours</div>
            <div class="meta-val">
              <span class="badge ${item.openNow ? 'bg-success' : 'bg-secondary'}">${item.openNow ? 'Open Now' : 'Closed'}</span>
              <small class="d-block text-muted">${item.opens24x7 ? '24x7' : `${item.openingTime || ''} - ${item.closingTime || ''}`}</small>
            </div>
          </div>
          <div class="meta-item">
            <div class="meta-label">Delivery Service</div>
            <div class="meta-val">
              <span class="${deliveryBadgeClass}">${deliveryStatus}</span>
              ${isDeliveryFeasible ? `<small class="d-block text-muted">Fee: ${UI.formatCurrency(delivery.fee || 0)}</small>` : ''}
            </div>
          </div>
        </div>

        <div class="card-actions">
          <div class="form-check mb-0">
            <input class="form-check-input compare-checkbox" type="checkbox" 
                   data-id="${item.pharmacyId}" 
                   id="compare-cb-${item.pharmacyId}"
                   ${isChecked(item.pharmacyId) ? 'checked' : ''}
                   onchange="handleCompareCheck(${item.pharmacyId}, '${escapeHtml(item.name)}', ${selectedMedicine.id}, ${requestedQty})">
            <label class="form-check-label font-weight-medium small" for="compare-cb-${item.pharmacyId}">
              Compare Pharmacy
            </label>
          </div>

          <div class="action-buttons-group">
            <button class="btn btn-sm btn-outline-secondary" onclick="handleCall(${item.pharmacyId}, ${selectedMedicine.id}, ${requestedQty})">
              <i class="bi bi-telephone-fill me-1"></i> Call
            </button>
            <button class="btn btn-sm btn-outline-success" onclick="handleWhatsApp(${item.pharmacyId}, ${selectedMedicine.id}, ${requestedQty})">
              <i class="bi bi-whatsapp me-1"></i> WhatsApp
            </button>
            <button class="btn btn-sm btn-outline-navy" onclick="handleNavigate(${item.pharmacyId})">
              <i class="bi bi-compass-fill me-1"></i> Navigate
            </button>
            <a href="pharmacy-details.html?id=${item.pharmacyId}&medicineId=${selectedMedicine.id}&quantity=${requestedQty}" class="btn btn-sm btn-outline-primary">
              <i class="bi bi-info-circle me-1"></i> Details
            </a>

            ${isDeliveryFeasible ? `
              <a href="/request/new.html?pharmacyId=${item.pharmacyId}&medicineId=${selectedMedicine.id}&quantity=${requestedQty}&fulfilment=DELIVERY" 
                 class="btn btn-sm btn-emergency">
                <i class="bi bi-lightning-charge-fill me-1"></i> Request Delivery
              </a>
            ` : ''}

            <a href="/request/new.html?pharmacyId=${item.pharmacyId}&medicineId=${selectedMedicine.id}&quantity=${requestedQty}&fulfilment=PICKUP" 
               class="btn btn-sm ${!isDeliveryFeasible ? 'btn-teal' : 'btn-outline-secondary'}">
              <i class="bi bi-bag-check-fill me-1"></i> Self-Pickup
            </a>
          </div>
        </div>
      </div>
    `;
  });

  container.innerHTML = html;
}

/**
 * Render Alternatives Response (When exact match is 0)
 */
function renderAlternatives(altData, requestedQty) {
  const container = document.getElementById('results-container');
  if (!container) return;

  let html = `
    <div class="alert alert-warning border-warning d-flex align-items-center mb-4" role="alert">
      <i class="bi bi-info-circle-fill fs-3 me-3 text-warning"></i>
      <div>
        <h5 class="alert-heading font-weight-bold mb-1">No Direct Matches Found</h5>
        <p class="mb-0 small">${escapeHtml(altData.message || 'We could not locate an approved pharmacy in the specified radius with full requested stock. Below are recommended alternatives:')}</p>
      </div>
    </div>
  `;

  // 1. Generic Substitutes
  if (altData.genericSubstitutes && altData.genericSubstitutes.length > 0) {
    html += `
      <div class="card-surface p-4 mb-4">
        <h5 class="font-weight-bold text-navy mb-2">
          <i class="bi bi-capsule me-2 text-teal"></i> Generic Substitutes (Same Active Compound)
        </h5>
        <p class="text-muted small mb-3">These medicines share the active molecule <strong>${escapeHtml(selectedMedicine.genericName || selectedMedicine.name)}</strong>. Please confirm with a doctor or pharmacist before substituting.</p>
        <div class="d-flex flex-wrap gap-2">
          ${altData.genericSubstitutes.map(sub => `
            <button class="btn btn-outline-navy btn-sm" onclick="selectAndSearchSubstitute(${sub.id}, '${escapeHtml(sub.name)}')">
              ${escapeHtml(sub.name)} (${escapeHtml(sub.strength || '')}) <i class="bi bi-arrow-right ms-1"></i>
            </button>
          `).join('')}
        </div>
      </div>
    `;
  }

  // 2. Partial Stock Matches
  if (altData.partialStockMatches && altData.partialStockMatches.length > 0) {
    html += `
      <h5 class="font-weight-bold text-navy mb-3">
        <i class="bi bi-box-seam me-2 text-warning"></i> Partial Stock Available Nearby
      </h5>
    `;
    altData.partialStockMatches.forEach(item => {
      html += renderAlternativeCard(item, requestedQty, 'Partial Stock: ' + item.availableQuantity + ' units available');
    });
  }

  // 3. Wider Radius Matches
  if (altData.widerRadiusMatches && altData.widerRadiusMatches.length > 0) {
    html += `
      <h5 class="font-weight-bold text-navy mt-4 mb-3">
        <i class="bi bi-arrows-angle-expand me-2 text-teal"></i> Extended Radius Pharmacies (Up to 50 km)
      </h5>
    `;
    altData.widerRadiusMatches.forEach(item => {
      html += renderAlternativeCard(item, requestedQty, 'Extended Radius: ' + item.distanceKm + ' km away');
    });
  }

  container.innerHTML = html;
}

function renderAlternativeCard(item, requestedQty, badgeText) {
  return `
    <div class="result-card mb-3">
      <div class="result-card-header">
        <div>
          <div class="d-flex align-items-center gap-2">
            <h5 class="pharmacy-title mb-0">${escapeHtml(item.name)}</h5>
            <span class="badge bg-warning text-dark">${escapeHtml(badgeText)}</span>
          </div>
          <div class="pharmacy-address">${escapeHtml(item.address)}</div>
        </div>
        <div class="price-tag">
          <div class="total-price">${UI.formatCurrency(item.unitPrice)} / unit</div>
        </div>
      </div>
      <div class="meta-grid">
        <div class="meta-item">
          <div class="meta-label">Distance</div>
          <div class="meta-val">${UI.formatDistance(item.distanceKm)} (${UI.formatEta(item.etaMinutes)})</div>
        </div>
        <div class="meta-item">
          <div class="meta-label">Available Qty</div>
          <div class="meta-val">${item.availableQuantity} units</div>
        </div>
        <div class="meta-item">
          <div class="meta-label">Operating Status</div>
          <div class="meta-val">${item.openNow ? '<span class="text-success font-weight-bold">Open</span>' : '<span class="text-muted">Closed</span>'}</div>
        </div>
      </div>
      <div class="d-flex justify-content-end gap-2 pt-2">
        <button class="btn btn-sm btn-outline-secondary" onclick="handleCall(${item.pharmacyId}, ${selectedMedicine.id}, ${requestedQty})">
          <i class="bi bi-telephone-fill me-1"></i> Call
        </button>
        <button class="btn btn-sm btn-outline-navy" onclick="handleNavigate(${item.pharmacyId})">
          <i class="bi bi-compass-fill me-1"></i> Navigate
        </button>
        <a href="/request/new.html?pharmacyId=${item.pharmacyId}&medicineId=${selectedMedicine.id}&quantity=${Math.min(item.availableQuantity, requestedQty)}&fulfilment=PICKUP" 
           class="btn btn-sm btn-teal">
          Self-Pickup (${item.availableQuantity} available)
        </a>
      </div>
    </div>
  `;
}

function selectAndSearchSubstitute(medId, name) {
  selectedMedicine = { id: medId, name: name };
  const input = document.getElementById('medicine-search-input');
  if (input) input.value = name;
  executeSearch();
}

/**
 * Handle Contact Actions
 */
async function handleCall(pharmacyId, medicineId, quantity) {
  try {
    const res = await Api.get(`/contact/${pharmacyId}/links`, { medicineId, quantity });
    const data = (res && res.data) ? res.data : res;
    if (data && data.callUrl) {
      await Api.post('/contact/log', { pharmacyId, medicineId, contactType: 'CALL' }).catch(() => {});
      window.location.href = data.callUrl;
    } else {
      UI.showToast('Direct calling link not available for this pharmacy.', 'info');
    }
  } catch (err) {
    UI.showToast(err.message || 'Failed to fetch contact details. Please log in.', 'danger');
  }
}

async function handleWhatsApp(pharmacyId, medicineId, quantity) {
  try {
    const res = await Api.get(`/contact/${pharmacyId}/links`, { medicineId, quantity });
    const data = (res && res.data) ? res.data : res;
    if (data && data.whatsappUrl) {
      await Api.post('/contact/log', { pharmacyId, medicineId, contactType: 'WHATSAPP' }).catch(() => {});
      window.open(data.whatsappUrl, '_blank');
    } else {
      UI.showToast('WhatsApp link not available for this pharmacy.', 'info');
    }
  } catch (err) {
    UI.showToast(err.message || 'Failed to open WhatsApp. Please log in.', 'danger');
  }
}

async function handleNavigate(pharmacyId) {
  const loc = (window.Geo && Geo.currentLocation) ? Geo.currentLocation : { lat: 13.0035, lng: 80.2570 };
  try {
    const res = await Api.get(`/navigation/${pharmacyId}`, {
      lat: loc.lat,
      lng: loc.lng,
      mode: 'driving'
    });
    const data = (res && res.data) ? res.data : res;
    if (data && data.directionsUrl) {
      window.open(data.directionsUrl, '_blank');
    }
  } catch (err) {
    UI.showToast(err.message || 'Navigation failed', 'danger');
  }
}

/**
 * Handle Compare Checkbox
 */
function handleCompareCheck(pharmacyId, name, medicineId, quantity) {
  if (!window.Compare) return;
  const loc = (window.Geo && Geo.currentLocation) ? Geo.currentLocation : { lat: 13.0035, lng: 80.2570 };
  const res = Compare.togglePharmacy(pharmacyId, name, medicineId, quantity, loc.lat, loc.lng);
  if (res === null) {
    const cb = document.getElementById(`compare-cb-${pharmacyId}`);
    if (cb) cb.checked = false;
  }
}

/**
 * Recent Searches Management
 */
async function loadRecentSearches() {
  try {
    const res = await Api.get('/search/history');
    const container = document.getElementById('recent-searches-list');
    const clearBtn = document.getElementById('btn-clear-history');
    if (!container) return;

    const list = extractListFromResponse(res);

    if (list && list.length > 0) {
      if (clearBtn) clearBtn.style.display = 'inline-block';
      container.innerHTML = list.map(h => `
        <span class="recent-chip" onclick="reRunSearch(${h.medicineId}, '${escapeHtml(h.queryText)}')">
          <i class="bi bi-clock-history"></i> ${escapeHtml(h.queryText)} (Qty: ${h.quantity})
        </span>
      `).join('');
    } else {
      if (clearBtn) clearBtn.style.display = 'none';
      container.innerHTML = '<span class="text-muted small">No recent searches</span>';
    }
  } catch (err) {
    console.warn('Could not load search history:', err);
  }
}

async function clearSearchHistory() {
  try {
    await Api.delete('/search/history');
    UI.showToast('Search history cleared.', 'info');
    loadRecentSearches();
  } catch (err) {
    UI.showToast('Failed to clear search history', 'danger');
  }
}

function reRunSearch(medId, name) {
  autoLoadMedicine(medId);
}

function renderSkeletonLoaders(count) {
  let skeletons = '';
  for (let i = 0; i < count; i++) {
    skeletons += `
      <div class="result-card mb-3">
        <div class="skeleton skeleton-title"></div>
        <div class="skeleton skeleton-text" style="width: 40%;"></div>
        <div class="skeleton skeleton-text" style="width: 90%; height: 50px; margin-top: 15px;"></div>
      </div>
    `;
  }
  return skeletons;
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
