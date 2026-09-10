package com.example.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.download.DownloadStatus
import com.example.ui.components.AddServerDialog
import com.example.ui.components.BlockedAccessBanner
import com.example.ui.components.ConnectDialog
import com.example.ui.components.DataManagerDialog
import com.example.ui.components.DownloadProgressDialog
import com.example.ui.components.HeroHeader
import com.example.ui.components.QuickConnectDialog
import com.example.ui.components.ServerCard
import com.example.ui.components.SettingsDialog
import com.example.ui.components.UpdateRequiredDialog
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampBackground
import com.example.ui.theme.SampBorder
import com.example.ui.theme.SampOnAmber
import com.example.ui.theme.SampRed
import com.example.ui.theme.SampSurface
import com.example.ui.theme.SampSurfaceVariant
import com.example.ui.viewmodel.LauncherViewModel

@Composable
fun LauncherScreen(
  viewModel: LauncherViewModel,
  modifier: Modifier = Modifier
) {
  val downloadStatus by viewModel.downloadStatus.collectAsState()
  val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
  val pendingRelease by viewModel.pendingRelease.collectAsState()
  val isAccessBlocked by viewModel.isAccessBlocked.collectAsState()

  val servers by viewModel.servers.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()

  val connectingServer by viewModel.connectingServer.collectAsState()
  val connectionStage by viewModel.connectionStage.collectAsState()
  val isConnected by viewModel.isConnected.collectAsState()

  val showSettings by viewModel.showSettingsDialog.collectAsState()
  val showDataManager by viewModel.showDataManagerDialog.collectAsState()
  val showQuickConnect by viewModel.showQuickConnectDialog.collectAsState()
  val showAddServer by viewModel.showAddServerDialog.collectAsState()
  val serverToDelete by viewModel.serverToDelete.collectAsState()
  val integrityResult by viewModel.integrityResult.collectAsState()

  val isDataReady = viewModel.downloadManager.isGameDataInstalled() && !isAccessBlocked
  val installedVersion = viewModel.preferences.installedDataVersion

  // Filter servers based on tab (0: All, 1: Favorites) & search query
  val filteredServers = servers.filter { srv ->
    val matchesTab = when (selectedTab) {
      1 -> srv.isFavorite
      else -> true
    }
    val matchesSearch = searchQuery.isBlank() ||
        srv.name.contains(searchQuery, ignoreCase = true) ||
        srv.gamemode.contains(searchQuery, ignoreCase = true) ||
        srv.ip.contains(searchQuery, ignoreCase = true)

    matchesTab && matchesSearch
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = SampBackground,
    floatingActionButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Quick Connect FAB
        FloatingActionButton(
          onClick = { viewModel.openQuickConnect(true) },
          containerColor = SampSurfaceVariant,
          contentColor = SampAmber,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("quick_connect_fab")
        ) {
          Icon(Icons.Default.FlashOn, contentDescription = "Quick Connect IP")
        }

        // Add Server FAB (Primary)
        ExtendedFloatingActionButton(
          onClick = { viewModel.openAddServer(true) },
          icon = { Icon(Icons.Default.Add, contentDescription = null) },
          text = { Text("Add Server", fontWeight = FontWeight.Bold) },
          containerColor = SampAmber,
          contentColor = SampOnAmber,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("add_server_fab")
        )
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Hero Header
      HeroHeader(
        playerName = viewModel.preferences.playerName,
        isDataReady = isDataReady,
        installedVersion = installedVersion,
        onOpenSettings = { viewModel.openSettings(true) },
        onOpenDataManager = { viewModel.openDataManager(true) },
        onRefreshCheck = { viewModel.checkRequiredDataOnLaunch() }
      )

      // Blocked Access Banner (if user declined update or data missing)
      AnimatedVisibility(
        visible = isAccessBlocked,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        BlockedAccessBanner(
          versionRequired = pendingRelease?.version,
          onDownloadClicked = { viewModel.onRetryUpdate() },
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
      }

      // Search & Tabs
      val configuration = LocalConfiguration.current
      val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

      if (isLandscape) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Tabs on the left
          TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SampAmber,
            modifier = Modifier.width(340.dp),
            indicator = { tabPositions ->
              TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = SampAmber,
                height = 3.dp
              )
            },
            divider = {}
          ) {
            Tab(
              selected = selectedTab == 0,
              onClick = { viewModel.setSelectedTab(0) },
              text = { Text("My Servers (${servers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
              selected = selectedTab == 1,
              onClick = { viewModel.setSelectedTab(1) },
              text = { Text("Favorites (${servers.count { it.isFavorite }})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          // Search bar taking the remaining width
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search servers, gamemodes, IPs...") },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("server_search_bar"),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = SampSurfaceVariant,
              unfocusedContainerColor = SampSurfaceVariant,
              focusedIndicatorColor = SampAmber,
              unfocusedIndicatorColor = SampBorder
            ),
            shape = RoundedCornerShape(8.dp)
          )
        }
      } else {
        // Portrait Search & Tabs
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
        ) {
          // Search bar
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search your servers, gamemodes, IPs...") },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 8.dp)
              .testTag("server_search_bar"),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = SampSurfaceVariant,
              unfocusedContainerColor = SampSurfaceVariant,
              focusedIndicatorColor = SampAmber,
              unfocusedIndicatorColor = SampBorder
            ),
            shape = RoundedCornerShape(8.dp)
          )

          // Tabs: All Servers, Favorites
          TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SampAmber,
            indicator = { tabPositions ->
              TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = SampAmber,
                height = 3.dp
              )
            },
            divider = {}
          ) {
            Tab(
              selected = selectedTab == 0,
              onClick = { viewModel.setSelectedTab(0) },
              text = { Text("My Servers (${servers.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
              selected = selectedTab == 1,
              onClick = { viewModel.setSelectedTab(1) },
              text = { Text("Favorites (${servers.count { it.isFavorite }})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
          }
        }
      }

      // Server List or Empty State
      if (filteredServers.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.Dns,
              contentDescription = null,
              tint = SampAmber.copy(alpha = 0.8f),
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = if (servers.isEmpty()) "No Servers Added Yet" else if (selectedTab == 1) "No Favorite Servers" else "No matching servers found",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (servers.isEmpty()) {
                "You have no servers in your list. Tap below to add your custom SA-MP server."
              } else if (selectedTab == 1) {
                "Star any of your added servers to view them in favorites."
              } else {
                "Try clearing your search query."
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (servers.isEmpty() || selectedTab == 0) {
              Spacer(modifier = Modifier.height(18.dp))
              Button(
                onClick = { viewModel.openAddServer(true) },
                colors = ButtonDefaults.buttonColors(containerColor = SampAmber),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("empty_state_add_server_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Server", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("server_list"),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredServers, key = { it.id }) { server ->
            ServerCard(
              server = server,
              isDataReady = isDataReady,
              onPlay = { viewModel.onPlayServer(server) },
              onToggleFavorite = { viewModel.toggleFavorite(server.id) },
              onDelete = { viewModel.confirmDeleteServer(server) }
            )
          }
        }
      }
    }

    // ==================== DIALOGS ====================

    // 1. Add Server Dialog (User-requested feature)
    if (showAddServer) {
      AddServerDialog(
        onAddServer = { name, ip, port, mode, lang, fav ->
          viewModel.addServer(name, ip, port, mode, lang, fav)
        },
        onDismiss = { viewModel.openAddServer(false) }
      )
    }

    // 2. Delete Server Confirmation Dialog
    if (serverToDelete != null) {
      val srv = serverToDelete!!
      AlertDialog(
        onDismissRequest = { viewModel.cancelDeleteServer() },
        containerColor = SampSurface,
        shape = RoundedCornerShape(14.dp),
        icon = {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SampRed)
        },
        title = {
          Text("Delete Server", fontWeight = FontWeight.Bold)
        },
        text = {
          Text(
            text = "Are you sure you want to remove '${srv.name}' (${srv.ip}:${srv.port}) from your server list?",
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        confirmButton = {
          Button(
            onClick = { viewModel.deleteServer(srv.id) },
            colors = ButtonDefaults.buttonColors(containerColor = SampRed),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Delete", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = { viewModel.cancelDeleteServer() },
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Cancel")
          }
        }
      )
    }

    // 3. Required Data File Update Dialog
    if (showUpdateDialog && pendingRelease != null) {
      UpdateRequiredDialog(
        release = pendingRelease!!,
        onAccept = { viewModel.onAcceptUpdate() },
        onDecline = { viewModel.onDeclineUpdate() }
      )
    }

    // 4. Active Download Progress Dialog
    if (downloadStatus !is DownloadStatus.Idle && downloadStatus !is DownloadStatus.Checking && downloadStatus !is DownloadStatus.UpdateAvailable && downloadStatus !is DownloadStatus.UpToDate) {
      DownloadProgressDialog(
        status = downloadStatus,
        onCancel = { viewModel.downloadManager.cancelDownload() },
        onDismiss = { viewModel.downloadManager.dismissStatus() },
        onRetry = { viewModel.onRetryUpdate() }
      )
    }

    // 5. Server Connection Dialog
    if (connectingServer != null) {
      ConnectDialog(
        server = connectingServer!!,
        stage = connectionStage,
        isConnected = isConnected,
        onDisconnect = { viewModel.closeConnectionDialog() }
      )
    }

    // 6. Settings Dialog
    if (showSettings) {
      SettingsDialog(
        preferences = viewModel.preferences,
        onDismiss = { viewModel.openSettings(false) },
        onSave = { name, url, fps, fastConn ->
          viewModel.updateSettings(name, url, fps, fastConn)
        }
      )
    }

    // 7. Data Manager Dialog
    if (showDataManager) {
      DataManagerDialog(
        preferences = viewModel.preferences,
        gameDataDir = viewModel.downloadManager.gameDataDir,
        isInstalled = viewModel.downloadManager.isGameDataInstalled(),
        integrityResult = integrityResult,
        onVerifyIntegrity = { viewModel.verifyInstalledIntegrity() },
        onCheckUpdates = {
          viewModel.openDataManager(false)
          viewModel.checkRequiredDataOnLaunch()
        },
        onResetData = { viewModel.resetDataForTesting() },
        onDismiss = { viewModel.openDataManager(false) }
      )
    }

    // 8. Quick Connect Dialog
    if (showQuickConnect) {
      QuickConnectDialog(
        onConnect = { ip, port -> viewModel.connectQuick(ip, port) },
        onDismiss = { viewModel.openQuickConnect(false) }
      )
    }
  }
}
