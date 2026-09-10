# SA-MP Mobile - Required Data File Distribution Server & Admin Panel

This server provides the backend distribution API and administrative web dashboard for the SA-MP Mobile Android Client.

## Quick Start

### 1. Install & Run
```bash
cd server
npm install
npm start
```

Default port: `3000` (can be overridden via `SERVER_PORT=...`).

### 2. Admin Web Panel
Open in browser:
- `http://localhost:3000`
- Default Credentials:
  - **Username:** `admin`
  - **Password:** `adminpassword123`

### 3. API Endpoints

#### Public Client Endpoints
- `GET /api/data-file/latest`
  Returns JSON metadata for the currently active data file package:
  ```json
  {
    "version": "1.0.0",
    "file_url": "http://10.0.2.2:3000/uploads/samp_mobile_data_v1.0.0.zip",
    "file_name": "samp_mobile_data_v1.0.0.zip",
    "file_size_bytes": 524611,
    "checksum": "45656ac6a67affdb8d8680a220c07831c37607b24f550c4d839b18ff89da0fb3",
    "release_notes": "Initial SA-MP required game core package",
    "uploaded_at": "2026-09-10T14:53:36.370Z"
  }
  ```
- `GET /uploads/:filename`
  Direct static download of data files (supports HTTP Range headers for chunked downloads).
- `GET /api/data-file/all`
  Returns all registered versions.

#### Admin Endpoints
- `POST /api/admin/login`
- `GET /api/admin/status`
- `POST /api/admin/upload` (multipart/form-data: `data_file`, `version`, `release_notes`, `set_active`)
- `POST /api/admin/releases/:id/activate`
- `DELETE /api/admin/releases/:id`

## Android Client Connection

- **Android Emulator:** Set Backend URL to `http://10.0.2.2:3000` (or leave default).
- **Physical Android Device:** Set Backend URL to your machine's local network IP (e.g. `http://192.168.1.50:3000`).
- **Offline / Standalone Fallback:** The Android app includes a built-in demo mode and local test server fallback if network is unreachable.
