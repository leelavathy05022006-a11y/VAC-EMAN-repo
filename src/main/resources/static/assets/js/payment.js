// Module 5 - Payment Management Client
// Gateway-Independent Checkout and Payment Handling

const paymentManager = {
  currentOrder: null,
  currentPayment: null,

  async initCheckout() {
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('orderId') || urlParams.get('id');

    if (!orderId) {
      ui.showError('No emergency order ID provided.');
      return;
    }

    try {
      ui.showLoading();
      const res = await api.request(`/api/v1/orders/${orderId}/checkout-summary`);
      if (res && res.success && res.data) {
        this.currentOrder = res.data;
        this.renderCheckoutSummary(res.data);
      } else {
        ui.showError(res.message || 'Failed to load order checkout details');
      }
    } catch (err) {
      ui.showError(err.message || 'Network error loading checkout');
    } finally {
      ui.hideLoading();
    }
  },

  renderCheckoutSummary(data) {
    document.getElementById('orderRefText').innerText = data.referenceCode || '#' + data.orderId;
    document.getElementById('pharmacyNameText').innerText = data.pharmacyName || 'Pharmacy #' + data.pharmacyId;
    document.getElementById('fulfilmentTypeText').innerText = data.fulfilmentType || 'DELIVERY';
    document.getElementById('orderStatusBadge').innerText = data.orderStatus || 'PENDING';

    // Render items
    const itemsTable = document.getElementById('checkoutItemsBody');
    if (itemsTable && data.items) {
      itemsTable.innerHTML = data.items.map(it => `
        <tr>
          <td>
            <div class="fw-semibold text-dark">${this.escape(it.medicineName)}</div>
          </td>
          <td class="text-center">${it.quantity}</td>
          <td class="text-end">₹${parseFloat(it.unitPrice).toFixed(2)}</td>
          <td class="text-end fw-semibold">₹${parseFloat(it.totalPrice).toFixed(2)}</td>
        </tr>
      `).join('');
    }

    // Financial totals
    document.getElementById('medicineCostText').innerText = '₹' + parseFloat(data.medicineCost || 0).toFixed(2);
    document.getElementById('deliveryFeeText').innerText = '₹' + parseFloat(data.deliveryFee || 0).toFixed(2);
    document.getElementById('grandTotalText').innerText = '₹' + parseFloat(data.totalAmount || 0).toFixed(2);

    // If order already has a PAID payment
    if (data.existingPayment && data.existingPayment.paymentStatus === 'PAID') {
      const alertBox = document.getElementById('alreadyPaidAlert');
      if (alertBox) alertBox.classList.remove('d-none');
      document.getElementById('paymentMethodForm').classList.add('d-none');
      document.getElementById('btnPayNow').classList.add('d-none');
      return;
    }

    // COD Option handling
    const codOption = document.getElementById('method-cod-container');
    if (codOption) {
      if (data.codSupported) {
        codOption.classList.remove('d-none');
      } else {
        codOption.classList.add('d-none');
      }
    }
  },

  async handlePaymentSubmit() {
    if (!this.currentOrder) return;

    const selectedMethod = document.querySelector('input[name="paymentMethod"]:checked');
    if (!selectedMethod) {
      ui.showError('Please select a payment method.');
      return;
    }

    const method = selectedMethod.value;
    const idempotencyKey = 'IDEM-' + this.currentOrder.orderId + '-' + method + '-' + Date.now();

    try {
      ui.showLoading();
      const payload = {
        orderId: this.currentOrder.orderId,
        paymentMethod: method,
        idempotencyKey: idempotencyKey
      };

      const res = await api.request('/api/v1/payments', {
        method: 'POST',
        body: payload
      });

      if (res && res.success && res.data) {
        this.currentPayment = res.data;

        if (method === 'COD') {
          // Cash on Delivery is PENDING until cash collection
          window.location.href = `/payment-status.html?ref=${res.data.paymentReference}&method=COD`;
        } else if (method === 'UPI_QR') {
          this.showQrModal(res.data);
        } else {
          this.showDemoGatewayModal(res.data);
        }
      } else {
        ui.showError(res.message || 'Could not initiate payment');
      }
    } catch (err) {
      ui.showError(err.message || 'Payment initiation failed');
    } finally {
      ui.hideLoading();
    }
  },

  showQrModal(payment) {
    const modalEl = document.getElementById('qrPaymentModal');
    if (!modalEl) return;

    document.getElementById('qrRefCode').innerText = payment.paymentReference;
    document.getElementById('qrAmount').innerText = '₹' + parseFloat(payment.amount).toFixed(2);

    // Render QR Code (simple SVG QR visualizer / Google Chart API fallback)
    const qrContainer = document.getElementById('qrImageContainer');
    const qrData = payment.qrCodeData || `upi://pay?pa=demo@emergencymed&pn=EmergencyMedDemo&am=${payment.amount}&cu=INR&tr=${payment.paymentReference}`;
    const qrUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=' + encodeURIComponent(qrData);
    qrContainer.innerHTML = `<img src="${qrUrl}" alt="UPI QR Code" class="img-fluid rounded border shadow-sm p-2 bg-white" style="width:200px; height:200px;">`;

    const bsModal = new bootstrap.Modal(modalEl);
    bsModal.show();
  },

  showDemoGatewayModal(payment) {
    const modalEl = document.getElementById('demoGatewayModal');
    if (!modalEl) return;

    document.getElementById('gatewayRefCode').innerText = payment.paymentReference;
    document.getElementById('gatewayMethodBadge').innerText = payment.paymentMethod;
    document.getElementById('gatewayAmount').innerText = '₹' + parseFloat(payment.amount).toFixed(2);

    const bsModal = new bootstrap.Modal(modalEl);
    bsModal.show();
  },

  async verifyPayment(simulatedOutcome) {
    if (!this.currentPayment) return;

    try {
      ui.showLoading();
      const payload = {
        paymentReference: this.currentPayment.paymentReference,
        gatewayTransactionId: this.currentPayment.gatewayTransactionId || 'TXN-DEMO-' + Date.now(),
        signature: 'DEMO_SIGNATURE_' + Date.now(),
        simulatedOutcome: simulatedOutcome || 'SUCCESS'
      };

      const res = await api.request('/api/v1/payments/verify', {
        method: 'POST',
        body: payload
      });

      if (res && res.success && res.data) {
        window.location.href = `/payment-status.html?ref=${res.data.paymentReference}`;
      } else {
        ui.showError(res.message || 'Payment verification failed');
      }
    } catch (err) {
      ui.showError(err.message || 'Verification failed');
    } finally {
      ui.hideLoading();
    }
  },

  async initStatusPage() {
    const urlParams = new URLSearchParams(window.location.search);
    const ref = urlParams.get('ref');
    const paymentId = urlParams.get('id');

    if (!ref && !paymentId) {
      ui.showError('No payment reference found');
      return;
    }

    try {
      ui.showLoading();
      const endpoint = ref ? `/api/v1/payments?ref=${ref}` : `/api/v1/payments/${paymentId}`;
      let paymentData = null;

      if (ref) {
        // Query payments/my or use order endpoint
        const myRes = await api.request('/api/v1/payments/my');
        if (myRes && myRes.success && myRes.data) {
          paymentData = myRes.data.find(p => p.paymentReference === ref);
        }
      }

      if (!paymentData && paymentId) {
        const idRes = await api.request(`/api/v1/payments/${paymentId}`);
        if (idRes && idRes.success) paymentData = idRes.data;
      }

      if (paymentData) {
        this.renderPaymentStatus(paymentData);
      } else {
        // Fallback display
        document.getElementById('statusPaymentRef').innerText = ref || paymentId;
      }
    } catch (err) {
      ui.showError(err.message || 'Failed to load payment status');
    } finally {
      ui.hideLoading();
    }
  },

  renderPaymentStatus(p) {
    this.currentPayment = p;
    document.getElementById('statusPaymentRef').innerText = p.paymentReference;
    document.getElementById('statusAmount').innerText = '₹' + parseFloat(p.amount).toFixed(2);
    document.getElementById('statusMethod').innerText = p.paymentMethod;
    document.getElementById('statusGateway').innerText = p.gatewayProvider || 'DEMO';
    document.getElementById('statusDate').innerText = p.paidAt ? new Date(p.paidAt).toLocaleString() : new Date(p.createdAt).toLocaleString();

    const orderLink = document.getElementById('linkTrackOrder');
    if (orderLink && p.orderId) {
      orderLink.href = `/request/track.html?id=${p.orderId}`;
    }

    const badge = document.getElementById('paymentStatusBadge');
    const title = document.getElementById('statusTitle');
    const desc = document.getElementById('statusDescription');
    const retryBtn = document.getElementById('btnRetryPayment');

    if (p.paymentStatus === 'PAID') {
      badge.className = 'badge bg-success-subtle text-success fs-6 px-3 py-2 border border-success';
      badge.innerText = 'PAID (Verified)';
      title.innerText = 'Payment Successful';
      title.className = 'text-success fw-bold fs-3 mb-2';
      desc.innerText = 'Your payment has been verified by the backend. The pharmacy and runner have been notified.';
      if (retryBtn) retryBtn.classList.add('d-none');
    } else if (p.paymentStatus === 'PENDING') {
      badge.className = 'badge bg-warning-subtle text-warning-emphasis fs-6 px-3 py-2 border border-warning';
      badge.innerText = p.paymentMethod === 'COD' ? 'PENDING (Pay on Delivery)' : 'PENDING';
      title.innerText = p.paymentMethod === 'COD' ? 'Cash on Delivery Confirmed' : 'Payment Processing';
      title.className = 'text-warning-emphasis fw-bold fs-3 mb-2';
      desc.innerText = p.paymentMethod === 'COD' 
        ? 'Please have exact cash ready during delivery/pickup handover.' 
        : 'Payment is awaiting confirmation from the bank or payment provider.';
      if (retryBtn) retryBtn.classList.add('d-none');
    } else if (p.paymentStatus === 'FAILED') {
      badge.className = 'badge bg-danger-subtle text-danger fs-6 px-3 py-2 border border-danger';
      badge.innerText = 'FAILED';
      title.innerText = 'Payment Failed';
      title.className = 'text-danger fw-bold fs-3 mb-2';
      desc.innerText = p.failureReason || 'Transaction was declined or cancelled. You can retry with a different method.';
      if (retryBtn) retryBtn.classList.remove('d-none');
    }
  },

  async handleRetry() {
    if (!this.currentPayment) return;
    try {
      ui.showLoading();
      const res = await api.request(`/api/v1/payments/${this.currentPayment.id}/retry`, {
        method: 'POST'
      });
      if (res && res.success && res.data) {
        window.location.href = `/checkout.html?orderId=${this.currentPayment.orderId}`;
      } else {
        ui.showError(res.message || 'Retry failed');
      }
    } catch (err) {
      ui.showError(err.message || 'Retry error');
    } finally {
      ui.hideLoading();
    }
  },

  escape(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }
};
