// Notifications Management & Polling (Every 15 Seconds)

const notifications = {
  pollInterval: null,

  init() {
    this.updateNavbarBell();
    // Refresh the navbar notification count every 15 seconds
    this.pollInterval = setInterval(() => this.updateNavbarBell(), 15000);

    if (document.getElementById('notifications-list-container')) {
      this.loadNotificationsList();
    }
  },

  async updateNavbarBell() {
    try {
      const data = await api.get('/api/v1/notifications/unread-count');
      const badge = document.getElementById('navbar-notif-badge');
      if (badge && data) {
        const count = data.count || 0;
        if (count > 0) {
          badge.textContent = count > 99 ? '99+' : count;
          badge.style.display = 'inline-block';
        } else {
          badge.style.display = 'none';
        }
      }
    } catch (e) {
      console.debug('Failed to fetch unread notification count', e);
    }
  },

  async loadNotificationsList(unreadOnly = false) {
    const container = document.getElementById('notifications-list-container');
    if (!container) return;

    ui.showLoading('Loading notifications...');
    try {
      const data = await api.get('/api/v1/notifications', { unreadOnly, page: 0, size: 50 });
      ui.hideLoading();

      if (!data || !data.content || data.content.length === 0) {
        ui.renderEmpty(container, 'No notifications found', 'bi-bell-slash');
        return;
      }

      container.innerHTML = data.content.map(n => this.renderItem(n)).join('');
    } catch (e) {
      ui.hideLoading();
      ui.toast('Failed to load notifications: ' + e.message, 'danger');
    }
  },

  renderItem(n) {
    const isEmergency = n.emergency || n.type === 'REQUEST_CREATED' || n.type.includes('EMERGENCY');
    const borderClass = isEmergency ? 'border-danger border-2' : n.read ? 'border-light' : 'border-primary';
    const bgClass = n.read ? 'bg-white' : (isEmergency ? 'bg-danger-subtle' : 'bg-light');
    const iconClass = isEmergency ? 'bi-exclamation-triangle-fill text-danger' :
                      n.type === 'REQUEST_ACCEPTED' ? 'bi-check-circle-fill text-success' :
                      n.type === 'DELIVERY_OFFER' ? 'bi-bicycle text-warning' : 'bi-info-circle-fill text-primary';

    const reqLink = n.requestId ? `/request/track.html?id=${n.requestId}` : '#';

    return `
      <div class="card app-card mb-3 ${borderClass} ${bgClass}" id="notif-card-${n.id}">
        <div class="card-body d-flex align-items-start gap-3">
          <div class="fs-3"><i class="bi ${iconClass}"></i></div>
          <div class="flex-grow-1" style="cursor: pointer;" onclick="notifications.openRequest(${n.id}, '${reqLink}')">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <h6 class="mb-0 fw-bold ${isEmergency ? 'text-danger' : 'text-dark'}">${n.title}</h6>
              <span class="text-muted small">${ui.formatDateTime(n.createdAt)}</span>
            </div>
            <p class="mb-1 text-secondary small">${n.message}</p>
            ${n.requestId ? `<span class="badge bg-secondary-subtle text-secondary small">Request ID: #${n.requestId}</span>` : ''}
            ${!n.read ? `<span class="badge bg-primary ms-1">New</span>` : ''}
          </div>
          ${!n.read ? `
            <button class="btn btn-sm btn-outline-secondary" title="Mark as read" onclick="notifications.markAsRead(${n.id})">
              <i class="bi bi-check2"></i>
            </button>
          ` : ''}
        </div>
      </div>
    `;
  },

  async markAsRead(id) {
    try {
      await api.patch(`/api/v1/notifications/${id}/read`, {});
      this.updateNavbarBell();
      const card = document.getElementById(`notif-card-${id}`);
      if (card) {
        card.classList.remove('bg-light', 'bg-danger-subtle');
        card.classList.add('bg-white');
        const badge = card.querySelector('.badge.bg-primary');
        if (badge) badge.remove();
        const btn = card.querySelector('button');
        if (btn) btn.remove();
      }
      ui.toast('Marked as read', 'success');
    } catch (e) {
      ui.toast('Error: ' + e.message, 'danger');
    }
  },

  async markAllAsRead() {
    try {
      ui.showLoading('Updating...');
      await api.patch('/api/v1/notifications/read-all', {});
      ui.hideLoading();
      this.updateNavbarBell();
      this.loadNotificationsList();
      ui.toast('All notifications marked as read', 'success');
    } catch (e) {
      ui.hideLoading();
      ui.toast('Error: ' + e.message, 'danger');
    }
  },

  async openRequest(notifId, url) {
    try {
      await api.patch(`/api/v1/notifications/${notifId}/read`, {});
    } catch (ignored) {}
    if (url && url !== '#') {
      window.location.href = url;
    }
  }
};

document.addEventListener('DOMContentLoaded', () => notifications.init());
