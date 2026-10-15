// Delivery Partner Operations (Availability, Offers, Active Jobs, History & Live Tracking)

const deliveryApp = {
  countdownTimers: {},
  geoWatchId: null,
  geoPollTimer: null,
  activeJobsCache: [],

  async init() {
    if (document.getElementById('delivery-availability-toggle')) {
      await this.loadAvailability();
      await this.loadOffers();
      await this.loadActiveJobs();

      // Poll offers every 10 seconds
      setInterval(() => this.loadOffers(), 10000);
    }

    if (document.getElementById('delivery-history-table-body')) {
      await this.loadHistory(0);
    }
  },

  async loadAvailability() {
    try {
      const data = await api.get('/api/v1/delivery/me/availability');
      const toggle = document.getElementById('delivery-availability-toggle');
      const statusText = document.getElementById('availability-status-text');
      if (toggle && data) {
        toggle.checked = data.available;
        statusText.textContent = data.available ? 'Online (Accepting Offers)' : 'Offline';
        statusText.className = data.available ? 'fw-bold text-success' : 'text-muted';
      }
    } catch (e) {
      console.warn('Failed to load availability:', e);
    }
  },

  async toggleAvailability(checked) {
    ui.showLoading(checked ? 'Going Online...' : 'Going Offline...');
    try {
      let lat = null, lng = null;
      if (checked && navigator.geolocation) {
        try {
          const pos = await new Promise((res, rej) => navigator.geolocation.getCurrentPosition(res, rej, { timeout: 4000 }));
          lat = pos.coords.latitude;
          lng = pos.coords.longitude;
        } catch (ignored) {
          lat = 13.0033;
          lng = 80.2550;
        }
      }

      const res = await api.put('/api/v1/delivery/me/availability', { available: checked, lat, lng });
      ui.hideLoading();
      const statusText = document.getElementById('availability-status-text');
      statusText.textContent = checked ? 'Online (Accepting Offers)' : 'Offline';
      statusText.className = checked ? 'fw-bold text-success' : 'text-muted';
      ui.toast(`Status updated: ${checked ? 'Online' : 'Offline'}`, 'success');
      if (checked) this.loadOffers();
    } catch (e) {
      ui.hideLoading();
      ui.toast('Error: ' + e.message, 'danger');
    }
  },

  async shareLocation() {
    if (!navigator.geolocation) {
      ui.toast('Geolocation not supported by browser', 'warning');
      return;
    }

    ui.showLoading('Acquiring current GPS location...');
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        try {
          await api.put('/api/v1/delivery/me/location', {
            lat: pos.coords.latitude,
            lng: pos.coords.longitude
          });
          ui.hideLoading();
          ui.toast('GPS Location shared successfully!', 'success');
        } catch (e) {
          ui.hideLoading();
          ui.toast('Failed to update location: ' + e.message, 'danger');
        }
      },
      (err) => {
        ui.hideLoading();
        ui.toast('Location permission denied or timed out', 'warning');
      }
    );
  },

  async loadOffers() {
    const container = document.getElementById('delivery-offers-container');
    if (!container) return;

    try {
      const offers = await api.get('/api/v1/delivery/offers');
      if (!offers || offers.length === 0) {
        container.innerHTML = `
          <div class="p-4 text-center border rounded bg-white">
            <i class="bi bi-bell-slash text-muted fs-3"></i>
            <p class="mb-0 mt-2 text-muted small">No active delivery offers right now. Keep your status online to receive incoming requests.</p>
          </div>
        `;
        return;
      }

      container.innerHTML = offers.map(o => this.renderOfferCard(o)).join('');
      // Initialize countdown timers
      offers.forEach(o => this.startOfferCountdown(o.assignmentId, o.secondsRemaining));
    } catch (e) {
      console.warn('Failed to load delivery offers', e);
    }
  },

  renderOfferCard(o) {
    return `
      <div class="card app-card border-danger mb-3 shadow-sm" id="offer-card-${o.assignmentId}">
        <div class="card-header bg-danger text-white d-flex justify-content-between align-items-center">
          <span class="fw-bold"><span class="pulse-beacon"></span> EMERGENCY DELIVERY OFFER</span>
          <span class="badge bg-white text-danger fw-bold" id="countdown-${o.assignmentId}">
            <i class="bi bi-stopwatch"></i> ${o.secondsRemaining}s
          </span>
        </div>
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <h5 class="fw-bold mb-0">${o.referenceCode}</h5>
            ${ui.formatPriorityBadge(o.priority)}
          </div>
          <div class="row g-2 mb-3 small">
            <div class="col-md-6">
              <div class="text-muted">Pickup Pharmacy:</div>
              <div class="fw-semibold">${o.pharmacyName}</div>
              <div class="text-secondary">${o.pharmacyAddress || '-'}</div>
            </div>
            <div class="col-md-6">
              <div class="text-muted">Delivery Address:</div>
              <div class="fw-semibold">${o.deliveryAddress}</div>
              <div class="text-success fw-bold">Delivery Fee: ${ui.formatMoney(o.deliveryFee)}</div>
            </div>
          </div>
          <div class="d-flex gap-2">
            <button class="btn btn-success flex-grow-1 fw-bold" onclick="deliveryApp.acceptOffer(${o.assignmentId})">
              <i class="bi bi-check-circle-fill"></i> Accept Offer
            </button>
            <button class="btn btn-outline-danger" onclick="deliveryApp.rejectOffer(${o.assignmentId})">
              <i class="bi bi-x-circle"></i> Decline
            </button>
          </div>
        </div>
      </div>
    `;
  },

  startOfferCountdown(assignmentId, seconds) {
    if (this.countdownTimers[assignmentId]) {
      clearInterval(this.countdownTimers[assignmentId]);
    }

    let remaining = seconds;
    this.countdownTimers[assignmentId] = setInterval(() => {
      remaining--;
      const el = document.getElementById(`countdown-${assignmentId}`);
      if (el) {
        if (remaining <= 0) {
          el.innerHTML = '<i class="bi bi-x-octagon"></i> Expired';
          clearInterval(this.countdownTimers[assignmentId]);
          setTimeout(() => this.loadOffers(), 1500);
        } else {
          el.innerHTML = `<i class="bi bi-stopwatch"></i> ${remaining}s`;
        }
      } else {
        clearInterval(this.countdownTimers[assignmentId]);
      }
    }, 1000);
  },

  async acceptOffer(id) {
    ui.showLoading('Accepting delivery offer...');
    try {
      await api.post(`/api/v1/delivery/offers/${id}/accept`, {});
      ui.hideLoading();
      ui.toast('Delivery assignment accepted! Head to the pharmacy.', 'success');
      this.loadOffers();
      this.loadActiveJobs();
    } catch (e) {
      ui.hideLoading();
      ui.toast('Failed to accept: ' + e.message, 'danger');
      this.loadOffers();
    }
  },

  async rejectOffer(id) {
    try {
      await api.post(`/api/v1/delivery/offers/${id}/reject`, { reason: 'Partner declined' });
      ui.toast('Offer declined', 'info');
      this.loadOffers();
    } catch (e) {
      ui.toast('Error: ' + e.message, 'danger');
    }
  },

  async loadActiveJobs() {
    const container = document.getElementById('active-jobs-container');
    if (!container) return;

    try {
      const res = await api.get('/api/v1/delivery/jobs', { type: 'active', page: 0, size: 10 });
      if (!res || !res.content || res.content.length === 0) {
        this.activeJobsCache = [];
        this.stopPartnerLiveTracking();
        container.innerHTML = `
          <div class="p-4 text-center border rounded bg-white">
            <i class="bi bi-box2 text-muted fs-3"></i>
            <p class="mb-0 mt-2 text-muted small">No active delivery jobs in progress.</p>
          </div>
        `;
        return;
      }

      this.activeJobsCache = res.content;
      this.startPartnerLiveTracking(res.content);

      container.innerHTML = res.content.map(j => `
        <div class="card app-card border-primary mb-3 shadow-sm">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5 class="fw-bold mb-0 text-primary">${j.referenceCode}</h5>
              ${ui.formatStatusBadge(j.status)}
            </div>
            <div class="p-2 mb-2 bg-light rounded small">
              <div><strong>Patient:</strong> ${j.patientName} (${j.patientPhone})</div>
              <div><strong>Delivery To:</strong> ${j.deliveryAddress}</div>
              <div><strong>Pharmacy:</strong> ${j.pharmacyName} (${j.pharmacyAddress})</div>
            </div>

            <!-- Live GPS Broadcasting Box -->
            <div class="p-2 mb-3 bg-white border rounded d-flex justify-content-between align-items-center flex-wrap gap-2">
              <div class="d-flex align-items-center gap-2 small">
                <span class="pulse-beacon-green"></span>
                <span class="fw-bold text-success">Live GPS:</span>
                <span id="gps-status-indicator-${j.id}" class="text-muted">Transmitting position...</span>
              </div>
              <div class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary py-0 px-2" style="font-size: 0.75rem;" onclick="deliveryApp.sendGpsPing(${j.id})" title="Send current device GPS coordinates">
                  <i class="bi bi-geo-alt-fill"></i> GPS Ping
                </button>
                <button class="btn btn-sm btn-outline-success py-0 px-2" style="font-size: 0.75rem;" onclick="deliveryApp.simulateStep(${j.id})" title="Simulate courier moving along route">
                  <i class="bi bi-play-fill"></i> Move Closer
                </button>
              </div>
            </div>

            <div class="d-flex gap-2">
              ${j.status === 'DELIVERY_ASSIGNED' ? `
                <button class="btn btn-teal fw-bold flex-grow-1" onclick="deliveryApp.markPickedUp(${j.id})">
                  <i class="bi bi-box-arrow-in-right"></i> Mark Order Picked Up
                </button>
              ` : ''}
              ${j.status === 'OUT_FOR_DELIVERY' ? `
                <button class="btn btn-emergency fw-bold flex-grow-1" onclick="deliveryApp.promptHandoverCode(${j.id})">
                  <i class="bi bi-shield-check"></i> Enter 4-Digit Handover Code & Complete
                </button>
              ` : ''}
              <a href="/request/track.html?id=${j.id}" class="btn btn-outline-secondary">
                <i class="bi bi-eye"></i> Track Map
              </a>
            </div>
          </div>
        </div>
      `).join('');
    } catch (e) {
      console.warn('Failed to load active jobs', e);
    }
  },

  // Continuous background GPS tracking for active delivery runs
  startPartnerLiveTracking(jobs) {
    if (this.geoWatchId || this.geoPollTimer) return;

    if (navigator.geolocation) {
      try {
        this.geoWatchId = navigator.geolocation.watchPosition(
          async (pos) => {
            try {
              await api.put('/api/v1/delivery/me/location', {
                lat: pos.coords.latitude,
                lng: pos.coords.longitude
              });
              this.updateGpsStatusText('Active (' + pos.coords.latitude.toFixed(4) + ', ' + pos.coords.longitude.toFixed(4) + ')');
            } catch (ignored) {}
          },
          (err) => {
            this.updateGpsStatusText('GPS wait / permission');
          },
          { enableHighAccuracy: true, maximumAge: 3000, timeout: 8000 }
        );
      } catch (e) {
        console.warn('watchPosition failed:', e);
      }
    }

    // Secondary fallback sync timer (every 6 seconds)
    this.geoPollTimer = setInterval(async () => {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          async (pos) => {
            try {
              await api.put('/api/v1/delivery/me/location', {
                lat: pos.coords.latitude,
                lng: pos.coords.longitude
              });
              this.updateGpsStatusText('Sync: ' + new Date().toLocaleTimeString());
            } catch (ignored) {}
          },
          () => {},
          { timeout: 4000 }
        );
      }
    }, 6000);
  },

  stopPartnerLiveTracking() {
    if (this.geoWatchId !== null && navigator.geolocation) {
      navigator.geolocation.clearWatch(this.geoWatchId);
      this.geoWatchId = null;
    }
    if (this.geoPollTimer) {
      clearInterval(this.geoPollTimer);
      this.geoPollTimer = null;
    }
  },

  updateGpsStatusText(msg) {
    this.activeJobsCache.forEach(j => {
      const el = document.getElementById(`gps-status-indicator-${j.id}`);
      if (el) el.textContent = msg;
    });
  },

  async sendGpsPing(jobId) {
    if (!navigator.geolocation) {
      ui.toast('Browser does not support geolocation', 'warning');
      return;
    }
    ui.showLoading('Transmitting GPS ping...');
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        try {
          await api.put('/api/v1/delivery/me/location', {
            lat: pos.coords.latitude,
            lng: pos.coords.longitude
          });
          ui.hideLoading();
          ui.toast('GPS ping broadcasted successfully!', 'success');
          this.updateGpsStatusText('Pinged at ' + new Date().toLocaleTimeString());
        } catch (e) {
          ui.hideLoading();
          ui.toast('Failed: ' + e.message, 'danger');
        }
      },
      (err) => {
        ui.hideLoading();
        ui.toast('GPS location permission denied or timed out', 'warning');
      }
    );
  },

  // Simulates courier moving along the route towards the pharmacy/destination
  async simulateStep(jobId) {
    const job = this.activeJobsCache.find(j => j.id === jobId);
    if (!job) return;

    try {
      ui.showLoading('Advancing courier position...');
      const avail = await api.get('/api/v1/delivery/me/availability');
      let curLat = (avail && avail.currentLat) ? Number(avail.currentLat) : 13.0040;
      let curLng = (avail && avail.currentLng) ? Number(avail.currentLng) : 80.2560;

      // Target coordinate: If DELIVERY_ASSIGNED -> pharmacy; if OUT_FOR_DELIVERY -> customer destination
      let targetLat = 12.9850;
      let targetLng = 80.2600;

      if (job.status === 'DELIVERY_ASSIGNED') {
        targetLat = job.pharmacyLat ? Number(job.pharmacyLat) : 13.0050;
        targetLng = job.pharmacyLng ? Number(job.pharmacyLng) : 80.2550;
      } else {
        targetLat = job.deliveryLat ? Number(job.deliveryLat) : 12.9850;
        targetLng = job.deliveryLng ? Number(job.deliveryLng) : 80.2600;
      }

      // Move 20% closer towards target
      let newLat = curLat + (targetLat - curLat) * 0.22;
      let newLng = curLng + (targetLng - curLng) * 0.22;

      await api.put('/api/v1/delivery/me/location', {
        lat: Number(newLat.toFixed(6)),
        lng: Number(newLng.toFixed(6))
      });

      ui.hideLoading();
      ui.toast(`Courier position advanced to (${newLat.toFixed(4)}, ${newLng.toFixed(4)})`, 'success');
      this.updateGpsStatusText(`Simulated: ${newLat.toFixed(4)}, ${newLng.toFixed(4)}`);
    } catch (e) {
      ui.hideLoading();
      ui.toast('Simulation error: ' + e.message, 'danger');
    }
  },

  async markPickedUp(id) {
    if (!confirm('Confirm order pickup from pharmacy? This commits inventory stock.')) return;
    ui.showLoading('Confirming pickup...');
    try {
      await api.post(`/api/v1/delivery/jobs/${id}/picked-up`, {});
      ui.hideLoading();
      ui.toast('Order is now OUT FOR DELIVERY!', 'success');
      this.loadActiveJobs();
    } catch (e) {
      ui.hideLoading();
      ui.toast('Pickup failed: ' + e.message, 'danger');
    }
  },

  promptHandoverCode(id) {
    const code = prompt('Enter the 4-digit recipient handover code:');
    if (!code) return;
    if (!/^[0-9]{4}$/.test(code.trim())) {
      ui.toast('Handover code must be exactly 4 digits', 'warning');
      return;
    }
    this.submitHandover(id, code.trim());
  },

  async submitHandover(id, handoverCode) {
    ui.showLoading('Verifying handover code...');
    try {
      await api.post(`/api/v1/delivery/jobs/${id}/delivered`, { handoverCode });
      ui.hideLoading();
      ui.toast('Order delivered successfully! Handover verified.', 'success');
      this.loadActiveJobs();
    } catch (e) {
      ui.hideLoading();
      ui.toast(e.message, 'danger');
    }
  },

  async loadHistory(page = 0) {
    const tbody = document.getElementById('delivery-history-table-body');
    if (!tbody) return;

    ui.showLoading('Loading delivery history...');
    try {
      const data = await api.get('/api/v1/delivery/jobs', { type: 'history', page, size: 10 });
      ui.hideLoading();

      if (!data || !data.content || data.content.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">No completed deliveries found</td></tr>`;
        return;
      }

      tbody.innerHTML = data.content.map(j => `
        <tr>
          <td class="fw-bold">${j.referenceCode}</td>
          <td>${ui.formatDateTime(j.completedAt || j.updatedAt)}</td>
          <td>${j.patientName}</td>
          <td>${j.deliveryAddress || '-'}</td>
          <td>${ui.formatMoney(j.deliveryFee)}</td>
          <td>${ui.formatStatusBadge(j.status)}</td>
          <td>
            <a href="/request/track.html?id=${j.id}" class="btn btn-sm btn-outline-primary">View</a>
          </td>
        </tr>
      `).join('');
    } catch (e) {
      ui.hideLoading();
      ui.toast('Failed to load history: ' + e.message, 'danger');
    }
  }
};

document.addEventListener('DOMContentLoaded', () => deliveryApp.init());
