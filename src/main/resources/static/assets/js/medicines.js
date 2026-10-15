let categoriesCache = [];

document.addEventListener('DOMContentLoaded', () => {
  loadCategories();
  loadMedicines();

  const searchInput = document.getElementById('med-search');
  if (searchInput) {
    let timeout = null;
    searchInput.addEventListener('input', (e) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => {
        loadMedicines(e.target.value);
      }, 300);
    });
  }

  setupMedicineForm();
  setupCategoryForm();
});

async function loadCategories() {
  try {
    categoriesCache = await apiRequest('/categories');
    renderCategoriesTable();
    populateCategoryDropdown();
  } catch (err) {
    console.error('Failed to load categories', err);
  }
}

function renderCategoriesTable() {
  const tbody = document.getElementById('categories-tbody');
  if (!tbody) return;
  if (categoriesCache.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted p-4">No categories found</td></tr>';
    return;
  }
  tbody.innerHTML = categoriesCache.map(c => `
    <tr>
      <td>${c.id}</td>
      <td class="fw-bold">${c.name}</td>
      <td>${c.description || '-'}</td>
      <td>${c.isActive ? '<span class="badge bg-success">Active</span>' : '<span class="badge bg-secondary">Inactive</span>'}</td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-primary" onclick="openEditCategoryModal(${c.id})">Edit</button>
      </td>
    </tr>
  `).join('');
}

function populateCategoryDropdown() {
  const select = document.getElementById('med-category');
  if (!select) return;
  select.innerHTML = categoriesCache.filter(c => c.isActive).map(c => `
    <option value="${c.id}">${c.name}</option>
  `).join('');
}

async function loadMedicines(q = '') {
  const tbody = document.getElementById('medicines-tbody');
  if (!tbody) return;
  tbody.innerHTML = renderSkeletonRows(9, 5);

  try {
    let url = '/medicines?size=50';
    if (q) url += `&q=${encodeURIComponent(q)}`;
    const pageData = await apiRequest(url);
    const list = pageData.content || [];

    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted p-4">No medicines found</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(m => `
      <tr>
        <td>${m.id}</td>
        <td class="fw-bold">${m.name}</td>
        <td class="text-muted">${m.genericName}</td>
        <td><span class="badge bg-light text-dark">${m.categoryName || '-'}</span></td>
        <td>${m.strength} (${m.dosageForm})</td>
        <td>${m.manufacturer}</td>
        <td>${m.requiresPrescription ? '<span class="badge-rx">Rx Required</span>' : '<span class="text-muted">OTC</span>'}</td>
        <td>${m.isActive ? '<span class="badge bg-success">Active</span>' : '<span class="badge bg-secondary">Inactive</span>'}</td>
        <td class="text-end">
          <div class="btn-group">
            <button class="btn btn-sm btn-outline-primary" onclick="openEditMedicineModal(${m.id})">Edit</button>
            <button class="btn btn-sm btn-outline-${m.isActive ? 'warning' : 'success'}" onclick="toggleMedicineStatus(${m.id}, ${!m.isActive})">
              ${m.isActive ? 'Deactivate' : 'Activate'}
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="9" class="text-center text-danger p-4">${err.message}</td></tr>`;
  }
}

function openAddMedicineModal() {
  document.getElementById('medModalTitle').innerText = 'Add New Medicine';
  document.getElementById('med-id').value = '';
  document.getElementById('medicineForm').reset();
  const modal = new bootstrap.Modal(document.getElementById('medicineModal'));
  modal.show();
}

async function openEditMedicineModal(id) {
  try {
    const m = await apiRequest(`/medicines/${id}`);
    document.getElementById('medModalTitle').innerText = 'Edit Medicine';
    document.getElementById('med-id').value = m.id;
    document.getElementById('med-name').value = m.name;
    document.getElementById('med-generic').value = m.genericName;
    document.getElementById('med-category').value = m.categoryId;
    document.getElementById('med-strength').value = m.strength;
    document.getElementById('med-dosage').value = m.dosageForm;
    document.getElementById('med-manufacturer').value = m.manufacturer;
    document.getElementById('med-rx').checked = !!m.requiresPrescription;

    const modal = new bootstrap.Modal(document.getElementById('medicineModal'));
    modal.show();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

function setupMedicineForm() {
  const form = document.getElementById('medicineForm');
  if (!form) return;
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('med-id').value;
    const body = {
      categoryId: parseInt(document.getElementById('med-category').value),
      name: document.getElementById('med-name').value,
      genericName: document.getElementById('med-generic').value,
      strength: document.getElementById('med-strength').value,
      dosageForm: document.getElementById('med-dosage').value,
      manufacturer: document.getElementById('med-manufacturer').value,
      requiresPrescription: document.getElementById('med-rx').checked,
      isActive: true
    };

    try {
      if (id) {
        await apiRequest(`/medicines/${id}`, { method: 'PUT', body: JSON.stringify(body) });
        showToast('Medicine updated successfully');
      } else {
        await apiRequest('/medicines', { method: 'POST', body: JSON.stringify(body) });
        showToast('Medicine created successfully');
      }
      bootstrap.Modal.getInstance(document.getElementById('medicineModal')).hide();
      loadMedicines();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

async function toggleMedicineStatus(id, active) {
  try {
    await apiRequest(`/medicines/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ isActive: active })
    });
    showToast(`Medicine status set to ${active ? 'Active' : 'Inactive'}`);
    loadMedicines();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

function openAddCategoryModal() {
  document.getElementById('catModalTitle').innerText = 'Add Category';
  document.getElementById('cat-id').value = '';
  document.getElementById('categoryForm').reset();
  const modal = new bootstrap.Modal(document.getElementById('categoryModal'));
  modal.show();
}

function openEditCategoryModal(id) {
  const cat = categoriesCache.find(c => c.id === id);
  if (!cat) return;
  document.getElementById('catModalTitle').innerText = 'Edit Category';
  document.getElementById('cat-id').value = cat.id;
  document.getElementById('cat-name').value = cat.name;
  document.getElementById('cat-desc').value = cat.description || '';
  const modal = new bootstrap.Modal(document.getElementById('categoryModal'));
  modal.show();
}

function setupCategoryForm() {
  const form = document.getElementById('categoryForm');
  if (!form) return;
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('cat-id').value;
    const body = {
      name: document.getElementById('cat-name').value,
      description: document.getElementById('cat-desc').value,
      isActive: true
    };

    try {
      if (id) {
        await apiRequest(`/categories/${id}`, { method: 'PUT', body: JSON.stringify(body) });
        showToast('Category updated');
      } else {
        await apiRequest('/categories', { method: 'POST', body: JSON.stringify(body) });
        showToast('Category created');
      }
      bootstrap.Modal.getInstance(document.getElementById('categoryModal')).hide();
      loadCategories();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}
