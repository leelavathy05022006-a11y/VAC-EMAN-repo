// Shared UI Component Helpers & Utilities

const ui = {
  toast(message, type = 'info') {
    let container = document.getElementById('app-toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'app-toast-container';
      container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
      document.body.appendChild(container);
    }

    const toastId = 'toast-' + Date.now();
    const bgClass = type === 'danger' || type === 'error' ? 'bg-danger text-white' :
                    type === 'success' ? 'bg-success text-white' :
                    type === 'warning' ? 'bg-warning text-dark' : 'bg-primary text-white';

    const html = `
      <div id="${toastId}" class="toast align-items-center ${bgClass} border-0 shadow-lg" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
          <div class="toast-body d-flex align-items-center gap-2">
            <i class="bi ${type === 'danger' ? 'bi-exclamation-octagon-fill' : type === 'success' ? 'bi-check-circle-fill' : 'bi-info-circle-fill'}"></i>
            <span>${message}</span>
          </div>
          <button type="button" class="btn-close ${type !== 'warning' ? 'btn-close-white' : ''} me-2 m-auto" data-bs-dismiss="toast" onclick="this.closest('.toast').remove()" aria-label="Close"></button>
        </div>
      </div>
    `;

    container.insertAdjacentHTML('beforeend', html);
    const toastEl = document.getElementById(toastId);
    if (window.bootstrap && bootstrap.Toast) {
      const bsToast = new bootstrap.Toast(toastEl, { delay: 4500 });
      bsToast.show();
      toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
    } else {
      setTimeout(() => toastEl.remove(), 4500);
    }
  },

  showLoading(text = 'Processing...') {
    let el = document.getElementById('app-loading-overlay');
    if (!el) {
      el = document.createElement('div');
      el.id = 'app-loading-overlay';
      el.className = 'loading-overlay';
      el.innerHTML = `
        <div class="spinner-border text-danger mb-2" style="width: 3rem; height: 3rem;" role="status"></div>
        <div id="loading-overlay-text" class="fw-semibold text-dark">${text}</div>
      `;
      document.body.appendChild(el);
    } else {
      document.getElementById('loading-overlay-text').textContent = text;
    }
    el.classList.add('active');
  },

  hideLoading() {
    const el = document.getElementById('app-loading-overlay');
    if (el) el.classList.remove('active');
  },

  formatMoney(amount) {
    if (amount === null || amount === undefined) return '₹0.00';
    return '₹' + Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  },

  formatDateTime(isoStr) {
    if (!isoStr) return '-';
    const d = new Date(isoStr);
    return d.toLocaleString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit', hour12: true
    });
  },

  formatStatusBadge(status) {
    if (!status) return '<span class="badge bg-secondary">-</span>';
    const labels = {
      'PENDING': 'Pending Verification',
      'ACCEPTED': 'Accepted',
      'FINDING_DELIVERY': 'Finding Rider',
      'DELIVERY_ASSIGNED': 'Rider Assigned',
      'READY_FOR_PICKUP': 'Ready for Pickup',
      'OUT_FOR_DELIVERY': 'Out for Delivery',
      'DELIVERED': 'Delivered',
      'PICKED_UP': 'Picked Up',
      'REJECTED': 'Rejected',
      'EXPIRED': 'Expired',
      'CANCELLED': 'Cancelled'
    };
    const label = labels[status] || status;
    return `<span class="badge-status status-${status}">${label}</span>`;
  },

  formatPriorityBadge(priority) {
    if (!priority) return '';
    return `<span class="badge-status priority-${priority}"><i class="bi bi-lightning-fill"></i> ${priority}</span>`;
  },

  renderEmpty(containerEl, message = 'No records found', icon = 'bi-inbox') {
    if (typeof containerEl === 'string') {
      containerEl = document.getElementById(containerEl);
    }
    if (!containerEl) return;
    containerEl.innerHTML = `
      <div class="empty-state">
        <i class="bi ${icon} empty-state-icon"></i>
        <h5 class="fw-bold text-secondary mb-1">${message}</h5>
        <p class="text-muted small">Nothing to show right now.</p>
      </div>
    `;
  }
};

function renderSkeletonRows(cols = 5, rows = 5) {
  let html = '';
  for (let r = 0; r < rows; r++) {
    html += '<tr>';
    for (let c = 0; c < cols; c++) {
      html += '<td><span class="placeholder-glow"><span class="placeholder col-10"></span></span></td>';
    }
    html += '</tr>';
  }
  return html;
}
window.renderSkeletonRows = renderSkeletonRows;

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
window.escapeHtml = escapeHtml;

window.showToast = function(msg, type = 'info') {
  ui.toast(msg, type);
};

window.UI = {
  showToast(msg, type = 'info') {
    ui.toast(msg, type);
  },
  toast(msg, type = 'info') {
    ui.toast(msg, type);
  },
  showLoading(text) {
    if (typeof ui.showLoading === 'function') ui.showLoading(text);
  },
  hideLoading() {
    if (typeof ui.hideLoading === 'function') ui.hideLoading();
  },
  formatMoney(amount) {
    return ui.formatMoney(amount);
  },
  formatCurrency(amount) {
    return ui.formatMoney(amount);
  },
  formatDistance(distKm) {
    if (distKm === null || distKm === undefined) return '0.0 km';
    return Number(distKm).toFixed(1) + ' km';
  },
  formatEta(minutes) {
    if (minutes === null || minutes === undefined) return '1 min';
    if (minutes < 60) return `${Math.round(minutes)} mins`;
    const hrs = Math.floor(minutes / 60);
    const mins = Math.round(minutes % 60);
    return `${hrs} hr ${mins} mins`;
  },
  formatDateTime(isoStr) {
    return ui.formatDateTime(isoStr);
  },
  formatStatusBadge(status) {
    return ui.formatStatusBadge(status);
  },
  formatPriorityBadge(priority) {
    return ui.formatPriorityBadge(priority);
  },
  escapeHtml(str) {
    return escapeHtml(str);
  },
  renderNavbar(user = null) {
    const el = document.getElementById('main-navbar');
    if (!el) return;
    el.innerHTML = `
      <nav class="navbar navbar-expand-lg navbar-dark" style="background-color: #102A43;">
        <div class="container">
          <a class="navbar-brand fw-bold d-flex align-items-center gap-2" href="/index.html">
            <i class="bi bi-hospital text-danger fs-4"></i>
            <span>EmergencyMed</span>
          </a>
          <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navContent">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
              <li class="nav-item"><a class="nav-link" href="/index.html">Home</a></li>
              <li class="nav-item"><a class="nav-link" href="/search.html">Medicine Search</a></li>
              <li class="nav-item"><a class="nav-link" href="/chennai-pharmacies.html">Pharmacies Map</a></li>
            </ul>
            <div class="d-flex align-items-center gap-2">
              ${user ? `
                <span class="text-white small me-2"><i class="bi bi-person-circle me-1"></i>${escapeHtml(user.fullName || user.email)}</span>
                <button class="btn btn-sm btn-outline-light" onclick="if(window.AuthGuard)AuthGuard.logout();else window.location.href='/login.html'">Sign Out</button>
              ` : `
                <a href="/login.html" class="btn btn-sm btn-outline-light">Sign In</a>
                <a href="/register.html" class="btn btn-sm btn-teal text-white">Register</a>
              `}
            </div>
          </div>
        </div>
      </nav>
    `;
  }
};

ui.formatCurrency = ui.formatMoney;
ui.formatDistance = window.UI.formatDistance;
ui.formatEta = window.UI.formatEta;

