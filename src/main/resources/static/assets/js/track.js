// Order Tracking & Live Real-Time Polling (Swiggy-style delivery tracking)

const trackApp = {
  requestId: null,
  pollTimer: null,
  liveTrackingTimer: null,
  currentData: null,
  map: null,
  pharmacyMarker: null,
  partnerMarker: null,
  destMarker: null,
  routePolyline: null,
  hasInitializedMap: false,

  async init() {
    const params = new URLSearchParams(window.location.search);
    this.requestId = params.get('id');

    if (!this.requestId) {
      ui.renderEmpty('track-main-container', 'No Request ID specified in URL', 'bi-search');
      return;
    }

    await this.fetchTrackingData();
    // Standard order details poll every 10 seconds
    this.pollTimer = setInterval(() => this.fetchTrackingData(true), 10000);
  },

  async fetchTrackingData(isSilent = false) {
    if (!isSilent) ui.showLoading('Loading order status...');
    try {
      const data = await api.get(`/api/v1/requests/${this.requestId}`);
      if (!isSilent) ui.hideLoading();
      this.currentData = data;
      this.render();

      // If delivery order, initialize and poll live map tracking
      if (data && data.fulfilmentType === 'DELIVERY') {
        this.updateLiveTrackingView(data);
      } else {
        const liveSec = document.getElementById('live-tracking-section');
        if (liveSec) liveSec.style.display = 'none';
        this.stopLiveTracking();
      }
    } catch (e) {
      if (!isSilent) {
        ui.hideLoading();
        ui.toast('Failed to load tracking details: ' + e.message, 'danger');
      }
    }
  },

  render() {
    const d = this.currentData;
    if (!d) return;

    // Header info
    document.getElementById('track-ref-code').textContent = d.referenceCode;
    document.getElementById('track-status-badge').innerHTML = ui.formatStatusBadge(d.status);
    document.getElementById('track-priority-badge').innerHTML = ui.formatPriorityBadge(d.priority);
    document.getElementById('track-created-time').textContent = ui.formatDateTime(d.createdAt);

    // Timeline
    this.renderTimeline(d);

    // Handover code card (for deliveries)
    const handoverCard = document.getElementById('handover-code-container');
    if (handoverCard) {
      if (d.fulfilmentType === 'DELIVERY' && d.handoverCode) {
        handoverCard.style.display = 'block';
        document.getElementById('handover-code-digits').textContent = d.handoverCode;
      } else {
        handoverCard.style.display = 'none';
      }
    }

    // Switch to pickup button
    const switchBtn = document.getElementById('btn-switch-pickup');
    if (switchBtn) {
      switchBtn.style.display = d.canSwitchToPickup ? 'inline-block' : 'none';
    }

    // Cancel button
    const cancelBtn = document.getElementById('btn-cancel-request');
    if (cancelBtn) {
      cancelBtn.style.display = d.canCancel ? 'inline-block' : 'none';
    }

    // Pharmacy Details
    document.getElementById('pharmacy-name-disp').textContent = d.pharmacyName || 'Approved Pharmacy';
    document.getElementById('pharmacy-addr-disp').textContent = d.pharmacyAddress || '-';
    document.getElementById('pharmacy-phone-disp').textContent = d.pharmacyPhone || '-';

    // Partner Details
    const partnerCard = document.getElementById('delivery-partner-card');
    if (partnerCard) {
      if (d.deliveryPartnerUserId) {
        partnerCard.style.display = 'block';
        document.getElementById('partner-name-disp').textContent = d.deliveryPartnerName || 'Assigned Rider';
        document.getElementById('partner-phone-disp').textContent = d.deliveryPartnerPhone || 'Available upon dispatch';
      } else {
        partnerCard.style.display = 'none';
      }
    }

    // Items list
    const itemsList = document.getElementById('track-items-list');
    if (itemsList) {
      itemsList.innerHTML = d.items.map(it => `
        <div class="d-flex justify-content-between py-2 border-bottom">
          <div>
            <span class="fw-semibold">${it.medicineName}</span>
            <span class="text-muted small ms-2">x${it.quantity}</span>
            ${it.requiresPrescription ? '<span class="badge bg-danger-subtle text-danger ms-1">Rx</span>' : ''}
          </div>
          <span class="fw-bold">${ui.formatMoney(it.lineTotal)}</span>
        </div>
      `).join('');
    }

    // Totals
    document.getElementById('track-items-total').textContent = ui.formatMoney(d.itemsTotal);
    document.getElementById('track-delivery-fee').textContent = ui.formatMoney(d.deliveryFee);
    document.getElementById('track-grand-total').textContent = ui.formatMoney(d.grandTotal);

    // Payment action button
    const checkoutBtn = document.getElementById('btn-order-checkout');
    if (checkoutBtn) {
      if (['CANCELLED', 'REJECTED', 'EXPIRED'].includes(d.status)) {
        checkoutBtn.parentElement.style.display = 'none';
      } else {
        checkoutBtn.parentElement.style.display = 'block';
        checkoutBtn.href = `/checkout.html?orderId=${d.id}`;
      }
    }

    // Prescription status
    const rxStatusEl = document.getElementById('track-rx-status');
    if (rxStatusEl) {
      const rxColors = {
        'NONE': 'bg-secondary',
        'PENDING': 'bg-warning text-dark',
        'VERIFIED': 'bg-success',
        'REJECTED': 'bg-danger'
      };
      rxStatusEl.innerHTML = `<span class="badge ${rxColors[d.prescriptionStatus] || 'bg-secondary'}">${d.prescriptionStatus}</span>`;
      if (d.prescriptionId) {
        document.getElementById('btn-view-rx').style.display = 'inline-block';
        document.getElementById('btn-view-rx').onclick = () => window.open(`/api/v1/prescriptions/${d.prescriptionId}/file`, '_blank');
      }
    }
  },

  // -------------------------------------------------------------------------
  // Live Map & Real-time Partner Tracking
  // -------------------------------------------------------------------------
  updateLiveTrackingView(d) {
    const liveSec = document.getElementById('live-tracking-section');
    if (!liveSec) return;
    liveSec.style.display = 'block';

    // Update legend
    const legPharm = document.getElementById('legend-pharmacy-name');
    if (legPharm && d.pharmacyName) legPharm.textContent = d.pharmacyName;

    const legPartner = document.getElementById('legend-partner-name');
    if (legPartner && d.deliveryPartnerName) legPartner.textContent = d.deliveryPartnerName;

    const legDest = document.getElementById('legend-dest-name');
    if (legDest && d.deliveryAddress) legDest.textContent = d.deliveryAddress.substring(0, 25) + '...';

    // Initialize Leaflet map if not yet created
    if (!this.map && window.L) {
      this.initMap(d);
    }

    // Fetch immediate live tracking data
    this.fetchLiveTracking();

    // Start fast polling timer (every 4 seconds) if delivery in progress
    const activeStatuses = ['DELIVERY_ASSIGNED', 'OUT_FOR_DELIVERY'];
    if (activeStatuses.includes(d.status)) {
      if (!this.liveTrackingTimer) {
        this.liveTrackingTimer = setInterval(() => this.fetchLiveTracking(), 4000);
      }
    } else {
      this.stopLiveTracking();
    }
  },

  initMap(d) {
    const mapEl = document.getElementById('live-delivery-map');
    if (!mapEl) return;

    // Default center (Chennai demo coordinate)
    const centerLat = d.deliveryLat || (d.pharmacyLat || 13.0040);
    const centerLng = d.deliveryLng || (d.pharmacyLng || 80.2560);

    this.map = L.map('live-delivery-map', {
      zoomControl: true,
      scrollWheelZoom: false
    }).setView([centerLat, centerLng], 14);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    // Invalidate size once rendered
    setTimeout(() => {
      if (this.map) this.map.invalidateSize();
    }, 200);

    this.hasInitializedMap = true;
  },

  async fetchLiveTracking() {
    try {
      const tracking = await api.get(`/api/v1/requests/${this.requestId}/live-tracking`);
      if (!tracking) return;

      this.updateMapState(tracking);
    } catch (e) {
      console.warn('Live tracking poll error:', e);
    }
  },

  updateMapState(t) {
    if (!this.map || !window.L) return;

    // Update status bar texts
    const statusMsgEl = document.getElementById('map-status-msg');
    if (statusMsgEl) {
      statusMsgEl.innerHTML = `<i class="bi bi-bicycle me-1"></i> ${t.statusMessage || 'Live tracking in progress'}`;
    }

    const distText = document.getElementById('map-dist-text');
    if (distText) {
      distText.textContent = (t.remainingDistanceKm !== null && t.remainingDistanceKm !== undefined)
        ? `${t.remainingDistanceKm} km remaining`
        : 'Calculating distance...';
    }

    const etaText = document.getElementById('map-eta-text');
    if (etaText) {
      etaText.textContent = (t.estimatedArrivalMinutes !== null && t.estimatedArrivalMinutes !== undefined)
        ? `ETA: ~${t.estimatedArrivalMinutes} mins`
        : 'Calculating ETA...';
    }

    const lastUpd = document.getElementById('map-last-updated');
    if (lastUpd) {
      const now = new Date();
      lastUpd.innerHTML = `<i class="bi bi-arrow-repeat"></i> Updated ${now.toLocaleTimeString()}`;
    }

    const liveBadge = document.getElementById('map-live-badge');
    if (liveBadge) {
      if (t.partnerLat && t.partnerLng && t.trackingActive) {
        liveBadge.textContent = 'GPS ACTIVE';
        liveBadge.className = 'badge bg-success-subtle text-success border border-success-subtle fw-semibold';
      } else if (t.status === 'DELIVERED') {
        liveBadge.textContent = 'DELIVERED';
        liveBadge.className = 'badge bg-primary-subtle text-primary border border-primary-subtle fw-semibold';
      } else {
        liveBadge.textContent = 'AWAITING GPS';
        liveBadge.className = 'badge bg-warning-subtle text-warning border border-warning-subtle fw-semibold';
      }
    }

    const boundsPoints = [];

    // 1. Pharmacy Marker
    if (t.pharmacyLat && t.pharmacyLng) {
      const pLatLng = [t.pharmacyLat, t.pharmacyLng];
      boundsPoints.push(pLatLng);

      if (!this.pharmacyMarker) {
        const pharmIcon = L.divIcon({
          className: '',
          html: '<div class="custom-map-marker marker-pharmacy" title="Pharmacy"><i class="bi bi-hospital"></i></div>',
          iconSize: [38, 38],
          iconAnchor: [19, 19]
        });
        this.pharmacyMarker = L.marker(pLatLng, { icon: pharmIcon }).addTo(this.map);
        this.pharmacyMarker.bindPopup(`<strong>${t.pharmacyName || 'Pharmacy'}</strong><br>${t.pharmacyAddress || 'Pickup Point'}`);
      } else {
        this.pharmacyMarker.setLatLng(pLatLng);
      }
    }

    // 2. Destination Marker (Patient Delivery Address)
    if (t.deliveryLat && t.deliveryLng) {
      const dLatLng = [t.deliveryLat, t.deliveryLng];
      boundsPoints.push(dLatLng);

      if (!this.destMarker) {
        const destIcon = L.divIcon({
          className: '',
          html: '<div class="custom-map-marker marker-destination" title="Delivery Address"><i class="bi bi-house-door-fill"></i></div>',
          iconSize: [38, 38],
          iconAnchor: [19, 19]
        });
        this.destMarker = L.marker(dLatLng, { icon: destIcon }).addTo(this.map);
        this.destMarker.bindPopup(`<strong>Delivery Destination</strong><br>${t.deliveryAddress || 'Your Address'}`);
      } else {
        this.destMarker.setLatLng(dLatLng);
      }
    }

    // 3. Delivery Partner Marker (Rider with live location)
    if (t.partnerLat && t.partnerLng) {
      const rLatLng = [t.partnerLat, t.partnerLng];
      boundsPoints.push(rLatLng);

      if (!this.partnerMarker) {
        const partnerIcon = L.divIcon({
          className: '',
          html: '<div class="custom-map-marker marker-partner" title="Courier"><i class="bi bi-bicycle"></i></div>',
          iconSize: [38, 38],
          iconAnchor: [19, 19]
        });
        this.partnerMarker = L.marker(rLatLng, { icon: partnerIcon }).addTo(this.map);
      } else {
        this.partnerMarker.setLatLng(rLatLng);
      }

      this.partnerMarker.bindPopup(`
        <strong>${t.deliveryPartnerName || 'Courier Partner'}</strong><br>
        <span class="text-muted"><i class="bi bi-telephone"></i> ${t.deliveryPartnerPhone || 'Active'}</span><br>
        <span class="badge bg-warning text-dark mt-1">${t.status}</span>
      `);

      // Draw or update route polyline
      const routePoints = [];
      if (t.status === 'DELIVERY_ASSIGNED' && t.pharmacyLat && t.pharmacyLng) {
        // Rider -> Pharmacy -> Destination
        routePoints.push(rLatLng);
        routePoints.push([t.pharmacyLat, t.pharmacyLng]);
        if (t.deliveryLat && t.deliveryLng) routePoints.push([t.deliveryLat, t.deliveryLng]);
      } else {
        // Rider -> Destination
        routePoints.push(rLatLng);
        if (t.deliveryLat && t.deliveryLng) routePoints.push([t.deliveryLat, t.deliveryLng]);
      }

      if (routePoints.length >= 2) {
        if (!this.routePolyline) {
          this.routePolyline = L.polyline(routePoints, {
            color: '#0D9488',
            weight: 5,
            opacity: 0.85,
            dashArray: '8, 8',
            lineJoin: 'round'
          }).addTo(this.map);
        } else {
          this.routePolyline.setLatLngs(routePoints);
        }
      }
    }

    // Fit map bounds if points exist and first load
    if (boundsPoints.length >= 2 && !this._initialFitted) {
      this._initialFitted = true;
      try {
        this.map.fitBounds(L.latLngBounds(boundsPoints).pad(0.2));
      } catch (ignored) {}
    }
  },

  recenterMap() {
    if (!this.map) return;
    const points = [];
    if (this.partnerMarker) points.push(this.partnerMarker.getLatLng());
    if (this.destMarker) points.push(this.destMarker.getLatLng());
    if (this.pharmacyMarker) points.push(this.pharmacyMarker.getLatLng());

    if (points.length > 0) {
      this.map.fitBounds(L.latLngBounds(points).pad(0.2));
    }
  },

  stopLiveTracking() {
    if (this.liveTrackingTimer) {
      clearInterval(this.liveTrackingTimer);
      this.liveTrackingTimer = null;
    }
  },

  renderTimeline(d) {
    const list = document.getElementById('track-timeline-list');
    if (!list) return;

    if (!d.history || d.history.length === 0) {
      list.innerHTML = `
        <div class="timeline-node active">
          <div class="timeline-dot"><i class="bi bi-clock"></i></div>
          <div class="timeline-content">
            <div class="timeline-title">${ui.formatStatusBadge(d.status)}</div>
            <div class="timeline-meta">${ui.formatDateTime(d.createdAt)}</div>
          </div>
        </div>
      `;
      return;
    }

    list.innerHTML = d.history.map((h, i) => {
      const isLast = i === d.history.length - 1;
      return `
        <div class="timeline-node ${isLast ? 'active' : ''}">
          <div class="timeline-dot"><i class="bi ${isLast ? 'bi-check-lg' : 'bi-circle-fill'}"></i></div>
          <div class="timeline-content">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <span class="timeline-title">${h.toStatus}</span>
              <span class="timeline-meta">${ui.formatDateTime(h.timestamp)}</span>
            </div>
            <p class="mb-0 small text-secondary">${h.note || ''}</p>
            <span class="text-muted" style="font-size: 0.725rem;">Actor: ${h.changedByName || 'System'}</span>
          </div>
        </div>
      `;
    }).join('');
  },

  promptCancel() {
    const reason = prompt('Please enter the reason for cancellation (minimum 5 characters):');
    if (!reason) return;
    if (reason.trim().length < 5) {
      ui.toast('Cancellation reason must be at least 5 characters', 'warning');
      return;
    }
    this.confirmCancel(reason.trim());
  },

  async confirmCancel(reason) {
    ui.showLoading('Cancelling request...');
    try {
      await api.post(`/api/v1/requests/${this.requestId}/cancel`, { reason });
      ui.hideLoading();
      ui.toast('Request cancelled successfully', 'info');
      this.fetchTrackingData();
    } catch (e) {
      ui.hideLoading();
      ui.toast('Cancellation failed: ' + e.message, 'danger');
    }
  },

  async confirmSwitchToPickup() {
    if (!confirm('Switch this order to Self-Pickup? The delivery fee will be removed, and you can collect the medicines from the pharmacy directly.')) {
      return;
    }

    ui.showLoading('Switching to self-pickup...');
    try {
      await api.post(`/api/v1/requests/${this.requestId}/switch-to-pickup`, {});
      ui.hideLoading();
      ui.toast('Switched to self-pickup!', 'success');
      this.fetchTrackingData();
    } catch (e) {
      ui.hideLoading();
      ui.toast('Failed: ' + e.message, 'danger');
    }
  }
};

document.addEventListener('DOMContentLoaded', () => trackApp.init());
