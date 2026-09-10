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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.storage.DataPreferences
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampGreen
import com.example.ui.theme.SampRed
import com.example.ui.theme.SampSurface
import com.example.ui.theme.SampSurfaceVariant
import java.io.File
import java.util.Date
import java.util.Locale

@Composable
fun DataManagerDialog(
  preferences: DataPreferences,
  gameDataDir: File,
  isInstalled: Boolean,
  integrityResult: Pair<Boolean, String>?,
  onVerifyIntegrity: () -> Unit,
  onCheckUpdates: () -> Unit,
  onResetData: () -> Unit,
  onDismiss: () -> Unit
) {
  val version = preferences.installedDataVersion ?: "Not Installed"
  val checksum = preferences.installedDataChecksum ?: "None"
  val sizeMb = preferences.installedDataSizeBytes / (1024f * 1024f)
  val installDate = if (preferences.installedDataTimestamp > 0) {
    Date(preferences.installedDataTimestamp).toLocaleString()
  } else "Never"

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("data_manager_dialog"),
    shape = RoundedCornerShape(14.dp),
    containerColor = SampSurface,
    icon = {
      Icon(
        imageVector = Icons.Default.Folder,
        contentDescription = "Data Manager",
        tint = SampAmber
      )
    },
    title = {
      Text(
        text = "Game Data Package Manager",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Status overview card
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(SampSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Installed Version:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = if (isInstalled) "v$version" else "Not Installed",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              color = if (isInstalled) SampGreen else SampRed
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("File Size:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = if (isInstalled) String.format(Locale.US, "%.1f MB", sizeMb) else "0 MB",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Installed On:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = installDate,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Location: ${gameDataDir.name}/",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.secondary
          )

          if (checksum != "None") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "SHA-256: ${checksum.take(16)}...",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Integrity verification action
        if (isInstalled) {
          OutlinedButton(
            onClick = onVerifyIntegrity,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" Verify File SHA-256 Integrity", fontSize = 12.sp)
          }

          if (integrityResult != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = integrityResult.second,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              ),
              color = if (integrityResult.first) SampGreen else SampRed,
              modifier = Modifier.padding(horizontal = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
        }

        // Action buttons: Check Update & Clear Data
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onCheckUpdates,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(" Check Update", fontSize = 11.sp)
          }

          if (isInstalled) {
            OutlinedButton(
              onClick = onResetData,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = SampRed)
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = SampRed)
              Text(" Reset / Delete", fontSize = 11.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = SampAmber),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Close", color = MaterialTheme.colorScheme.onPrimary)
      }
    }
  )
}
