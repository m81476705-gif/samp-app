package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Games
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SampServer
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampGreen
import com.example.ui.theme.SampSurfaceVariant

@Composable
fun ConnectDialog(
  server: SampServer,
  stage: String,
  isConnected: Boolean,
  onLaunchGameClient: () -> Unit,
  onDisconnect: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDisconnect,
    modifier = Modifier.testTag("connect_dialog"),
    shape = RoundedCornerShape(14.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    icon = {
      Icon(
        imageVector = Icons.Default.Games,
        contentDescription = "Game Session",
        tint = if (isConnected) SampGreen else SampAmber
      )
    },
    title = {
      Text(
        text = if (isConnected) "Connected to Server" else "Connecting to SA-MP",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = server.name,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${server.ip}:${server.port} • ${server.gamemode}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Terminal style connection log box
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SampSurfaceVariant)
            .padding(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (!isConnected) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = SampAmber
              )
            } else {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SampGreen,
                modifier = Modifier.size(16.dp)
              )
            }
            Text(
              text = stage,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              ),
              color = if (isConnected) SampGreen else MaterialTheme.colorScheme.onSurface
            )
          }

          if (isConnected) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Welcome to San Andreas Multiplayer! Data files verified. Assets loaded.",
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              ),
              color = MaterialTheme.colorScheme.secondary
            )
          }
        }
      }
    },
    confirmButton = {
      if (isConnected) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = onLaunchGameClient,
            colors = ButtonDefaults.buttonColors(containerColor = SampAmber),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Launch Game", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = onDisconnect,
            colors = ButtonDefaults.buttonColors(containerColor = SampGreen),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Done", color = MaterialTheme.colorScheme.surface)
          }
        }
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDisconnect,
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(if (isConnected) "Disconnect" else "Cancel")
      }
    }
  )
}
