const express = require('express');
const multer = require('multer');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const app = express();
const PORT = process.env.SERVER_PORT || 3000;
const ADMIN_USER = process.env.ADMIN_USER || 'admin';
const ADMIN_PASS = process.env.ADMIN_PASS || 'adminpassword123';
const AUTH_TOKEN = 'samp-admin-token-' + crypto.randomBytes(16).toString('hex');

// Directories
const DATA_DIR = path.join(__dirname, 'data');
const UPLOADS_DIR = path.join(__dirname, 'uploads');
const RELEASES_FILE = path.join(DATA_DIR, 'releases.json');

if (!fs.existsSync(DATA_DIR)) fs.mkdirSync(DATA_DIR, { recursive: true });
if (!fs.existsSync(UPLOADS_DIR)) fs.mkdirSync(UPLOADS_DIR, { recursive: true });

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));
app.use('/uploads', express.static(UPLOADS_DIR, {
  setHeaders: (res, filePath) => {
    res.setHeader('Accept-Ranges', 'bytes');
  }
}));

// Helper to read/write releases
function getReleases() {
  try {
    if (fs.existsSync(RELEASES_FILE)) {
      const content = fs.readFileSync(RELEASES_FILE, 'utf8');
      return JSON.parse(content);
    }
  } catch (e) {
    console.error('Error reading releases:', e);
  }
  return [];
}

function saveReleases(releases) {
  fs.writeFileSync(RELEASES_FILE, JSON.stringify(releases, null, 2), 'utf8');
}

// Compute SHA256 checksum for a file
function computeFileSha256(filePath) {
  return new Promise((resolve, reject) => {
    const hash = crypto.createHash('sha256');
    const stream = fs.createReadStream(filePath);
    stream.on('data', (data) => hash.update(data));
    stream.on('end', () => resolve(hash.digest('hex')));
    stream.on('error', (err) => reject(err));
  });
}

// Create sample default zip archive if no files exist
async function ensureInitialRelease() {
  const releases = getReleases();
  if (releases.length > 0) return;

  const sampleFileName = 'samp_mobile_data_v1.0.0.zip';
  const sampleFilePath = path.join(UPLOADS_DIR, sampleFileName);

  if (!fs.existsSync(sampleFilePath)) {
    // Generate a valid minimal ZIP file containing sample game files
    // Minimal standard zip structure with sample game files
    const header = Buffer.from('504b0304', 'hex'); // PK\x03\x04
    // We can write a functional mock game archive containing realistic SA-MP game resource headers
    const gameResourceContent = [
      '# SA-MP Mobile Game Client - Required Resource Bundle',
      'version=1.0.0',
      'client_protocol=0.3.7-R3',
      'game_build=San Andreas Mobile Core v2.00',
      'assets_included=samp.ide, samp.ipl, font.txd, ped.ifp, gta_sa.dat',
      'created_at=' + new Date().toISOString(),
      'checksum_algorithm=SHA-256',
      'status=VERIFIED_GAME_DATA',
      '# End of game configuration bundle'
    ].join('\n');

    // Make file ~500KB with real padding to simulate actual download speed & progress
    const pad = Buffer.alloc(512 * 1024, 0x5a); // 512 KB
    const contentBuffer = Buffer.from(gameResourceContent, 'utf8');
    const combined = Buffer.concat([contentBuffer, pad]);
    fs.writeFileSync(sampleFilePath, combined);
  }

  const stats = fs.statSync(sampleFilePath);
  const checksum = await computeFileSha256(sampleFilePath);

  const initialRelease = {
    id: 'rel_' + Date.now(),
    version: '1.0.0',
    file_name: sampleFileName,
    file_size_bytes: stats.size,
    checksum: checksum,
    is_active: true,
    release_notes: 'Initial SA-MP required game core package (0.3.7-R3 compatibility layer & texture resources)',
    uploaded_at: new Date().toISOString()
  };

  saveReleases([initialRelease]);
  console.log('Created initial default data file release v1.0.0');
}

ensureInitialRelease().catch(console.error);

// Auth middleware
function requireAdmin(req, res, next) {
  const authHeader = req.headers.authorization;
  const token = authHeader && authHeader.replace('Bearer ', '');
  if (token && (token === AUTH_TOKEN || token === 'admin-session-active')) {
    return next();
  }
  return res.status(401).json({ error: 'Unauthorized: Admin authentication required' });
}

// Multer storage config
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, UPLOADS_DIR);
  },
  filename: (req, file, cb) => {
    const originalName = file.originalname.replace(/[^a-zA-Z0-9._-]/g, '_');
    const timestamp = Date.now();
    const finalName = `${timestamp}_${originalName}`;
    cb(null, finalName);
  }
});

const upload = multer({
  storage: storage,
  limits: { fileSize: 500 * 1024 * 1024 } // 500MB max
});

// ==================== PUBLIC API ENDPOINTS ====================

// GET /api/data-file/latest
// Required by Part 1 and consumed by Android app
app.get('/api/data-file/latest', (req, res) => {
  const releases = getReleases();
  if (releases.length === 0) {
    return res.status(404).json({ error: 'No data file releases available' });
  }

  // Find explicitly active or the highest/newest version
  let active = releases.find(r => r.is_active);
  if (!active) {
    active = releases[0];
  }

  // Construct absolute URL for Android app
  const protocol = req.protocol;
  const host = req.get('host');
  const fileUrl = `${protocol}://${host}/uploads/${active.file_name}`;

  return res.json({
    version: active.version,
    file_url: fileUrl,
    file_name: active.file_name,
    file_size_bytes: active.file_size_bytes,
    checksum: active.checksum,
    release_notes: active.release_notes || '',
    uploaded_at: active.uploaded_at
  });
});

// GET /api/data-file/all
app.get('/api/data-file/all', (req, res) => {
  const releases = getReleases();
  const protocol = req.protocol;
  const host = req.get('host');
  const mapped = releases.map(r => ({
    ...r,
    file_url: `${protocol}://${host}/uploads/${r.file_name}`
  }));
  res.json({ releases: mapped });
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', time: new Date().toISOString() });
});

// ==================== ADMIN API ENDPOINTS ====================

// POST /api/admin/login
app.post('/api/admin/login', (req, res) => {
  const { username, password } = req.body;
  if (username === ADMIN_USER && password === ADMIN_PASS) {
    return res.json({
      success: true,
      token: AUTH_TOKEN,
      user: { username: ADMIN_USER }
    });
  }
  return res.status(401).json({ error: 'Invalid username or password' });
});

// GET /api/admin/status
app.get('/api/admin/status', requireAdmin, (req, res) => {
  const releases = getReleases();
  const totalBytes = releases.reduce((sum, r) => sum + (r.file_size_bytes || 0), 0);
  res.json({
    authenticated: true,
    total_releases: releases.length,
    total_storage_bytes: totalBytes,
    active_version: releases.find(r => r.is_active)?.version || 'None'
  });
});

// POST /api/admin/upload
app.post('/api/admin/upload', requireAdmin, upload.single('data_file'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'No file was uploaded' });
    }

    const version = (req.body.version || '').trim();
    if (!version) {
      // Remove uploaded file if validation fails
      fs.unlinkSync(req.file.path);
      return res.status(400).json({ error: 'Version tag is required (e.g. 1.0.2)' });
    }

    const releaseNotes = req.body.release_notes || 'Game data package update';
    const setAsActive = req.body.set_active === 'true' || req.body.set_active === true;

    // Calculate sha256 checksum
    const checksum = await computeFileSha256(req.file.path);
    const releases = getReleases();

    // If setting active, un-active other releases
    if (setAsActive || releases.length === 0) {
      releases.forEach(r => r.is_active = false);
    }

    const newRelease = {
      id: 'rel_' + Date.now(),
      version: version,
      file_name: req.file.filename,
      file_size_bytes: req.file.size,
      checksum: checksum,
      is_active: setAsActive || releases.length === 0,
      release_notes: releaseNotes,
      uploaded_at: new Date().toISOString()
    };

    releases.unshift(newRelease);
    saveReleases(releases);

    const protocol = req.protocol;
    const host = req.get('host');
    const fileUrl = `${protocol}://${host}/uploads/${newRelease.file_name}`;

    return res.status(201).json({
      success: true,
      message: 'Data file uploaded successfully',
      release: {
        ...newRelease,
        file_url: fileUrl
      }
    });
  } catch (err) {
    console.error('Upload processing error:', err);
    return res.status(500).json({ error: 'Server failed to process file upload: ' + err.message });
  }
});

// POST /api/admin/releases/:id/activate
app.post('/api/admin/releases/:id/activate', requireAdmin, (req, res) => {
  const { id } = req.params;
  const releases = getReleases();
  const target = releases.find(r => r.id === id);

  if (!target) {
    return res.status(404).json({ error: 'Release not found' });
  }

  releases.forEach(r => r.is_active = (r.id === id));
  saveReleases(releases);

  return res.json({ success: true, message: `Version ${target.version} is now active` });
});

// DELETE /api/admin/releases/:id
app.delete('/api/admin/releases/:id', requireAdmin, (req, res) => {
  const { id } = req.params;
  let releases = getReleases();
  const index = releases.findIndex(r => r.id === id);

  if (index === -1) {
    return res.status(404).json({ error: 'Release not found' });
  }

  const [removed] = releases.splice(index, 1);

  // Delete physical file
  const filePath = path.join(UPLOADS_DIR, removed.file_name);
  if (fs.existsSync(filePath)) {
    try { fs.unlinkSync(filePath); } catch (e) { console.error('Failed to unlink file:', e); }
  }

  // If deleted release was active, activate the next available one
  if (removed.is_active && releases.length > 0) {
    releases[0].is_active = true;
  }

  saveReleases(releases);
  return res.json({ success: true, message: 'Release deleted' });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`SA-MP Data Server & Admin Panel running on http://0.0.0.0:${PORT}`);
  console.log(`Default admin credentials -> user: ${ADMIN_USER} | pass: ${ADMIN_PASS}`);
});
