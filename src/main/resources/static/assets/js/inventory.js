let currentStatus = '';
let currentSearch = '';
let selectedPharmacyId = '';

document.addEventListener('DOMContentLoaded', () => {
  loadPharmaciesDropdown();
  loadSummary();
  loadAlerts();
  loadInventory();

  // Search input handler
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    let timeout = null;
    searchInput.addEventListener('input', (e) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => {
        currentSearch = e.target.value.trim();
        loadInventory();
      }, 300);
    });
  }

  // Status tab filters
  document.querySelectorAll('#status-filter .nav-link').forEach(tab => {
    tab.addEventListener('click', (e) => {
      e.preventDefault();
      document.querySelectorAll('#status-filter .nav-link').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      currentStatus = tab.getAttribute('data-status') || '';
      loadInventory();
    });
  });

  // Modal forms setup
  setupEditForm();
  setupStockInForm();
  setupAdjustForm();
});

async function loadPharmaciesDropdown() {
  const select = document.getElementById('pharmacy-select');
  if (!select) return;

  try {
    const list = await apiRequest('/pharmacies');
    if (Array.isArray(list) && list.length > 0) {
      select.innerHTML = `<option value="">My Pharmacy (Default Session: ID 101)</option>` +
        list.map(p => {
          const loc = p.locality ? ` - ${p.locality}` : '';
          const typeBadge = p.facilityType && p.facilityType !== 'PHARMACY' ? ` [${p.facilityType}]` : '';
          return `<option value="${p.id}">${escapeHtml(p.name)}${escapeHtml(loc)}${typeBadge} (ID: ${p.id})</option>`;
        }).join('');
    }
  } catch (err) {
    console.warn('Could not load pharmacy list for dropdown, keeping default session', err);
  }

  select.addEventListener('change', (e) => {
    selectedPharmacyId = e.target.value;
    loadSummary();
    loadAlerts();
    loadInventory();
  });
}

function refreshCurrentPharmacy() {
  loadSummary();
  loadAlerts();
  loadInventory();
  showToast('Inventory refreshed');
}

async function loadSummary() {
  try {
    let url = '/inventory/summary';
    if (selectedPharmacyId) url += `?pharmacyId=${encodeURIComponent(selectedPharmacyId)}`;
    const summary = await apiRequest(url);
    document.getElementById('card-total').innerText = summary.totalItems ?? 0;
    document.getElementById('card-instock').innerText = summary.inStockCount ?? 0;
    document.getElementById('card-lowstock').innerText = summary.lowStockCount ?? 0;
    document.getElementById('card-outstock').innerText = summary.outOfStockCount ?? 0;
    document.getElementById('card-expiring').innerText = summary.expiringCount ?? 0;
  } catch (err) {
    console.error('Failed to load summary', err);
  }
}

async function loadAlerts() {
  try {
    let url = '/inventory/alerts';
    if (selectedPharmacyId) url += `?pharmacyId=${encodeURIComponent(selectedPharmacyId)}`;
    const alerts = await apiRequest(url);
    const banner = document.getElementById('alerts-banner');
    const list = document.getElementById('alerts-list');
    if (alerts && alerts.length > 0) {
      banner.classList.remove('d-none');
      list.innerHTML = alerts.map(a => {
        let badgeClass = 'bg-warning text-dark';
        let alertLabel = a.stockStatus;
        if (a.stockStatus === 'EXPIRED') {
          badgeClass = 'bg-danger text-white';
          alertLabel = 'EXPIRED';
        } else if (a.stockStatus === 'OUT_OF_STOCK') {
          badgeClass = 'bg-danger text-white';
          alertLabel = 'OUT OF STOCK';
        } else if (a.expiringWithin30Days) {
          badgeClass = 'bg-warning text-dark';
          alertLabel = 'EXPIRING IN < 30 DAYS';
        } else if (a.stockStatus === 'LOW_STOCK') {
          badgeClass = 'bg-warning-subtle text-warning-emphasis border border-warning';
          alertLabel = `LOW STOCK (< ${a.lowStockThreshold})`;
        }
        return `<div class="mb-1 d-flex align-items-center gap-2 flex-wrap">
          <span class="badge ${badgeClass}">${alertLabel}</span>
          <strong>${escapeHtml(a.medicineName)}</strong>
          <span class="text-muted">(Available: ${a.availableQuantity}, Threshold: ${a.lowStockThreshold}, Expiry: ${a.expiryDate || 'N/A'})</span>
        </div>`;
      }).join('');
    } else {
      banner.classList.add('d-none');
    }
  } catch (err) {
    console.error('Failed to load alerts', err);
  }
}

async function loadInventory() {
  const tbody = document.getElementById('inventory-tbody');
  const emptyState = document.getElementById('empty-state');
  const reorderBanner = document.getElementById('reorder-banner');
  const reorderList = document.getElementById('reorder-list');
  const reorderBadge = document.getElementById('reorder-count-badge');

  tbody.innerHTML = renderSkeletonRows(12, 5);
  emptyState.classList.add('d-none');

  try {
    let url = `/inventory?size=50`;
    if (selectedPharmacyId) url += `&pharmacyId=${encodeURIComponent(selectedPharmacyId)}`;
    if (currentStatus) url += `&status=${encodeURIComponent(currentStatus)}`;
    if (currentSearch) url += `&q=${encodeURIComponent(currentSearch)}`;

    const pageData = await apiRequest(url);
    const rows = pageData.content || [];

    if (rows.length === 0) {
      tbody.innerHTML = '';
      emptyState.classList.remove('d-none');
      if (reorderBanner) reorderBanner.classList.add('d-none');
      return;
    }

    // Identify low stock items for Smart Reorder Suggestions
    const reorderItems = rows.filter(r => r.availableQuantity <= r.lowStockThreshold);
    if (reorderBanner && reorderList) {
      if (reorderItems.length > 0) {
        reorderBanner.classList.remove('d-none');
        if (reorderBadge) reorderBadge.innerText = `${reorderItems.length} Recommended`;
        reorderList.innerHTML = reorderItems.map(r => {
          const suggestedQty = Math.max((r.lowStockThreshold * 2) - r.availableQuantity, 10);
          return `
            <div class="card p-2 border shadow-sm bg-white d-flex flex-row align-items-center justify-content-between gap-3" style="min-width: 280px;">
              <div>
                <div class="fw-bold text-dark small">${escapeHtml(r.medicineName)}</div>
                <div class="text-muted" style="font-size: 0.75rem;">
                  Current: <span class="fw-semibold text-danger">${r.availableQuantity}</span> / Min: ${r.lowStockThreshold}
                </div>
              </div>
              <button class="btn btn-sm btn-primary-teal text-nowrap py-1 px-2" onclick="openStockIn(${r.id}, ${suggestedQty})">
                <i class="bi bi-cart-plus me-1"></i>Restock +${suggestedQty}
              </button>
            </div>
          `;
        }).join('');
      } else {
        reorderBanner.classList.add('d-none');
      }
    }

    tbody.innerHTML = rows.map(r => {
      const isOut = r.stockStatus === 'OUT_OF_STOCK';
      const isLow = r.stockStatus === 'LOW_STOCK';
      const isExpired = r.stockStatus === 'EXPIRED';
      const isExpiringSoon = Boolean(r.expiringWithin30Days);
      
      const rowClass = isOut ? 'row-out-of-stock' : isLow ? 'row-low-stock' : isExpired ? 'row-expired' : '';
      
      let statusBadge = '';
      if (isExpired) {
        statusBadge = '<span class="badge-expired"><i class="bi bi-calendar-x me-1"></i>EXPIRED</span>';
      } else if (isOut) {
        statusBadge = '<span class="badge-out-stock"><i class="bi bi-x-circle me-1"></i>OUT OF STOCK</span>';
      } else if (isLow) {
        statusBadge = `<span class="badge-low-stock"><i class="bi bi-exclamation-triangle me-1"></i>LOW STOCK (&lt; ${r.lowStockThreshold})</span>`;
      } else if (isExpiringSoon) {
        statusBadge = '<span class="badge bg-warning-subtle text-dark border border-warning"><i class="bi bi-hourglass-split me-1"></i>EXPIRING SOON</span>';
      } else {
        statusBadge = '<span class="badge-in-stock"><i class="bi bi-check2 me-1"></i>IN STOCK</span>';
      }

      const sourceBadge = r.dataSource === 'LIVE' ?
        `<span class="badge-live"><i class="bi bi-check-circle me-1"></i>Live</span>` :
        `<span class="badge-sample"><i class="bi bi-box me-1"></i>Demo Data</span>`;

      const confirmBtn = r.dataSource === 'SAMPLE' ?
        `<button class="btn btn-sm btn-outline-success me-1" onclick="confirmRow(${r.id})" title="Verify demo data to live stock"><i class="bi bi-check2"></i> Confirm</button>` : '';

      // Reorder suggestion shortcut for low/out of stock
      let reorderBtn = '';
      if (r.availableQuantity <= r.lowStockThreshold) {
        const suggested = Math.max((r.lowStockThreshold * 2) - r.availableQuantity, 10);
        reorderBtn = `<button class="btn btn-sm btn-outline-warning text-dark fw-semibold" title="Smart suggestion: restock ${suggested} units" onclick="openStockIn(${r.id}, ${suggested})"><i class="bi bi-stars text-warning"></i> +${suggested}</button>`;
      }

      const freshnessHtml = formatFreshness(r.lastUpdatedAt);

      return `
        <tr class="${rowClass}">
          <td>
            <div class="fw-bold">${escapeHtml(r.medicineName)}</div>
            <div class="small text-muted">${escapeHtml(r.genericName || '')}</div>
          </td>
          <td>${escapeHtml(r.strength || '')} <span class="badge bg-light text-dark ms-1">${escapeHtml(r.dosageForm || '')}</span></td>
          <td><code>${escapeHtml(r.batchNo || '-')}</code></td>
          <td class="fw-semibold">${r.quantity}</td>
          <td class="text-muted">${r.reservedQuantity}</td>
          <td class="fw-bold ${r.availableQuantity === 0 ? 'text-danger' : r.availableQuantity <= r.lowStockThreshold ? 'text-warning' : 'text-success'}">${r.availableQuantity}</td>
          <td>₹${parseFloat(r.unitPrice || 0).toFixed(2)}</td>
          <td>${r.expiryDate || 'N/A'}</td>
          <td>${statusBadge}</td>
          <td>${freshnessHtml}</td>
          <td>${sourceBadge}</td>
          <td class="text-end">
            <div class="btn-group">
              ${confirmBtn}
              ${reorderBtn}
              <button class="btn btn-sm btn-outline-primary" onclick="openStockIn(${r.id})" title="Stock in quantity">Stock in</button>
              <button class="btn btn-sm btn-outline-secondary" onclick="openAdjust(${r.id}, ${r.quantity})" title="Audit adjust quantity">Adjust</button>
              <button class="btn btn-sm btn-outline-info" onclick="openEdit(${r.id}, '${escapeHtml(r.batchNo || '')}', ${r.unitPrice || 0}, ${r.lowStockThreshold || 10}, '${r.expiryDate || ''}')" title="Edit batch / price / expiry">Edit</button>
              <button class="btn btn-sm btn-outline-dark" onclick="openHistory(${r.id})" title="Audit transaction history">History</button>
              <button class="btn btn-sm btn-outline-danger" onclick="deleteRow(${r.id})" title="Remove item">Remove</button>
            </div>
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    console.error('Failed to load inventory rows', err);
    tbody.innerHTML = `<tr><td colspan="12" class="text-center text-danger p-4">Error loading inventory. ${err.message}</td></tr>`;
  }
}

function formatFreshness(ts) {
  if (!ts) return `<span class="badge-freshness text-muted">Unknown</span>`;
  try {
    const d = new Date(ts);
    if (isNaN(d.getTime())) return `<span class="badge-freshness">${escapeHtml(ts)}</span>`;
    
    const now = new Date();
    const diffMs = now - d;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHours = Math.floor(diffMin / 60);
    const diffDays = Math.floor(diffHours / 24);

    let relative = '';
    if (diffSec < 60) relative = 'Just now';
    else if (diffMin < 60) relative = `${diffMin}m ago`;
    else if (diffHours < 24) relative = `${diffHours}h ago`;
    else relative = `${diffDays}d ago`;

    const formattedDate = d.toLocaleDateString('en-IN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    return `<span class="badge-freshness" title="Updated: ${d.toLocaleString()}"><i class="bi bi-clock-history me-1 text-primary"></i>${relative} <small class="text-muted">(${formattedDate})</small></span>`;
  } catch (e) {
    return `<span class="badge-freshness">${escapeHtml(ts)}</span>`;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str).replace(/[&<>"']/g, m => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  })[m]);
}

async function confirmRow(id) {
  try {
    await apiRequest(`/inventory/${id}/confirm`, { method: 'POST' });
    showToast('Row confirmed to Live stock');
    loadSummary();
    loadInventory();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function deleteRow(id) {
  if (!confirm('Are you sure you want to soft-remove this inventory item?')) return;
  try {
    await apiRequest(`/inventory/${id}`, { method: 'DELETE' });
    showToast('Inventory item removed');
    loadSummary();
    loadInventory();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

function openEdit(id, batchNo, price, threshold, expiry) {
  document.getElementById('edit-id').value = id;
  document.getElementById('edit-batchNo').value = batchNo || '';
  document.getElementById('edit-unitPrice').value = price || '';
  document.getElementById('edit-threshold').value = threshold || 10;
  document.getElementById('edit-expiryDate').value = expiry || '';
  const modal = new bootstrap.Modal(document.getElementById('editModal'));
  modal.show();
}

function setupEditForm() {
  const form = document.getElementById('editForm');
  if (!form) return;
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('edit-id').value;
    const body = {
      batchNo: document.getElementById('edit-batchNo').value,
      unitPrice: parseFloat(document.getElementById('edit-unitPrice').value),
      lowStockThreshold: parseInt(document.getElementById('edit-threshold').value),
      expiryDate: document.getElementById('edit-expiryDate').value
    };
    try {
      await apiRequest(`/inventory/${id}`, { method: 'PUT', body: JSON.stringify(body) });
      bootstrap.Modal.getInstance(document.getElementById('editModal')).hide();
      showToast('Inventory item updated successfully');
      loadSummary();
      loadInventory();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

function openStockIn(id, prefillQty = null) {
  document.getElementById('stockin-id').value = id;
  document.getElementById('stockin-qty').value = prefillQty ? prefillQty : '';
  document.getElementById('stockin-batchNo').value = '';
  document.getElementById('stockin-expiryDate').value = '';
  const modal = new bootstrap.Modal(document.getElementById('stockInModal'));
  modal.show();
}

function setupStockInForm() {
  const form = document.getElementById('stockInForm');
  if (!form) return;
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('stockin-id').value;
    const body = {
      quantity: parseInt(document.getElementById('stockin-qty').value),
      batchNo: document.getElementById('stockin-batchNo').value || null,
      expiryDate: document.getElementById('stockin-expiryDate').value || null
    };
    try {
      await apiRequest(`/inventory/${id}/stock-in`, { method: 'POST', body: JSON.stringify(body) });
      bootstrap.Modal.getInstance(document.getElementById('stockInModal')).hide();
      showToast('Stock-in completed successfully');
      loadSummary();
      loadAlerts();
      loadInventory();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

function openAdjust(id, currentQty) {
  document.getElementById('adjust-id').value = id;
  document.getElementById('adjust-newQty').value = currentQty;
  document.getElementById('adjust-reason').value = '';
  const modal = new bootstrap.Modal(document.getElementById('adjustModal'));
  modal.show();
}

function setupAdjustForm() {
  const form = document.getElementById('adjustForm');
  if (!form) return;
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('adjust-id').value;
    const body = {
      newQuantity: parseInt(document.getElementById('adjust-newQty').value),
      reason: document.getElementById('adjust-reason').value
    };
    try {
      await apiRequest(`/inventory/${id}/adjust`, { method: 'POST', body: JSON.stringify(body) });
      bootstrap.Modal.getInstance(document.getElementById('adjustModal')).hide();
      showToast('Stock count adjusted successfully');
      loadSummary();
      loadAlerts();
      loadInventory();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

async function openHistory(id) {
  const tbody = document.getElementById('history-tbody');
  tbody.innerHTML = '<tr><td colspan="7" class="text-center p-3">Loading stock transactions...</td></tr>';
  const modal = new bootstrap.Modal(document.getElementById('historyModal'));
  modal.show();

  try {
    const data = await apiRequest(`/inventory/${id}/transactions`);
    const txns = data.content || [];

    if (txns.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted p-3">No transactions found for this item</td></tr>';
      return;
    }

    tbody.innerHTML = txns.map(t => `
      <tr>
        <td><small class="text-nowrap">${formatTransactionDate(t.createdAt)}</small></td>
        <td><span class="badge ${t.txnType === 'STOCK_IN' ? 'bg-success' : t.txnType === 'ADJUSTMENT' ? 'bg-warning text-dark' : 'bg-secondary'}">${t.txnType}</span></td>
        <td class="${t.quantityChange >= 0 ? 'text-success fw-bold' : 'text-danger fw-bold'}">${t.quantityChange > 0 ? '+' : ''}${t.quantityChange}</td>
        <td class="fw-semibold">${t.quantityAfter}</td>
        <td class="text-muted">${t.reservedAfter}</td>
        <td><code>${t.referenceCode || '-'}</code></td>
        <td><small class="text-muted">${escapeHtml(t.remarks || '-')}</small></td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger p-3">${escapeHtml(err.message)}</td></tr>`;
  }
}

function formatTransactionDate(ts) {
  if (!ts) return '-';
  try {
    const d = new Date(ts);
    return isNaN(d.getTime()) ? ts : d.toLocaleString('en-IN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  } catch (e) {
    return ts;
  }
}
