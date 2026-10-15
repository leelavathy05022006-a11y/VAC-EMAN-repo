// Emergency Medicine Request Wizard Logic (4 Steps)

const requestWizard = {
  currentStep: 1,
  pharmacyId: 1,
  medicinesList: [],
  selectedItems: [], // { medicineId, name, unitPrice, quantity, requiresPrescription }
  fulfilmentType: 'DELIVERY',
  deliveryFee: 50.00,
  uploadedPrescriptionId: null,

  async init() {
    this.parseUrlParams();
    await this.loadMedicines();
    this.bindEvents();
    this.renderStep();
    this.calculateTotals();
  },

  parseUrlParams() {
    const params = new URLSearchParams(window.location.search);
    if (params.get('pharmacyId')) {
      this.pharmacyId = parseInt(params.get('pharmacyId'), 10);
    }
    if (params.get('fulfilment')) {
      this.fulfilmentType = params.get('fulfilment').toUpperCase();
    }
  },

  async loadMedicines() {
    // Standard baseline catalog fallback
    this.medicinesList = [
      { id: 1, name: 'Paracetamol 650mg', price: 35.50, requiresPrescription: false, form: 'Tablet 650mg' },
      { id: 11, name: 'Paracetamol 500mg', price: 28.50, requiresPrescription: false, form: 'Tablet 500mg' },
      { id: 2, name: 'Salbutamol Inhaler', price: 145.00, requiresPrescription: true, form: 'Inhaler 100mcg' },
      { id: 3, name: 'Hydrocortisone Sodium Succinate 100mg', price: 95.00, requiresPrescription: true, form: 'Vial 100mg' },
      { id: 4, name: 'Adrenaline (Epinephrine) Injection', price: 185.00, requiresPrescription: true, form: 'Injection 1mg/ml' },
      { id: 5, name: 'Amoxicillin 500mg', price: 65.00, requiresPrescription: true, form: 'Capsule 500mg' }
    ];

    try {
      const res = await api.get('/medicines?size=100');
      const items = (res && res.content) ? res.content : (Array.isArray(res) ? res : (res && res.data && res.data.content) ? res.data.content : (res && res.data) ? res.data : []);
      if (items && items.length > 0) {
        this.medicinesList = items.map(m => ({
          id: m.id,
          name: m.name,
          price: m.unitPrice || 35.00,
          requiresPrescription: m.requiresPrescription === true,
          form: `${m.dosageForm || 'Unit'} ${m.strength || ''}`.trim()
        }));
      }
    } catch (e) {
      console.warn('Using baseline medicines list:', e);
    }

    // Check prefill from URL
    const params = new URLSearchParams(window.location.search);
    const prefillMedId = params.get('medicineId') ? parseInt(params.get('medicineId'), 10) : null;
    const prefillQty = params.get('quantity') ? parseInt(params.get('quantity'), 10) : 1;

    if (prefillMedId) {
      let match = this.medicinesList.find(m => m.id === prefillMedId);
      if (!match) {
        try {
          const single = await api.get(`/medicines/${prefillMedId}`);
          const mData = (single && single.data) ? single.data : single;
          if (mData && mData.id) {
            match = {
              id: mData.id,
              name: mData.name,
              price: mData.unitPrice || 35.00,
              requiresPrescription: mData.requiresPrescription === true,
              form: `${mData.dosageForm || 'Unit'} ${mData.strength || ''}`.trim()
            };
            this.medicinesList.unshift(match);
          }
        } catch (ignored) {}
      }

      if (match) {
        this.selectedItems = [{
          medicineId: match.id,
          name: match.name,
          unitPrice: match.price,
          quantity: Math.min(100, Math.max(1, prefillQty)),
          requiresPrescription: match.requiresPrescription
        }];
      }
    }

    if (this.selectedItems.length === 0 && this.medicinesList.length > 0) {
      const item = this.medicinesList[0];
      this.selectedItems.push({
        medicineId: item.id,
        name: item.name,
        unitPrice: item.price,
        quantity: 1,
        requiresPrescription: item.requiresPrescription
      });
    }

    this.renderItemsList();
  },

  bindEvents() {
    // Stepper next/prev
    document.querySelectorAll('.btn-next-step').forEach(btn => {
      btn.addEventListener('click', () => this.nextStep());
    });
    document.querySelectorAll('.btn-prev-step').forEach(btn => {
      btn.addEventListener('click', () => this.prevStep());
    });

    // Fulfilment radio
    document.querySelectorAll('input[name="fulfilmentType"]').forEach(radio => {
      radio.addEventListener('change', (e) => {
        this.fulfilmentType = e.target.value;
        this.updateFulfilmentUI();
        this.calculateTotals();
      });
    });

    // File upload
    const fileInput = document.getElementById('rx-file-input');
    if (fileInput) {
      fileInput.addEventListener('change', (e) => this.handleFileUpload(e.target.files[0]));
    }

    // Geolocation simulation
    const locBtn = document.getElementById('btn-use-location');
    if (locBtn) {
      locBtn.addEventListener('click', () => this.useCurrentLocation());
    }

    // Submit emergency button
    const submitBtn = document.getElementById('btn-submit-emergency');
    if (submitBtn) {
      submitBtn.addEventListener('click', () => this.submitRequest());
    }
  },

  renderStep() {
    for (let i = 1; i <= 4; i++) {
      const pane = document.getElementById(`step-pane-${i}`);
      const stepItem = document.getElementById(`step-nav-${i}`);
      if (pane) {
        pane.style.display = i === this.currentStep ? 'block' : 'none';
      }
      if (stepItem) {
        stepItem.classList.remove('active', 'completed');
        if (i === this.currentStep) {
          stepItem.classList.add('active');
        } else if (i < this.currentStep) {
          stepItem.classList.add('completed');
        }
      }
    }

    if (this.currentStep === 4) {
      this.renderReviewSummary();
    }
  },

  nextStep() {
    if (this.validateStep(this.currentStep)) {
      this.currentStep++;
      this.renderStep();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  },

  prevStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
      this.renderStep();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  },

  validateStep(step) {
    if (step === 1) {
      if (this.selectedItems.length === 0) {
        ui.toast('Please select at least 1 medicine item', 'warning');
        return false;
      }
      return true;
    }

    if (step === 2) {
      if (this.fulfilmentType === 'DELIVERY') {
        const addr = document.getElementById('deliveryAddress').value.trim();
        if (addr.length < 10) {
          ui.toast('Please provide a complete delivery address (at least 10 characters)', 'warning');
          document.getElementById('deliveryAddress').focus();
          return false;
        }
      }
      return true;
    }

    if (step === 3) {
      const name = document.getElementById('patientName').value.trim();
      const phone = document.getElementById('patientPhone').value.trim();

      if (!name) {
        ui.toast('Patient name is required', 'warning');
        document.getElementById('patientName').focus();
        return false;
      }

      if (!/^[6-9][0-9]{9}$/.test(phone)) {
        ui.toast('Please enter a valid 10-digit Indian mobile number', 'warning');
        document.getElementById('patientPhone').focus();
        return false;
      }

      const needsRx = this.selectedItems.some(i => i.requiresPrescription);
      if (needsRx && !this.uploadedPrescriptionId) {
        ui.toast('Prescription upload is required for the selected prescription medicines', 'warning');
        return false;
      }

      return true;
    }

    return true;
  },

  renderItemsList() {
    const container = document.getElementById('selected-items-container');
    if (!container) return;

    if (this.selectedItems.length === 0) {
      container.innerHTML = '<p class="text-muted small">No medicines added.</p>';
      return;
    }

    container.innerHTML = this.selectedItems.map((item, idx) => `
      <div class="d-flex justify-content-between align-items-center p-3 mb-2 border rounded bg-white">
        <div>
          <div class="fw-bold">${item.name}</div>
          <div class="text-muted small">Unit Price: ${ui.formatMoney(item.unitPrice)} ${item.requiresPrescription ? '<span class="badge bg-danger-subtle text-danger ms-1">Rx Required</span>' : ''}</div>
        </div>
        <div class="d-flex align-items-center gap-3">
          <div class="qty-stepper">
            <button type="button" onclick="requestWizard.updateQty(${idx}, -1)">-</button>
            <input type="text" value="${item.quantity}" readonly>
            <button type="button" onclick="requestWizard.updateQty(${idx}, 1)">+</button>
          </div>
          <div class="fw-bold text-end" style="min-width: 80px;">${ui.formatMoney(item.unitPrice * item.quantity)}</div>
          ${this.selectedItems.length > 1 ? `
            <button type="button" class="btn btn-sm btn-outline-danger" onclick="requestWizard.removeItem(${idx})">
              <i class="bi bi-trash"></i>
            </button>
          ` : ''}
        </div>
      </div>
    `).join('');

    this.calculateTotals();
    this.updatePrescriptionRequiredNotice();
  },

  addMedicinePrompt() {
    if (this.selectedItems.length >= 5) {
      ui.toast('Maximum 5 medicines per emergency request', 'warning');
      return;
    }

    const unselected = this.medicinesList.filter(m => !this.selectedItems.some(s => s.medicineId === m.id));
    if (unselected.length === 0) {
      ui.toast('All available medicines from this pharmacy have been added', 'info');
      return;
    }

    const selectOptions = unselected.map(m => `<option value="${m.id}">${m.name} (${ui.formatMoney(m.price)})</option>`).join('');

    const modalHtml = `
      <div class="modal fade" id="addMedicineModal" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Add Medicine</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <label class="form-label fw-semibold">Select Medicine from Apollo Adyar Pharmacy:</label>
              <select id="modal-med-select" class="form-select">${selectOptions}</select>
              <div class="mt-3">
                <label class="form-label fw-semibold">Quantity (1 - 100):</label>
                <input id="modal-med-qty" type="number" class="form-control" value="1" min="1" max="100">
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
              <button type="button" class="btn btn-teal" onclick="requestWizard.confirmAddMedicine()">Add Item</button>
            </div>
          </div>
        </div>
      </div>
    `;

    document.getElementById('modal-container').innerHTML = modalHtml;
    const modal = new bootstrap.Modal(document.getElementById('addMedicineModal'));
    modal.show();
  },

  confirmAddMedicine() {
    const medId = parseInt(document.getElementById('modal-med-select').value, 10);
    const qty = parseInt(document.getElementById('modal-med-qty').value, 10) || 1;
    const med = this.medicinesList.find(m => m.id === medId);

    if (med) {
      this.selectedItems.push({
        medicineId: med.id,
        name: med.name,
        unitPrice: med.price,
        quantity: Math.min(100, Math.max(1, qty)),
        requiresPrescription: med.requiresPrescription
      });
      this.renderItemsList();
      bootstrap.Modal.getInstance(document.getElementById('addMedicineModal')).hide();
    }
  },

  updateQty(idx, delta) {
    const item = this.selectedItems[idx];
    if (item) {
      const newQty = item.quantity + delta;
      if (newQty >= 1 && newQty <= 100) {
        item.quantity = newQty;
        this.renderItemsList();
      }
    }
  },

  removeItem(idx) {
    if (this.selectedItems.length > 1) {
      this.selectedItems.splice(idx, 1);
      this.renderItemsList();
    }
  },

  calculateTotals() {
    const itemsTotal = this.selectedItems.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
    const fee = this.fulfilmentType === 'DELIVERY' ? this.deliveryFee : 0;
    const grandTotal = itemsTotal + fee;

    document.querySelectorAll('.items-total-disp').forEach(el => el.textContent = ui.formatMoney(itemsTotal));
    document.querySelectorAll('.delivery-fee-disp').forEach(el => el.textContent = ui.formatMoney(fee));
    document.querySelectorAll('.grand-total-disp').forEach(el => el.textContent = ui.formatMoney(grandTotal));
  },

  updateFulfilmentUI() {
    const deliveryFields = document.getElementById('delivery-details-section');
    if (deliveryFields) {
      deliveryFields.style.display = this.fulfilmentType === 'DELIVERY' ? 'block' : 'none';
    }
    const isCityMeds = this.pharmacyId === 2;
    if (isCityMeds) {
      const deliveryRadio = document.getElementById('radio-delivery');
      if (deliveryRadio) {
        deliveryRadio.disabled = true;
        document.getElementById('delivery-disabled-notice').style.display = 'block';
      }
    }
  },

  updatePrescriptionRequiredNotice() {
    const hasRx = this.selectedItems.some(i => i.requiresPrescription);
    const notice = document.getElementById('prescription-required-alert');
    if (notice) {
      notice.style.display = hasRx ? 'block' : 'none';
    }
  },

  useCurrentLocation() {
    if (navigator.geolocation) {
      ui.showLoading('Acquiring GPS location...');
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          ui.hideLoading();
          document.getElementById('deliveryLat').value = pos.coords.latitude.toFixed(6);
          document.getElementById('deliveryLng').value = pos.coords.longitude.toFixed(6);
          ui.toast('GPS location recorded', 'success');
        },
        (err) => {
          ui.hideLoading();
          // Fallback Chennai location
          document.getElementById('deliveryLat').value = '13.005000';
          document.getElementById('deliveryLng').value = '80.255000';
          ui.toast('Location simulated (Chennai, Adyar)', 'info');
        }
      );
    } else {
      document.getElementById('deliveryLat').value = '13.005000';
      document.getElementById('deliveryLng').value = '80.255000';
      ui.toast('Default location set', 'info');
    }
  },

  async handleFileUpload(file) {
    if (!file) return;

    if (file.size > 2 * 1024 * 1024) {
      ui.toast('File size exceeds maximum 2 MB limit', 'danger');
      return;
    }

    const allowed = ['application/pdf', 'image/jpeg', 'image/png', 'image/jpg'];
    if (!allowed.includes(file.type)) {
      ui.toast('Invalid format. Only JPG, PNG, and PDF files are accepted', 'danger');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    const doctorName = document.getElementById('doctorName')?.value;
    if (doctorName) formData.append('doctorName', doctorName);

    ui.showLoading('Uploading prescription...');
    try {
      const res = await api.upload('/api/v1/prescriptions/upload', formData);
      ui.hideLoading();
      this.uploadedPrescriptionId = res.prescriptionId;
      document.getElementById('rx-upload-success').style.display = 'block';
      document.getElementById('rx-filename-disp').textContent = file.name;
      ui.toast('Prescription uploaded successfully', 'success');
    } catch (e) {
      ui.hideLoading();
      ui.toast('Upload failed: ' + e.message, 'danger');
    }
  },

  renderReviewSummary() {
    const summaryContainer = document.getElementById('review-summary-container');
    if (!summaryContainer) return;

    const patientName = document.getElementById('patientName').value.trim();
    const patientPhone = document.getElementById('patientPhone').value.trim();
    const priority = document.querySelector('input[name="priority"]:checked')?.value || 'URGENT';
    const notes = document.getElementById('notes').value.trim();
    const address = document.getElementById('deliveryAddress')?.value.trim();

    summaryContainer.innerHTML = `
      <div class="row g-3">
        <div class="col-md-6">
          <div class="p-3 bg-light rounded border">
            <h6 class="fw-bold text-navy mb-2"><i class="bi bi-person-fill"></i> Patient Details</h6>
            <div><strong>Name:</strong> ${patientName}</div>
            <div><strong>Phone:</strong> ${patientPhone}</div>
            <div><strong>Priority:</strong> ${ui.formatPriorityBadge(priority)}</div>
            ${notes ? `<div class="mt-2 text-muted small"><strong>Notes:</strong> ${notes}</div>` : ''}
          </div>
        </div>
        <div class="col-md-6">
          <div class="p-3 bg-light rounded border">
            <h6 class="fw-bold text-navy mb-2"><i class="bi bi-geo-alt-fill"></i> Fulfilment & Pharmacy</h6>
            <div><strong>Fulfilment:</strong> <span class="badge bg-secondary">${this.fulfilmentType}</span></div>
            <div><strong>Pharmacy:</strong> Apollo Adyar Pharmacy</div>
            ${this.fulfilmentType === 'DELIVERY' ? `<div><strong>Delivery Address:</strong> ${address}</div>` : ''}
            <div><strong>Payment:</strong> ${this.fulfilmentType === 'DELIVERY' ? 'Cash on Delivery (COD)' : 'Pay at Pharmacy'}</div>
          </div>
        </div>
        <div class="col-12">
          <div class="p-3 bg-light rounded border">
            <h6 class="fw-bold text-navy mb-2"><i class="bi bi-capsule"></i> Medicines Requested</h6>
            <ul class="list-group list-group-flush bg-transparent">
              ${this.selectedItems.map(i => `
                <li class="list-group-item bg-transparent d-flex justify-content-between py-1 px-0">
                  <span>${i.name} (x${i.quantity})</span>
                  <span class="fw-semibold">${ui.formatMoney(i.unitPrice * i.quantity)}</span>
                </li>
              `).join('')}
            </ul>
          </div>
        </div>
      </div>
    `;
  },

  async submitRequest() {
    const payload = {
      pharmacyId: this.pharmacyId,
      fulfilmentType: this.fulfilmentType,
      priority: document.querySelector('input[name="priority"]:checked')?.value || 'URGENT',
      items: this.selectedItems.map(i => ({ medicineId: i.medicineId, quantity: i.quantity })),
      patientName: document.getElementById('patientName').value.trim(),
      patientPhone: document.getElementById('patientPhone').value.trim(),
      doctorName: document.getElementById('doctorName')?.value.trim() || null,
      doctorRegNo: document.getElementById('doctorRegNo')?.value.trim() || null,
      deliveryAddress: this.fulfilmentType === 'DELIVERY' ? document.getElementById('deliveryAddress')?.value.trim() : null,
      deliveryLat: document.getElementById('deliveryLat')?.value ? parseFloat(document.getElementById('deliveryLat').value) : null,
      deliveryLng: document.getElementById('deliveryLng')?.value ? parseFloat(document.getElementById('deliveryLng').value) : null,
      prescriptionId: this.uploadedPrescriptionId,
      notes: document.getElementById('notes')?.value.trim() || null
    };

    ui.showLoading('Submitting emergency request...');
    try {
      const result = await api.post('/api/v1/requests', payload);
      ui.hideLoading();
      ui.toast('Emergency request submitted successfully!', 'success');
      setTimeout(() => {
        window.location.href = `/request/track.html?id=${result.id}`;
      }, 700);
    } catch (e) {
      ui.hideLoading();
      ui.toast('Failed to submit request: ' + e.message, 'danger');
    }
  }
};

document.addEventListener('DOMContentLoaded', () => requestWizard.init());
