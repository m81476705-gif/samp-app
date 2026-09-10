package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DataFileResponse
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampBorder
import com.example.ui.theme.SampSurfaceVariant
import java.util.Locale

@Composable
fun UpdateRequiredDialog(
  release: DataFileResponse,
  onAccept: () -> Unit,
  onDecline: () -> Unit
) {
  val sizeMb = if (release.fileSizeBytes > 0) {
    val mb = release.fileSizeBytes / (1024f * 1024f)
    if (mb < 0.1f) {
      String.format(Locale.US, "%.0f KB", release.fileSizeBytes / 1024f)
    } else {
      String.format(Locale.US, "%.1f MB", mb)
    }
  } else {
    "Unknown size"
  }

  AlertDialog(
    modifier = Modifier.testTag("update_dialog"),
    shape = RoundedCornerShape(14.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp,
    icon = {
      Icon(
        imageVector = Icons.Default.Download,
        contentDescription = "Download Update",
        tint = SampAmber,
        modifier = Modifier.padding(top = 8.dp)
      )
    },
    title = {
      Text(
        text = "Required Data File Update",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Required data file update available ($sizeMb). Download now?",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(SampSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp)
        ) {
          Text(
            text = "Version: v${release.version}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = SampAmber
          )
          if (!release.releaseNotes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = release.releaseNotes,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          if (release.checksum.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "SHA-256: ${release.checksum.take(16)}...",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.secondary
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onAccept,
        colors = ButtonDefaults.buttonColors(
          containerColor = SampAmber,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("update_dialog_yes_button")
      ) {
        Text("Yes, Download Now", fontWeight = FontWeight.SemiBold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDecline,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SampBorder)),
        modifier = Modifier.testTag("update_dialog_no_button")
      ) {
        Text("No (Later)")
      }
    },
    onDismissRequest = onDecline
  )
}
