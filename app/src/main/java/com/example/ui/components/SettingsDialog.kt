package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiClient
import com.example.data.storage.DataPreferences
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampBorder
import com.example.ui.theme.SampGreen
import com.example.ui.theme.SampRed
import com.example.ui.theme.SampSurface
import com.example.ui.theme.SampSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsDialog(
  preferences: DataPreferences,
  onDismiss: () -> Unit,
  onSave: (name: String, backendUrl: String, fps: Int, fastConnect: Boolean) -> Unit
) {
  var name by remember { mutableStateOf(preferences.playerName) }
  var backendUrl by remember { mutableStateOf(preferences.backendBaseUrl) }
  var fps by remember { mutableStateOf(preferences.fpsLimit) }
  var fastConnect by remember { mutableStateOf(preferences.fastConnect) }

  var testStatus by remember { mutableStateOf<String?>(null) }
  var isTesting by remember { mutableStateOf(false) }
  var testSuccess by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("settings_dialog"),
    shape = RoundedCornerShape(14.dp),
    containerColor = SampSurface,
    icon = {
      Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "Settings",
        tint = SampAmber
      )
    },
    title = {
      Text(
        text = "Client & Backend Settings",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Nickname
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Player Nickname") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("nickname_input"),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = SampSurfaceVariant,
            unfocusedContainerColor = SampSurfaceVariant,
            focusedIndicatorColor = SampAmber,
            unfocusedIndicatorColor = SampBorder
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Backend URL
        OutlinedTextField(
          value = backendUrl,
          onValueChange = {
            backendUrl = it
            testStatus = null
          },
          label = { Text("Data Server API URL") },
          placeholder = { Text("http://10.0.2.2:3000") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("backend_url_input"),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = SampSurfaceVariant,
            unfocusedContainerColor = SampSurfaceVariant,
            focusedIndicatorColor = SampAmber,
            unfocusedIndicatorColor = SampBorder
          )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Preset buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = backendUrl.contains("10.0.2.2"),
            onClick = {
              backendUrl = "http://10.0.2.2:3000/"
              testStatus = null
            },
            label = { Text("Emulator (10.0.2.2)", fontSize = 10.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SampAmber.copy(alpha = 0.2f),
              selectedLabelColor = SampAmber
            )
          )

          FilterChip(
            selected = backendUrl.contains("127.0.0.1"),
            onClick = {
              backendUrl = "http://127.0.0.1:3000/"
              testStatus = null
            },
            label = { Text("Localhost", fontSize = 10.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SampAmber.copy(alpha = 0.2f),
              selectedLabelColor = SampAmber
            )
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Test Connection Button & status
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          OutlinedButton(
            onClick = {
              isTesting = true
              testStatus = "Testing connection..."
              coroutineScope.launch {
                try {
                  val result = withContext(Dispatchers.IO) {
                    val service = ApiClient.createService(backendUrl)
                    service.getLatestDataFile()
                  }
                  if (result.isSuccessful && result.body() != null) {
                    testSuccess = true
                    testStatus = "Connected! Found v${result.body()!!.version}"
                  } else {
                    testSuccess = false
                    testStatus = "HTTP ${result.code()}"
                  }
                } catch (e: Exception) {
                  testSuccess = false
                  testStatus = "Failed: ${e.message?.take(25)}..."
                } finally {
                  isTesting = false
                }
              }
            },
            enabled = !isTesting,
            shape = RoundedCornerShape(6.dp)
          ) {
            if (isTesting) {
              CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            } else {
              Text("Test Server", fontSize = 12.sp)
            }
          }

          if (testStatus != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (testSuccess) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (testSuccess) SampGreen else SampRed,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = testStatus ?: "",
                fontSize = 11.sp,
                color = if (testSuccess) SampGreen else SampRed,
                modifier = Modifier.padding(start = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fast Connect
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(SampSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Fast Connect", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text("Skip intro logos & spawn delay", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Switch(
            checked = fastConnect,
            onCheckedChange = { fastConnect = it },
            colors = SwitchDefaults.colors(checkedThumbColor = SampAmber, checkedTrackColor = SampAmber.copy(alpha = 0.4f))
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(name, backendUrl, fps, fastConnect) },
        colors = ButtonDefaults.buttonColors(containerColor = SampAmber),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("save_settings_button")
      ) {
        Text("Save Settings", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
        Text("Cancel")
      }
    }
  )
}
