/**
 * Emergency Medicine Availability Network - AI Health Assistant Chatbot (chatbot.js)
 * Implements conversational UI, responsive drawer, safe escaping, order cards, CSRF via api.js.
 */

const Chatbot = {
  isOpen: false,
  sessionId: null,
  historyKey: 'emergencymed_chatbot_history_v1',
  sessionKey: 'emergencymed_chatbot_session_id',
  lastFailedMessage: null,

  init() {
    this.sessionId = sessionStorage.getItem(this.sessionKey) || null;
    this.renderWidget();
    this.bindEvents();
    this.loadSavedHistory();
  },

  escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  },

  renderWidget() {
    if (document.getElementById('chatbot-launcher')) return;

    // 1. Floating Launcher Button
    const launcher = document.createElement('button');
    launcher.id = 'chatbot-launcher';
    launcher.className = 'chatbot-launcher-btn';
    launcher.setAttribute('aria-label', 'Open AI Health & Pharmacy Assistant');
    launcher.setAttribute('title', 'AI Health & Pharmacy Assistant');
    launcher.innerHTML = '<i class="bi bi-chat-dots-fill"></i>';
    document.body.appendChild(launcher);

    // 2. Chat Widget Window
    const widget = document.createElement('div');
    widget.id = 'chatbot-widget';
    widget.className = 'chatbot-widget d-none';
    widget.setAttribute('role', 'region');
    widget.setAttribute('aria-label', 'AI Health & Pharmacy Assistant Chat Window');

    widget.innerHTML = `
      <div class="chatbot-header">
        <div class="chatbot-header-info">
          <div class="chatbot-avatar">
            <i class="bi bi-robot"></i>
          </div>
          <div>
            <div class="chatbot-header-title">AI Health & Pharmacy Assistant</div>
            <div class="chatbot-header-status">
              <span class="status-dot"></span> Online &bull; Live Network
            </div>
          </div>
        </div>
        <div class="chatbot-header-actions">
          <button id="chatbot-btn-clear" class="chatbot-header-btn" title="Start New Conversation" aria-label="Start New Conversation">
            <i class="bi bi-arrow-counterclockwise"></i>
          </button>
          <button id="chatbot-btn-close" class="chatbot-header-btn" title="Close chat" aria-label="Close chat">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
      </div>

      <div class="chatbot-messages" id="chatbot-messages" role="log" aria-live="polite">
        <!-- Messages rendered here -->
      </div>

      <div class="chatbot-quick-replies" id="chatbot-quick-replies">
        <button class="quick-reply-chip" data-msg="I have a headache. What should I do?">Headache Advice</button>
        <button class="quick-reply-chip" data-msg="My hand is hurting. Is there any ointment?">Hand Pain / Ointment</button>
        <button class="quick-reply-chip" data-msg="Do you have Dolo?">Do you have Dolo?</button>
        <button class="quick-reply-chip" data-msg="Find Paracetamol in Adyar">Find Paracetamol</button>
        <button class="quick-reply-chip" data-msg="Order this medicine">Order Medicine</button>
        <button class="quick-reply-chip" data-msg="Track my order REQ-2026-001">Track My Order</button>
      </div>

      <div class="chatbot-footer">
        <textarea id="chatbot-input" class="chatbot-input" placeholder="Ask about symptoms, medicines, stock, orders..." rows="1" aria-label="Type your message"></textarea>
        <button id="chatbot-btn-send" class="chatbot-send-btn" aria-label="Send message" title="Send message">
          <i class="bi bi-send-fill"></i>
        </button>
      </div>
    `;

    document.body.appendChild(widget);
  },

  bindEvents() {
    const launcher = document.getElementById('chatbot-launcher');
    const closeBtn = document.getElementById('chatbot-btn-close');
    const clearBtn = document.getElementById('chatbot-btn-clear');
    const sendBtn = document.getElementById('chatbot-btn-send');
    const input = document.getElementById('chatbot-input');
    const quickReplies = document.getElementById('chatbot-quick-replies');

    launcher.addEventListener('click', () => this.toggleChat());
    closeBtn.addEventListener('click', () => this.toggleChat(false));
    clearBtn.addEventListener('click', () => this.clearChat());

    sendBtn.addEventListener('click', () => this.sendMessage());

    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        this.sendMessage();
      }
    });

    quickReplies.addEventListener('click', (e) => {
      const chip = e.target.closest('.quick-reply-chip');
      if (chip && chip.dataset.msg) {
        this.sendMessage(chip.dataset.msg);
      }
    });
  },

  toggleChat(forceState) {
    const widget = document.getElementById('chatbot-widget');
    const launcher = document.getElementById('chatbot-launcher');
    if (!widget) return;

    this.isOpen = typeof forceState === 'boolean' ? forceState : !this.isOpen;

    if (this.isOpen) {
      widget.classList.remove('d-none');
      launcher.innerHTML = '<i class="bi bi-chevron-down"></i>';
      document.getElementById('chatbot-input').focus();
      this.scrollToBottom();
    } else {
      widget.classList.add('d-none');
      launcher.innerHTML = '<i class="bi bi-chat-dots-fill"></i>';
    }
  },

  loadSavedHistory() {
    const saved = sessionStorage.getItem(this.historyKey);
    const container = document.getElementById('chatbot-messages');
    container.innerHTML = '';

    if (saved) {
      try {
        const msgs = JSON.parse(saved);
        if (Array.isArray(msgs) && msgs.length > 0) {
          msgs.forEach(m => this.appendMessageBubble(m.sender, m.text, m.data, false));
          this.scrollToBottom();
          return;
        }
      } catch (e) {
        console.warn('[Chatbot] Failed to parse saved session history:', e);
      }
    }

    this.appendBotWelcome();
  },

  appendBotWelcome() {
    const welcome = "Hi! Welcome to Emergency Medicine Availability Network. I am your AI Health and Pharmacy Assistant. I can help you evaluate symptoms, provide safe self-care guidance, check stock across nearby pharmacies, and place or track emergency medicine requests. How can I assist you today?";
    this.appendMessageBubble('bot', welcome, null, true);
  },

  async sendMessage(customText) {
    const input = document.getElementById('chatbot-input');
    const text = (customText || input.value || '').trim();
    if (!text) return;

    if (!customText) {
      input.value = '';
      input.style.height = '40px';
    }

    this.lastFailedMessage = null;

    // Append user message
    this.appendMessageBubble('user', text, null, true);
    this.showTypingIndicator();

    // Gather coordinates if available
    let lat = null;
    let lng = null;
    let locality = null;
    if (window.Geo && typeof Geo.getCoords === 'function') {
      const coords = Geo.getCoords();
      if (coords) {
        lat = coords.lat;
        lng = coords.lng;
      }
    }

    try {
      const payload = {
        message: text,
        sessionId: this.sessionId,
        lat,
        lng,
        locality
      };

      const csrf = (() => {
        const name = 'XSRF-TOKEN=';
        const ca = decodeURIComponent(document.cookie).split(';');
        for (let i = 0; i < ca.length; i++) {
          let c = ca[i].trim();
          if (c.indexOf(name) === 0) return c.substring(name.length, c.length);
        }
        return '';
      })();

      const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      };
      if (csrf) headers['X-XSRF-TOKEN'] = csrf;

      const response = await fetch('/api/v1/chatbot/message', {
        method: 'POST',
        headers,
        body: JSON.stringify(payload)
      });

      this.hideTypingIndicator();
      const res = await response.json().catch(() => null);

      if (response.ok && res && res.success && res.data) {
        const data = res.data;
        if (data.sessionId) {
          this.sessionId = data.sessionId;
          sessionStorage.setItem(this.sessionKey, this.sessionId);
        }
        this.appendMessageBubble('bot', data.replyText, data, true);
      } else {
        const errMsg = (res && res.message) ? res.message : 'Sorry, I could not process your request at this moment. Please try again.';
        this.appendMessageBubble('bot', errMsg, null, true);
      }
    } catch (err) {
      this.hideTypingIndicator();
      console.error('[Chatbot Error]', err);
      this.lastFailedMessage = text;
      let errMsg = 'Failed to connect to the health assistant. Please check your network or try again.';
      if (err.status === 429) {
        errMsg = 'Rate limit exceeded: Please wait a moment before sending another message.';
      } else if (err.message) {
        errMsg = err.message;
      }
      this.appendMessageBubble('bot', errMsg, { isError: true }, true);
    }
  },

  appendMessageBubble(sender, text, data = null, saveToStorage = true) {
    const container = document.getElementById('chatbot-messages');
    const row = document.createElement('div');
    row.className = `chat-row ${sender}`;

    const bubble = document.createElement('div');
    bubble.className = `chat-bubble ${sender}`;

    // Safely format linebreaks without raw innerHTML injection
    const escapedText = this.escapeHtml(text)
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>');
    bubble.innerHTML = `<div>${escapedText}</div>`;

    // Render structured elements if present
    if (data) {
      // 1. Emergency Alert Card
      if (data.emergencyAlert) {
        const alertEl = document.createElement('div');
        alertEl.className = 'emergency-alert-card';
        alertEl.innerHTML = `
          <div class="emergency-alert-title">
            <i class="bi bi-exclamation-octagon-fill"></i> ${this.escapeHtml(data.emergencyAlert.title)}
          </div>
          <p class="mb-2" style="font-size: 0.82rem;">${this.escapeHtml(data.emergencyAlert.description)}</p>
          <div>
            <a href="tel:108" class="emergency-dial-btn"><i class="bi bi-telephone-fill"></i> Call Ambulance (108)</a>
            <a href="tel:112" class="emergency-dial-btn" style="background-color: #102A43;"><i class="bi bi-telephone-fill"></i> National Helpline (112)</a>
          </div>
        `;
        bubble.appendChild(alertEl);
      }

      // 2. Pharmacy Cards
      if (Array.isArray(data.pharmacyCards) && data.pharmacyCards.length > 0) {
        data.pharmacyCards.forEach(p => {
          const card = document.createElement('div');
          card.className = 'pharmacy-card-chat';

          let badgeClass = 'badge-unverified';
          if (p.dataSource === 'LIVE') badgeClass = 'badge-live';
          else if (p.dataSource === 'SAMPLE') badgeClass = 'badge-demo';

          card.innerHTML = `
            <div class="pharmacy-card-chat-title">
              <span>${this.escapeHtml(p.name)}</span>
              <span class="pharmacy-chat-badge ${badgeClass}">${this.escapeHtml(p.dataFreshnessLabel || p.dataSource)}</span>
            </div>
            <div class="pharmacy-chat-details">
              <div><i class="bi bi-geo-alt-fill text-danger me-1"></i> ${this.escapeHtml(p.address)}</div>
              <div class="d-flex justify-content-between">
                <span><strong>Distance:</strong> ${p.distanceKm ? p.distanceKm.toFixed(1) + ' km' : '—'} (${p.travelTimeMinutes || '—'} mins)</span>
                <span><strong>Stock:</strong> ${p.availableQuantity || 0} units</span>
              </div>
              <div class="d-flex justify-content-between align-items-center">
                <span class="pharmacy-chat-price">₹${p.unitPrice ? Number(p.unitPrice).toFixed(2) : '0.00'}</span>
                <span><i class="bi bi-clock me-1"></i> ${p.openNow ? '<span class="text-success font-weight-bold">Open</span>' : '<span class="text-danger font-weight-bold">Closed</span>'}</span>
              </div>
              <small class="text-muted"><i class="bi bi-info-circle me-1"></i> ${this.escapeHtml(p.deliveryAvailable ? 'Delivery Available' : 'Self-Pickup Only')}</small>
            </div>
            <div class="pharmacy-chat-actions">
              ${p.callUrl ? `<a href="${this.escapeHtml(p.callUrl)}" class="chat-btn chat-btn-outline-navy"><i class="bi bi-telephone"></i> Call</a>` : ''}
              ${p.whatsappUrl ? `<a href="${this.escapeHtml(p.whatsappUrl)}" target="_blank" class="chat-btn chat-btn-teal"><i class="bi bi-whatsapp"></i> WhatsApp</a>` : ''}
              ${p.directionsUrl ? `<a href="${this.escapeHtml(p.directionsUrl)}" target="_blank" class="chat-btn chat-btn-outline-navy"><i class="bi bi-compass"></i> Navigate</a>` : ''}
              ${p.detailsUrl ? `<a href="${this.escapeHtml(p.detailsUrl)}" class="chat-btn chat-btn-outline-navy"><i class="bi bi-eye"></i> Details</a>` : ''}
              <button type="button" class="chat-btn chat-btn-emergency" onclick="Chatbot.sendMessage('Order from ${this.escapeHtml(p.name)}')"><i class="bi bi-cart-plus-fill"></i> Order</button>
            </div>
          `;
          bubble.appendChild(card);
        });
      }

      // 3. Generic Substitutes
      if (Array.isArray(data.substitutes) && data.substitutes.length > 0) {
        const subBox = document.createElement('div');
        subBox.className = 'substitute-chip';
        subBox.innerHTML = `<strong>Generic Substitutes Available:</strong><br>`;
        data.substitutes.forEach(s => {
          subBox.innerHTML += `&bull; ${this.escapeHtml(s.name)} (${this.escapeHtml(s.strength || '')})<br>`;
        });
        subBox.innerHTML += `<small class="text-danger font-weight-bold mt-1 d-block"><i class="bi bi-exclamation-triangle-fill"></i> Confirm with a doctor or pharmacist before using a substitute.</small>`;
        bubble.appendChild(subBox);
      }

      // 4. Orders
      if (Array.isArray(data.orders) && data.orders.length > 0) {
        data.orders.forEach(o => {
          const oEl = document.createElement('div');
          oEl.className = 'pharmacy-card-chat mt-2 border-primary';

          let statusClass = 'bg-primary';
          if (o.status === 'CONFIRMED' || o.status === 'DELIVERED') statusClass = 'bg-success';
          else if (o.status === 'PENDING_PRESCRIPTION') statusClass = 'bg-warning text-dark';

          oEl.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-1 pb-1 border-bottom">
              <strong class="text-navy"><i class="bi bi-receipt me-1"></i> ${this.escapeHtml(o.referenceCode)}</strong>
              <span class="badge ${statusClass}">${this.escapeHtml(o.status)}</span>
            </div>
            <div style="font-size: 0.82rem;" class="mt-1">
              <div><strong>Medicine:</strong> ${this.escapeHtml(o.medicineName)} (${o.quantity} units)</div>
              <div><strong>Pharmacy:</strong> ${this.escapeHtml(o.pharmacyName)}</div>
              <div><strong>Payment:</strong> ${this.escapeHtml(o.paymentMode)}</div>
              ${o.handoverOtp ? `<div class="mt-1"><strong>Handover OTP:</strong> <span class="badge bg-dark px-2 py-1">${this.escapeHtml(o.handoverOtp)}</span></div>` : ''}
              ${o.deliveryPartnerName ? `<div><strong>Partner:</strong> ${this.escapeHtml(o.deliveryPartnerName)}</div>` : ''}
              <small class="text-muted d-block mt-1"><i class="bi bi-clock me-1"></i> ${this.escapeHtml(o.createdAt || '')}</small>
            </div>
          `;
          bubble.appendChild(oEl);
        });
      }

      // 5. Retry button if error
      if (data.isError && this.lastFailedMessage) {
        const retryBtn = document.createElement('button');
        retryBtn.className = 'btn btn-sm btn-outline-danger mt-2';
        retryBtn.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i> Retry Message';
        retryBtn.onclick = () => {
          if (this.lastFailedMessage) {
            const msg = this.lastFailedMessage;
            this.lastFailedMessage = null;
            this.sendMessage(msg);
          }
        };
        bubble.appendChild(retryBtn);
      }
    }

    // Interactive Action Buttons if bot asks for check or confirmation
    if (sender === 'bot') {
      if (text.includes('Would you like to confirm this order?')) {
        const actions = document.createElement('div');
        actions.className = 'd-flex gap-2 mt-2 pt-1 border-top';
        actions.innerHTML = `
          <button class="chat-btn chat-btn-teal flex-grow-1" onclick="Chatbot.sendMessage('Yes, confirm order')">
            <i class="bi bi-check-circle-fill me-1"></i> Confirm Order
          </button>
          <button class="chat-btn chat-btn-outline-navy flex-grow-1" onclick="Chatbot.sendMessage('Cancel order')">
            <i class="bi bi-x-circle me-1"></i> Cancel
          </button>
        `;
        bubble.appendChild(actions);
      } else if (text.includes('Would you like me to check nearby pharmacies')) {
        const actions = document.createElement('div');
        actions.className = 'd-flex gap-2 mt-2 pt-1 border-top';
        actions.innerHTML = `
          <button class="chat-btn chat-btn-teal flex-grow-1" onclick="Chatbot.sendMessage('Yes, check nearby pharmacies')">
            <i class="bi bi-search me-1"></i> Check Nearby Pharmacies
          </button>
        `;
        bubble.appendChild(actions);
      }
    }

    const timeEl = document.createElement('span');
    timeEl.className = 'chat-time';
    timeEl.textContent = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    bubble.appendChild(timeEl);

    row.appendChild(bubble);
    container.appendChild(row);
    this.scrollToBottom();

    if (saveToStorage) {
      this.saveHistoryItem({ sender, text, data });
    }
  },

  showTypingIndicator() {
    this.hideTypingIndicator();
    const container = document.getElementById('chatbot-messages');
    const indicator = document.createElement('div');
    indicator.id = 'chatbot-typing-indicator';
    indicator.className = 'chat-row bot';
    indicator.innerHTML = `
      <div class="typing-indicator" aria-label="Assistant is typing...">
        <div class="typing-dot"></div>
        <div class="typing-dot"></div>
        <div class="typing-dot"></div>
      </div>
    `;
    container.appendChild(indicator);
    this.scrollToBottom();
  },

  hideTypingIndicator() {
    const el = document.getElementById('chatbot-typing-indicator');
    if (el) el.remove();
  },

  scrollToBottom() {
    const container = document.getElementById('chatbot-messages');
    if (container) {
      container.scrollTop = container.scrollHeight;
    }
  },

  saveHistoryItem(item) {
    let list = [];
    try {
      const raw = sessionStorage.getItem(this.historyKey);
      if (raw) list = JSON.parse(raw);
    } catch (e) {}
    list.push(item);
    if (list.length > 30) list.shift();
    sessionStorage.setItem(this.historyKey, JSON.stringify(list));
  },

  async clearChat() {
    sessionStorage.removeItem(this.historyKey);
    sessionStorage.removeItem(this.sessionKey);
    this.sessionId = null;
    this.lastFailedMessage = null;
    const container = document.getElementById('chatbot-messages');
    container.innerHTML = '';

    // Clear on server if user is logged in
    try {
      await fetch('/api/v1/chatbot/history', { method: 'DELETE' });
    } catch (ignored) {}

    this.appendBotWelcome();
  }
};

// Auto-initialize when DOM is ready or immediately if already loaded
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    Chatbot.init();
  });
} else {
  Chatbot.init();
}
