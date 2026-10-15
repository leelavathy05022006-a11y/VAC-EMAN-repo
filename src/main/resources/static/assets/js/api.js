// Shared API Client
// Handles CSRF tokens, headers, and standard response envelopes

const api = {
  getCsrfToken() {
    const name = 'XSRF-TOKEN=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
      let c = ca[i].trim();
      if (c.indexOf(name) === 0) {
        return c.substring(name.length, c.length);
      }
    }
    return '';
  },

  async request(url, options = {}) {
    let targetUrl = url;
    if (targetUrl && !targetUrl.startsWith('/api/v1') && !targetUrl.startsWith('/api/') && targetUrl.startsWith('/')) {
      targetUrl = '/api/v1' + targetUrl;
    }

    const defaultHeaders = {
      'Accept': 'application/json'
    };

    const method = (options.method || 'GET').toUpperCase();
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
      const csrf = this.getCsrfToken();
      if (csrf) {
        defaultHeaders['X-XSRF-TOKEN'] = csrf;
      }
    }

    if (options.body && !(options.body instanceof FormData)) {
      defaultHeaders['Content-Type'] = 'application/json';
      options.body = JSON.stringify(options.body);
    }

    options.headers = {
      ...defaultHeaders,
      ...options.headers
    };

    try {
      const response = await fetch(targetUrl, options);

      // Handle raw file downloads
      if (options.responseType === 'blob') {
        if (!response.ok) {
          throw new Error('Failed to download file');
        }
        return await response.blob();
      }

      const json = await response.json().catch(() => null);

      if (!response.ok) {
        let errorMsg = 'An error occurred';
        if (json && json.message) {
          errorMsg = json.message;
        }
        if (json && json.errors && json.errors.length > 0) {
          errorMsg += ': ' + json.errors.map(e => e.message || e.field).join(', ');
        }
        const err = new Error(errorMsg);
        err.status = response.status;
        err.payload = json;
        throw err;
      }

      return json ? json.data : null;
    } catch (err) {
      console.error(`API Error [${options.method || 'GET'} ${url}]:`, err);
      throw err;
    }
  },

  get(url, params = {}) {
    const urlObj = new URL(url, window.location.origin);
    Object.keys(params).forEach(k => {
      if (params[k] !== undefined && params[k] !== null && params[k] !== '') {
        urlObj.searchParams.append(k, params[k]);
      }
    });
    return this.request(urlObj.pathname + urlObj.search, { method: 'GET' });
  },

  post(url, body) {
    return this.request(url, { method: 'POST', body });
  },

  put(url, body) {
    return this.request(url, { method: 'PUT', body });
  },

  patch(url, body) {
    return this.request(url, { method: 'PATCH', body });
  },

  delete(url) {
    return this.request(url, { method: 'DELETE' });
  },

  upload(url, formData) {
    return this.request(url, {
      method: 'POST',
      body: formData
    });
  }
};

window.api = api;
window.Api = api;

window.apiRequest = async function(url, options = {}) {
  let targetUrl = url;
  if (!targetUrl.startsWith('/api/v1') && !targetUrl.startsWith('/api/') && targetUrl.startsWith('/')) {
    targetUrl = '/api/v1' + targetUrl;
  }
  return await api.request(targetUrl, options);
};

if (!window.UI) {
  window.UI = {
    showToast(msg, type = 'info') {
      if (typeof window.showToast === 'function') {
        window.showToast(msg, type);
      } else {
        console.log(`[Toast ${type}] ${msg}`);
      }
    },
    toast(msg, type = 'info') {
      if (typeof window.showToast === 'function') {
        window.showToast(msg, type);
      } else {
        console.log(`[Toast ${type}] ${msg}`);
      }
    }
  };
}

