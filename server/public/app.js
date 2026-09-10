// State management
let authToken = localStorage.getItem('samp_admin_token') || '';

// DOM Elements
const loginOverlay = document.getElementById('login-overlay');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const dashboardApp = document.getElementById('dashboard-app');
const btnLogout = document.getElementById('btn-logout');

const metricVersion = document.getElementById('metric-version');
const metricFilename = document.getElementById('metric-filename');
const metricSize = document.getElementById('metric-size');
const metricChecksum = document.getElementById('metric-checksum');
const metricTotalReleases = document.getElementById('metric-total-releases');
const metricTotalStorage = document.getElementById('metric-total-storage');

const uploadForm = document.getElementById('upload-form');
const uploadFileInput = document.getElementById('upload-file');
const fileChosenText = document.getElementById('file-chosen-text');
const uploadProgressContainer = document.getElementById('upload-progress-container');
const uploadProgressFill = document.getElementById('upload-progress-fill');
const uploadProgressPercent = document.getElementById('upload-progress-percent');
const uploadProgressStatus = document.getElementById('upload-progress-status');
const uploadAlert = document.getElementById('upload-alert');

const apiResponseJson = document.getElementById('api-response-json');
const btnRefreshApi = document.getElementById('btn-refresh-api');
const btnCopyUrl = document.getElementById('btn-copy-url');
const apiUrlText = document.getElementById('api-url-text');

const releasesTableBody = document.getElementById('releases-table-body');
const btnRefreshReleases = document.getElementById('btn-refresh-releases');

// Helper to format bytes
function formatBytes(bytes) {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Check session on load
window.addEventListener('DOMContentLoaded', () => {
  if (authToken) {
    showDashboard();
  } else {
    showLogin();
  }

  // Update displayed API host
  const fullEndpoint = window.location.origin + '/api/data-file/latest';
  apiUrlText.textContent = fullEndpoint;
});

function showLogin() {
  loginOverlay.classList.remove('hidden');
  dashboardApp.classList.add('hidden');
}

function showDashboard() {
  loginOverlay.classList.add('hidden');
  dashboardApp.classList.remove('hidden');
  loadDashboardData();
  fetchLiveApi();
}

// Login
loginForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  loginError.classList.add('hidden');
  const username = document.getElementById('login-username').value;
  const password = document.getElementById('login-password').value;

  try {
    const res = await fetch('/api/admin/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const data = await res.json();

    if (res.ok && data.token) {
      authToken = data.token;
      localStorage.setItem('samp_admin_token', authToken);
      showDashboard();
    } else {
      loginError.textContent = data.error || 'Authentication failed';
      loginError.classList.remove('hidden');
    }
  } catch (err) {
    loginError.textContent = 'Server connection error: ' + err.message;
    loginError.classList.remove('hidden');
  }
});

// Logout
btnLogout.addEventListener('click', () => {
  authToken = '';
  localStorage.removeItem('samp_admin_token');
  showLogin();
});

// File input selection feedback
uploadFileInput.addEventListener('change', () => {
  if (uploadFileInput.files.length > 0) {
    const f = uploadFileInput.files[0];
    fileChosenText.textContent = `${f.name} (${formatBytes(f.size)})`;
    fileChosenText.style.color = 'var(--accent-primary)';
  } else {
    fileChosenText.textContent = 'Supports .zip, .rar containing game files';
    fileChosenText.style.color = 'var(--text-dim)';
  }
});

// Upload form submission
uploadForm.addEventListener('submit', (e) => {
  e.preventDefault();
  uploadAlert.classList.add('hidden');

  const file = uploadFileInput.files[0];
  const version = document.getElementById('upload-version').value.trim();
  const notes = document.getElementById('upload-notes').value.trim();
  const setActive = document.getElementById('upload-set-active').checked;

  if (!file) {
    showAlert('upload-alert', 'Please select a data file (.zip or .rar)', 'error');
    return;
  }
  if (!version) {
    showAlert('upload-alert', 'Version tag is required', 'error');
    return;
  }

  const formData = new FormData();
  formData.append('data_file', file);
  formData.append('version', version);
  formData.append('release_notes', notes);
  formData.append('set_active', setActive);

  // Use XMLHttpRequest to track upload progress
  const xhr = new XMLHttpRequest();
  xhr.open('POST', '/api/admin/upload');
  xhr.setRequestHeader('Authorization', 'Bearer ' + authToken);

  uploadProgressContainer.classList.remove('hidden');
  uploadProgressFill.style.width = '0%';
  uploadProgressPercent.textContent = '0%';
  uploadProgressStatus.textContent = 'Uploading data package...';

  xhr.upload.onprogress = (event) => {
    if (event.lengthComputable) {
      const percent = Math.round((event.loaded / event.total) * 100);
      uploadProgressFill.style.width = percent + '%';
      uploadProgressPercent.textContent = percent + '%';
      if (percent === 100) {
        uploadProgressStatus.textContent = 'Computing SHA-256 integrity checksum on server...';
      }
    }
  };

  xhr.onload = () => {
    uploadProgressContainer.classList.add('hidden');
    if (xhr.status >= 200 && xhr.status < 300) {
      try {
        const res = JSON.parse(xhr.responseText);
        showAlert('upload-alert', `Package v${version} uploaded successfully!`, 'success');
        uploadForm.reset();
        fileChosenText.textContent = 'Supports .zip, .rar containing game files';
        fileChosenText.style.color = 'var(--text-dim)';
        loadDashboardData();
        fetchLiveApi();
      } catch (err) {
        showAlert('upload-alert', 'Uploaded successfully', 'success');
      }
    } else if (xhr.status === 401) {
      showAlert('upload-alert', 'Session expired. Please log in again.', 'error');
      showLogin();
    } else {
      let errMsg = 'Upload failed';
      try {
        const errJson = JSON.parse(xhr.responseText);
        errMsg = errJson.error || errMsg;
      } catch (e) {}
      showAlert('upload-alert', errMsg, 'error');
    }
  };

  xhr.onerror = () => {
    uploadProgressContainer.classList.add('hidden');
    showAlert('upload-alert', 'Network error during upload', 'error');
  };

  xhr.send(formData);
});

// Load Dashboard Data & Releases Table
async function loadDashboardData() {
  try {
    const res = await fetch('/api/data-file/all');
    const data = await res.json();
    const releases = data.releases || [];

    // Update metrics
    metricTotalReleases.textContent = releases.length;
    const totalBytes = releases.reduce((acc, r) => acc + (r.file_size_bytes || 0), 0);
    metricTotalStorage.textContent = formatBytes(totalBytes);

    const active = releases.find(r => r.is_active) || releases[0];
    if (active) {
      metricVersion.textContent = 'v' + active.version;
      metricFilename.textContent = active.file_name;
      metricSize.textContent = formatBytes(active.file_size_bytes);
      const shortHash = active.checksum ? (active.checksum.substring(0, 16) + '...') : 'None';
      metricChecksum.textContent = `SHA-256: ${shortHash}`;
      metricChecksum.onclick = () => {
        navigator.clipboard.writeText(active.checksum || '');
        alert('SHA-256 copied to clipboard!');
      };
    } else {
      metricVersion.textContent = 'None';
      metricFilename.textContent = 'No package uploaded';
      metricSize.textContent = '0 MB';
      metricChecksum.textContent = 'None';
    }

    // Render table
    renderReleasesTable(releases);
  } catch (err) {
    console.error('Failed to load releases:', err);
  }
}

function renderReleasesTable(releases) {
  if (!releases || releases.length === 0) {
    releasesTableBody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center py-4" style="color: var(--text-dim);">
          No data packages uploaded yet. Use the upload form above to add one.
        </td>
      </tr>
    `;
    return;
  }

  releasesTableBody.innerHTML = releases.map(r => {
    const isAct = r.is_active;
    const shortHash = r.checksum ? (r.checksum.substring(0, 10) + '...' + r.checksum.slice(-6)) : 'None';
    const dateStr = r.uploaded_at ? new Date(r.uploaded_at).toLocaleString() : '-';

    return `
      <tr>
        <td>
          <span class="badge ${isAct ? 'badge-active' : 'badge-inactive'}">
            ${isAct ? 'ACTIVE' : 'INACTIVE'}
          </span>
        </td>
        <td><strong style="color: var(--accent-primary);">v${escapeHtml(r.version)}</strong></td>
        <td class="mono" style="font-size: 12px;">${escapeHtml(r.file_name)}</td>
        <td>${formatBytes(r.file_size_bytes)}</td>
        <td class="mono" style="font-size: 11px; color: var(--accent-cyan); cursor: pointer;" title="Full SHA: ${r.checksum}" onclick="navigator.clipboard.writeText('${r.checksum}')">
          ${shortHash} 📋
        </td>
        <td style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${escapeHtml(r.release_notes || '')}">
          ${escapeHtml(r.release_notes || '-')}
        </td>
        <td style="font-size: 12px; color: var(--text-dim);">${dateStr}</td>
        <td class="text-right">
          <div class="table-actions">
            ${!isAct ? `<button class="btn btn-secondary btn-sm" onclick="activateRelease('${r.id}')">Make Active</button>` : ''}
            <a href="${r.file_url}" download class="btn btn-ghost btn-sm" title="Download directly">Download</a>
            <button class="btn btn-danger-ghost btn-sm" onclick="deleteRelease('${r.id}')">Delete</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

// Activate Release
window.activateRelease = async function(id) {
  if (!confirm('Activate this version as the latest required update for all mobile players?')) return;
  try {
    const res = await fetch(`/api/admin/releases/${id}/activate`, {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + authToken }
    });
    if (res.ok) {
      loadDashboardData();
      fetchLiveApi();
    } else {
      alert('Failed to activate release');
    }
  } catch (e) {
    alert('Error activating release: ' + e.message);
  }
};

// Delete Release
window.deleteRelease = async function(id) {
  if (!confirm('Are you sure you want to delete this game data package? This action cannot be undone.')) return;
  try {
    const res = await fetch(`/api/admin/releases/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + authToken }
    });
    if (res.ok) {
      loadDashboardData();
      fetchLiveApi();
    } else {
      alert('Failed to delete release');
    }
  } catch (e) {
    alert('Error deleting release: ' + e.message);
  }
};

// Fetch Live API Preview
async function fetchLiveApi() {
  try {
    apiResponseJson.textContent = 'Fetching latest endpoint data...';
    const res = await fetch('/api/data-file/latest');
    const data = await res.json();
    apiResponseJson.textContent = JSON.stringify(data, null, 2);
  } catch (err) {
    apiResponseJson.textContent = 'Error querying API: ' + err.message;
  }
}

// Refresh buttons
btnRefreshApi.addEventListener('click', fetchLiveApi);
btnRefreshReleases.addEventListener('click', loadDashboardData);

// Copy URL button
btnCopyUrl.addEventListener('click', () => {
  navigator.clipboard.writeText(apiUrlText.textContent);
  alert('API endpoint URL copied!');
});

function showAlert(elementId, message, type) {
  const el = document.getElementById(elementId);
  el.className = `alert alert-${type}`;
  el.textContent = message;
  el.classList.remove('hidden');
}

function escapeHtml(text) {
  if (!text) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
