package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.download.DownloadStatus
import com.example.data.download.GameDataDownloadManager
import com.example.data.model.DataFileResponse
import com.example.data.model.SampServer
import com.example.data.storage.DataPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
  val preferences = DataPreferences(application)
  val downloadManager = GameDataDownloadManager(application, preferences)

  val downloadStatus: StateFlow<DownloadStatus> = downloadManager.status
    .stateIn(viewModelScope, SharingStarted.Eagerly, DownloadStatus.Idle)

  // Dialog & Access Blocking state
  private val _showUpdateDialog = MutableStateFlow(false)
  val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

  private val _pendingRelease = MutableStateFlow<DataFileResponse?>(null)
  val pendingRelease: StateFlow<DataFileResponse?> = _pendingRelease.asStateFlow()

  // User requirement: "If No: block access to 'Play' / server list (or allow retry later)"
  private val _isAccessBlocked = MutableStateFlow(false)
  val isAccessBlocked: StateFlow<Boolean> = _isAccessBlocked.asStateFlow()

  // Connection Simulation state
  private val _connectingServer = MutableStateFlow<SampServer?>(null)
  val connectingServer: StateFlow<SampServer?> = _connectingServer.asStateFlow()

  private val _connectionStage = MutableStateFlow("Connecting to server...")
  val connectionStage: StateFlow<String> = _connectionStage.asStateFlow()

  private val _isConnected = MutableStateFlow(false)
  val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

  // Tab & Filter states (0 = All Servers, 1 = Favorites)
  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // Dialog toggles
  private val _showSettingsDialog = MutableStateFlow(false)
  val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

  private val _showDataManagerDialog = MutableStateFlow(false)
  val showDataManagerDialog: StateFlow<Boolean> = _showDataManagerDialog.asStateFlow()

  private val _showQuickConnectDialog = MutableStateFlow(false)
  val showQuickConnectDialog: StateFlow<Boolean> = _showQuickConnectDialog.asStateFlow()

  private val _showAddServerDialog = MutableStateFlow(false)
  val showAddServerDialog: StateFlow<Boolean> = _showAddServerDialog.asStateFlow()

  private val _serverToDelete = MutableStateFlow<SampServer?>(null)
  val serverToDelete: StateFlow<SampServer?> = _serverToDelete.asStateFlow()

  private val _integrityResult = MutableStateFlow<Pair<Boolean, String>?>(null)
  val integrityResult: StateFlow<Pair<Boolean, String>?> = _integrityResult.asStateFlow()

  // Servers are user-added only (no hardcoded/preloaded mock servers)
  private val _servers = MutableStateFlow<List<SampServer>>(preferences.getSavedServers())
  val servers: StateFlow<List<SampServer>> = _servers.asStateFlow()

  init {
    // Check required data files on launch
    checkRequiredDataOnLaunch()
  }

  fun checkRequiredDataOnLaunch() {
    viewModelScope.launch {
      val isInstalled = downloadManager.isGameDataInstalled()
      if (!isInstalled) {
        _isAccessBlocked.value = true
      }

      val result = downloadManager.checkForUpdates()
      when (result) {
        is DownloadStatus.UpdateAvailable -> {
          _pendingRelease.value = result.release
          _showUpdateDialog.value = true
          _isAccessBlocked.value = true
        }
        is DownloadStatus.UpToDate -> {
          _isAccessBlocked.value = false
        }
        is DownloadStatus.Error -> {
          if (isInstalled) {
            _isAccessBlocked.value = false
          } else {
            val fallbackRelease = DataFileResponse(
              version = "1.0.0",
              fileUrl = "${preferences.backendBaseUrl.trimEnd('/')}/uploads/samp_mobile_data_v1.0.0.zip",
              fileName = "samp_mobile_data_v1.0.0.zip",
              fileSizeBytes = 524611L,
              checksum = "45656ac6a67affdb8d8680a220c07831c37607b24f550c4d839b18ff89da0fb3",
              releaseNotes = "Initial SA-MP required game core package"
            )
            _pendingRelease.value = fallbackRelease
            _showUpdateDialog.value = true
            _isAccessBlocked.value = true
          }
        }
        else -> Unit
      }
    }
  }

  // User clicked "Yes / Download Now"
  fun onAcceptUpdate() {
    _showUpdateDialog.value = false
    val release = _pendingRelease.value ?: return
    downloadManager.startDownload(release, viewModelScope)
  }

  // User clicked "No / Later"
  fun onDeclineUpdate() {
    _showUpdateDialog.value = false
    _isAccessBlocked.value = true
  }

  fun onRetryUpdate() {
    if (_pendingRelease.value != null) {
      _showUpdateDialog.value = true
    } else {
      checkRequiredDataOnLaunch()
    }
  }

  fun onPlayServer(server: SampServer) {
    if (!downloadManager.isGameDataInstalled() || _isAccessBlocked.value) {
      if (_pendingRelease.value != null) {
        _showUpdateDialog.value = true
      } else {
        checkRequiredDataOnLaunch()
      }
      return
    }

    startServerConnection(server)
  }

  private fun startServerConnection(server: SampServer) {
    _connectingServer.value = server
    _isConnected.value = false
    _connectionStage.value = "Connecting to ${server.ip}:${server.port}..."

    viewModelScope.launch {
      delay(800)
      _connectionStage.value = "Connected. Validating data files (v${preferences.installedDataVersion})..."
      delay(900)
      _connectionStage.value = "Loading map assets & ped textures..."
      delay(800)
      _connectionStage.value = "Authorizing player '${preferences.playerName}'..."
      delay(800)
      _connectionStage.value = "Connected to ${server.name}!"
      _isConnected.value = true
    }
  }

  fun closeConnectionDialog() {
    _connectingServer.value = null
    _isConnected.value = false
  }

  // Add Server methods
  fun openAddServer(show: Boolean) {
    _showAddServerDialog.value = show
  }

  fun addServer(
    name: String,
    ip: String,
    port: Int,
    gamemode: String,
    language: String,
    isFavorite: Boolean
  ) {
    val newServer = SampServer(
      id = "srv_${System.currentTimeMillis()}",
      name = name,
      ip = ip,
      port = port,
      players = 0,
      maxPlayers = 100,
      gamemode = gamemode,
      language = language,
      ping = (20..80).random(),
      isFavorite = isFavorite,
      isHosted = false
    )
    val updated = listOf(newServer) + _servers.value
    _servers.value = updated
    preferences.saveServers(updated)
    _showAddServerDialog.value = false
  }

  // Delete Server methods
  fun confirmDeleteServer(server: SampServer) {
    _serverToDelete.value = server
  }

  fun cancelDeleteServer() {
    _serverToDelete.value = null
  }

  fun deleteServer(serverId: String) {
    val updated = _servers.value.filter { it.id != serverId }
    _servers.value = updated
    preferences.saveServers(updated)
    _serverToDelete.value = null
  }

  fun toggleFavorite(serverId: String) {
    val updated = _servers.value.map { srv ->
      if (srv.id == serverId) srv.copy(isFavorite = !srv.isFavorite) else srv
    }
    _servers.value = updated
    preferences.saveServers(updated)
  }

  fun setSelectedTab(tabIndex: Int) {
    _selectedTab.value = tabIndex
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun openSettings(show: Boolean) {
    _showSettingsDialog.value = show
  }

  fun openDataManager(show: Boolean) {
    _showDataManagerDialog.value = show
    if (show) {
      _integrityResult.value = null
    }
  }

  fun openQuickConnect(show: Boolean) {
    _showQuickConnectDialog.value = show
  }

  fun connectQuick(ip: String, portStr: String) {
    val port = portStr.toIntOrNull() ?: 7777
    val customServer = SampServer(
      id = "srv_custom_${System.currentTimeMillis()}",
      name = "Direct Connect [$ip:$port]",
      ip = ip,
      port = port,
      players = 1,
      maxPlayers = 100,
      gamemode = "Custom",
      language = "Any",
      ping = 28
    )
    _showQuickConnectDialog.value = false
    onPlayServer(customServer)
  }

  fun updateSettings(name: String, backendUrl: String, fps: Int, fastConn: Boolean) {
    preferences.playerName = name.trim().ifEmpty { DataPreferences.DEFAULT_PLAYER_NAME }
    preferences.backendBaseUrl = backendUrl.trim().ifEmpty { DataPreferences.DEFAULT_BACKEND_URL }
    preferences.fpsLimit = fps
    preferences.fastConnect = fastConn
    _showSettingsDialog.value = false
    checkRequiredDataOnLaunch()
  }

  fun verifyInstalledIntegrity() {
    val result = downloadManager.verifyInstalledFileIntegrity()
    _integrityResult.value = result
  }

  fun resetDataForTesting() {
    preferences.clearInstalledData()
    if (downloadManager.installedArchiveFile.exists()) {
      downloadManager.installedArchiveFile.delete()
    }
    _isAccessBlocked.value = true
    _showDataManagerDialog.value = false
    checkRequiredDataOnLaunch()
  }
}
