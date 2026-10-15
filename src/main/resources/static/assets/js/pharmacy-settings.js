document.addEventListener('DOMContentLoaded', () => {
  loadSettings();

  const opens24x7 = document.getElementById('opens24x7');
  if (opens24x7) {
    opens24x7.addEventListener('change', (e) => {
      const hoursFields = document.getElementById('hours-fields');
      if (e.target.checked) {
        hoursFields.classList.add('opacity-50');
      } else {
        hoursFields.classList.remove('opacity-50');
      }
      updatePreview();
    });
  }

  const deliveryEnabled = document.getElementById('deliveryEnabled');
  if (deliveryEnabled) {
    deliveryEnabled.addEventListener('change', updatePreview);
  }

  ['contactPhone', 'whatsappNumber', 'deliveryRadiusKm', 'deliveryFee', 'openingTime', 'closingTime', 'acceptsEmergencyOrders'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.addEventListener('input', updatePreview);
  });

  const form = document.getElementById('settingsForm');
  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearValidationErrors('settingsForm');

      const whatsapp = document.getElementById('whatsappNumber').value.trim();
      if (whatsapp && (whatsapp.includes('+') || !/^[0-9]{11,13}$/.test(whatsapp))) {
        showToast('WhatsApp number must be 11-13 digits with country code and no + sign', 'danger');
        return;
      }

      const is24x7 = document.getElementById('opens24x7').checked;
      const openT = document.getElementById('openingTime').value;
      const closeT = document.getElementById('closingTime').value;
      if (!is24x7 && openT && closeT && closeT <= openT) {
        showToast('Closing time must be after opening time', 'danger');
        return;
      }

      const body = {
        contactPhone: document.getElementById('contactPhone').value || null,
        whatsappNumber: whatsapp || null,
        deliveryEnabled: document.getElementById('deliveryEnabled').checked,
        deliveryRadiusKm: parseFloat(document.getElementById('deliveryRadiusKm').value) || 5.0,
        deliveryFee: parseFloat(document.getElementById('deliveryFee').value) || 20.0,
        opens24x7: is24x7,
        openingTime: openT ? openT + ':00' : null,
        closingTime: closeT ? closeT + ':00' : null,
        acceptsEmergencyOrders: document.getElementById('acceptsEmergencyOrders').checked
      };

      try {
        await apiRequest('/pharmacy-settings/me', { method: 'PUT', body: JSON.stringify(body) });
        showToast('Pharmacy settings saved successfully!');
        loadSettings();
      } catch (err) {
        showToast(err.message, 'danger');
      }
    });
  }
});

async function loadSettings() {
  try {
    const data = await apiRequest('/pharmacy-settings/me');
    document.getElementById('contactPhone').value = data.contactPhone || '';
    document.getElementById('whatsappNumber').value = data.whatsappNumber || '';
    document.getElementById('deliveryEnabled').checked = !!data.deliveryEnabled;
    document.getElementById('deliveryRadiusKm').value = data.deliveryRadiusKm || 5.0;
    document.getElementById('deliveryFee').value = data.deliveryFee || 20.0;
    document.getElementById('opens24x7').checked = !!data.opens24x7;
    document.getElementById('openingTime').value = data.openingTime ? data.openingTime.substring(0, 5) : '';
    document.getElementById('closingTime').value = data.closingTime ? data.closingTime.substring(0, 5) : '';
    document.getElementById('acceptsEmergencyOrders').checked = data.acceptsEmergencyOrders !== false;

    updatePreview();
  } catch (err) {
    console.error('Failed to load settings', err);
  }
}

function updatePreview() {
  const phone = document.getElementById('contactPhone').value || 'Not provided';
  const whatsapp = document.getElementById('whatsappNumber').value || 'Not provided';
  const delEnabled = document.getElementById('deliveryEnabled').checked;
  const radius = document.getElementById('deliveryRadiusKm').value || '5.0';
  const fee = document.getElementById('deliveryFee').value || '20.0';
  const is24x7 = document.getElementById('opens24x7').checked;
  const openT = document.getElementById('openingTime').value;
  const closeT = document.getElementById('closingTime').value;
  const emergency = document.getElementById('acceptsEmergencyOrders').checked;

  document.getElementById('prev-phone').innerText = phone;
  document.getElementById('prev-whatsapp').innerText = whatsapp;
  document.getElementById('prev-delivery').innerText = delEnabled ? `Enabled (Up to ${radius} km, Fee ₹${fee})` : 'Disabled (Self-pickup only)';
  document.getElementById('prev-hours').innerText = is24x7 ? 'Open 24x7' : (openT && closeT ? `${openT} - ${closeT}` : 'Not set');
  document.getElementById('prev-emergency').innerText = emergency ? 'Accepted' : 'Not accepted';
}
