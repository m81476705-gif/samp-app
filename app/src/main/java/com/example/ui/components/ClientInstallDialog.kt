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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampGreen
import com.example.ui.theme.SampRed
import com.example.ui.theme.SampSurfaceVariant

@Composable
fun ClientInstallDialog(
  isClientInstalled: Boolean,
  clientPackage: String?,
  storagePath: String,
  onInstallClientApk: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("client_install_dialog"),
    shape = RoundedCornerShape(14.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    icon = {
      Icon(
        imageVector = Icons.Default.SportsEsports,
        contentDescription = "Game Client",
        tint = if (isClientInstalled) SampGreen else SampAmber
      )
    },
    title = {
      Text(
        text = if (isClientInstalled) "SA-MP Game Client Ready" else "Game Client (APK) Required",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      val scroll = rememberScrollState()
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scroll)
      ) {
        Text(
          text = "GTA SA-MP Game එක සෙල්ලම් කිරීමට අවශ්‍ය Game Client APK සහ Data Files තත්ත්වය:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Card
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
            Icon(
              imageVector = if (isClientInstalled) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = if (isClientInstalled) SampGreen else SampAmber,
              modifier = Modifier.size(20.dp)
            )
            Column {
              Text(
                text = if (isClientInstalled) "Game Client Installed" else "Client Not Installed Yet",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isClientInstalled) SampGreen else SampAmber
              )
              Text(
                text = if (isClientInstalled) "Package: $clientPackage" else "Needs GTA SA / SA-MP APK",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Data Files Path: $storagePath",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
            color = SampAmber
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "• Server එකක් තෝරා 'Connect' එබූ විට, අපේ Launcher එකෙන් කෙලින්ම Game Client එක open වී server එකට සම්බන්ධ වේ.\n" +
                 "• ඔබගේ දුරකථනයේ තවමත් GTA SA / SA-MP APK එකක් නොමැති නම්, 'Download & Install Client APK' ඔබා ස්ථාපනය කරගත හැක.",
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onInstallClientApk,
        colors = ButtonDefaults.buttonColors(containerColor = SampAmber),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
        Text(
          text = if (isClientInstalled) " Re-Install / Update Client" else " Install Client APK",
          color = MaterialTheme.colorScheme.onPrimary,
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Close")
      }
    }
  )
}
