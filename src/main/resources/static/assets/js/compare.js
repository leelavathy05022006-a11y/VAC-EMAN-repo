/**
 * Module 3: Medicine Search & Availability - Compare Tray & Matrix Manager (compare.js)
 * Manages sticky compare tray (max 3 pharmacies) and renders side-by-side comparison.
 */

const Compare = {
  selectedPharmacies: new Map(), // pharmacyId -> { name, medicineId, quantity, lat, lng }

  togglePharmacy(pharmacyId, pharmacyName, medicineId, quantity, lat, lng) {
    if (this.selectedPharmacies.has(pharmacyId)) {
      this.selectedPharmacies.delete(pharmacyId);
      this.updateTray();
      return false;
    }

    if (this.selectedPharmacies.size >= 3) {
      UI.showToast('You can compare a maximum of 3 pharmacies at a time.', 'warning');
      return null;
    }

    this.selectedPharmacies.set(pharmacyId, {
      id: pharmacyId,
      name: pharmacyName,
      medicineId,
      quantity,
      lat,
      lng
    });

    this.updateTray();
    return true;
  },

  removePharmacy(pharmacyId) {
    this.selectedPharmacies.delete(pharmacyId);
    this.updateTray();
    // Also uncheck checkbox in DOM if present
    const cb = document.querySelector(`.compare-checkbox[data-id="${pharmacyId}"]`);
    if (cb) cb.checked = false;
  },

  clearAll() {
    this.selectedPharmacies.clear();
    this.updateTray();
    document.querySelectorAll('.compare-checkbox').forEach(cb => cb.checked = false);
  },

  updateTray() {
    const tray = document.getElementById('compare-tray');
    const chipsContainer = document.getElementById('tray-chips-container');
    const countBadge = document.getElementById('tray-count');
    const compareBtn = document.getElementById('btn-open-compare');

    if (!tray) return;

    const count = this.selectedPharmacies.size;
    if (count > 0) {
      tray.classList.add('visible');
    } else {
      tray.classList.remove('visible');
    }

    if (countBadge) countBadge.textContent = count;
    if (compareBtn) {
      compareBtn.disabled = (count < 2);
    }

    if (chipsContainer) {
      chipsContainer.innerHTML = '';
      this.selectedPharmacies.forEach(item => {
        const chip = document.createElement('div');
        chip.className = 'tray-chip';
        chip.innerHTML = `
          <span>${item.name}</span>
          <button type="button" class="remove-btn" onclick="Compare.removePharmacy(${item.id})">
            <i class="bi bi-x-circle-fill"></i>
          </button>
        `;
        chipsContainer.appendChild(chip);
      });
    }
  },

  openCompareModal() {
    if (this.selectedPharmacies.size < 2) {
      UI.showToast('Please select at least 2 pharmacies to compare.', 'warning');
      return;
    }

    const firstItem = this.selectedPharmacies.values().next().value;
    const ids = Array.from(this.selectedPharmacies.keys());

    // Redirect to compare.html with query parameters
    const params = new URLSearchParams();
    ids.forEach(id => params.append('pharmacyIds', id));
    params.set('medicineId', firstItem.medicineId);
    params.set('quantity', firstItem.quantity);
    params.set('lat', firstItem.lat);
    params.set('lng', firstItem.lng);

    window.location.href = `compare.html?${params.toString()}`;
  }
};
