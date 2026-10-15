/**
 * Emergency Medicine Availability Network - Geolocation Utility (geo.js)
 * Implements browser location -> saved profile location -> manual coordinates fallback.
 */

const Geo = {
  currentLocation: {
    lat: null,
    lng: null,
    source: null // 'browser', 'profile', or 'manual'
  },

  /**
   * Resolves user coordinates following the priority chain:
   * 1. Browser HTML5 Geolocation
   * 2. Saved user profile coordinates (/api/v1/users/me)
   * 3. Manual coordinate panel / default Chennai center
   */
  async resolveLocation() {
    // 1. Try HTML5 Geolocation
    if (navigator.geolocation) {
      try {
        const position = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject, {
            enableHighAccuracy: true,
            timeout: 6000,
            maximumAge: 300000
          });
        });

        this.currentLocation = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          source: 'browser'
        };
        this.updateLocationBadge('GPS Location', true);
        return this.currentLocation;
      } catch (geoError) {
        console.warn('Browser geolocation denied or timed out:', geoError.message);
        // Show manual panel automatically when location is denied
        const advPanel = document.getElementById('manual-location-panel');
        if (advPanel && window.bootstrap && window.bootstrap.Collapse) {
          const bsCollapse = new bootstrap.Collapse(advPanel, { toggle: false });
          bsCollapse.show();
        }
      }
    }

    // 2. Fallback to saved profile location
    try {
      const res = await Api.get('/users/me');
      if (res && res.data && res.data.latitude && res.data.longitude) {
        this.currentLocation = {
          lat: res.data.latitude,
          lng: res.data.longitude,
          source: 'profile'
        };
        this.updateLocationBadge('Profile Location (Adyar)', false);
        return this.currentLocation;
      }
    } catch (profileError) {
      console.warn('Profile location fallback unavailable:', profileError);
    }

    // 3. Fallback default: Adyar, Chennai (13.0012, 80.2565)
    this.currentLocation = {
      lat: 13.0012,
      lng: 80.2565,
      source: 'manual'
    };
    this.updateLocationBadge('Adyar, Chennai (Default)', false);
    return this.currentLocation;
  },

  setManualLocation(lat, lng) {
    this.currentLocation = {
      lat: parseFloat(lat),
      lng: parseFloat(lng),
      source: 'manual'
    };
    this.updateLocationBadge(`Manual (${lat.toFixed(3)}, ${lng.toFixed(3)})`, false);
  },

  updateLocationBadge(label, isGps = false) {
    const badge = document.getElementById('location-status-badge');
    if (badge) {
      badge.innerHTML = `
        <span class="badge ${isGps ? 'bg-success' : 'bg-secondary'} py-2 px-3">
          <i class="bi ${isGps ? 'bi-geo-alt-fill' : 'bi-geo-alt'} me-1"></i> ${label}
        </span>
      `;
    }
  }
};
