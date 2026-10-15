// Auth Guard & Dynamic Session Handler
// Verifies user sessions, checks role permissions, and controls dashboard access

const AuthGuard = {
  currentUser: null,

  async init(allowedRoles = []) {
    try {
      const user = await api.get('/api/v1/auth/me');
      if (!user) {
        sessionStorage.setItem('redirect_after_login', window.location.pathname + window.location.search);
        window.location.href = '/login.html';
        return null;
      }

      this.currentUser = user;

      // Check role permissions if specified
      if (allowedRoles && allowedRoles.length > 0) {
        const role = user.role;
        const normalizedRole = role.startsWith('ROLE_') ? role.substring(5) : role;
        const normalizedAllowed = allowedRoles.map(r => r.startsWith('ROLE_') ? r.substring(5) : r);

        if (!normalizedAllowed.includes(normalizedRole) && normalizedRole !== 'ADMIN') {
          console.warn(`Access restricted: user role ${normalizedRole} is not in [${normalizedAllowed.join(', ')}]`);
          const targetDashboard = this.getRoleDashboard(normalizedRole);
          if (window.location.pathname !== targetDashboard) {
            window.location.href = targetDashboard;
            return null;
          }
        }
      }

      this.renderRoleBar();
      this.updateNavbarUser();
      return this.currentUser;
    } catch (err) {
      console.warn('Session inactive or expired, redirecting to login:', err);
      sessionStorage.setItem('redirect_after_login', window.location.pathname + window.location.search);
      window.location.href = '/login.html';
      return null;
    }
  },

  getRoleDashboard(role) {
    switch (role) {
      case 'ADMIN': return '/dashboard/admin.html';
      case 'PATIENT': return '/dashboard/patient.html';
      case 'HOSPITAL': return '/dashboard/hospital.html';
      case 'PHARMACY': return '/dashboard/pharmacy.html';
      case 'DELIVERY': return '/dashboard/delivery.html';
      default: return '/index.html';
    }
  },

  renderRoleBar() {
    let bar = document.getElementById('role-switcher-banner');
    if (!bar) {
      bar = document.createElement('div');
      bar.id = 'role-switcher-banner';
      bar.className = 'role-banner d-flex justify-content-between align-items-center flex-wrap gap-2 px-3 py-1';
      bar.style.backgroundColor = '#102A43';
      bar.style.color = '#FFFFFF';
      bar.style.fontSize = '0.85rem';
      document.body.prepend(bar);
    }

    const u = this.currentUser || { userId: 1, role: 'PATIENT', fullName: 'User' };
    const roleColors = {
      'PATIENT': 'badge bg-primary text-white',
      'HOSPITAL': 'badge bg-info text-dark',
      'PHARMACY': 'badge bg-warning text-dark',
      'DELIVERY': 'badge bg-success text-white',
      'ADMIN': 'badge bg-danger text-white'
    };

    bar.innerHTML = `
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-shield-lock-fill text-warning"></i>
        <span>Logged in as: <strong>${escapeHtml(u.fullName || u.email)}</strong></span>
        <span class="${roleColors[u.role] || 'badge bg-secondary'}">${u.role}</span>
      </div>
      <div class="d-flex align-items-center gap-2">
        <a href="/index.html" class="text-white text-decoration-none me-2"><i class="bi bi-house me-1"></i>Home</a>
        <button class="btn btn-sm btn-outline-light py-0 px-2" onclick="AuthGuard.logout()">Logout</button>
      </div>
    `;
  },

  updateNavbarUser() {
    const userDisplay = document.getElementById('navbar-user-display') || document.getElementById('user-name');
    if (userDisplay && this.currentUser) {
      userDisplay.textContent = this.currentUser.fullName || this.currentUser.email;
    }
  },

  async logout() {
    try {
      await api.post('/api/v1/auth/logout', {});
    } catch (ignored) {}
    window.location.href = '/login.html';
  }
};

window.AuthGuard = AuthGuard;
window.auth = AuthGuard;
