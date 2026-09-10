package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.download.DownloadStatus
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampGreen
import com.example.ui.theme.SampRed
import com.example.ui.theme.SampSurfaceVariant
import java.util.Locale

@Composable
fun DownloadProgressDialog(
  status: DownloadStatus,
  onCancel: () -> Unit,
  onDismiss: () -> Unit,
  onRetry: () -> Unit
) {
  when (status) {
    is DownloadStatus.Downloading -> {
      val animatedProgress by animateFloatAsState(targetValue = status.progress, label = "dl_prog")
      val percentInt = (status.progress * 100).toInt()
      val downloadedMb = status.downloadedBytes / (1024f * 1024f)
      val totalMb = status.totalBytes / (1024f * 1024f)
      val speedMb = status.speedBps / (1024f * 1024f)

      AlertDialog(
        onDismissRequest = { /* Non-cancellable by clicking outside during download */ },
        modifier = Modifier.testTag("download_progress_dialog"),
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
          Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = "Downloading",
            tint = SampAmber
          )
        },
        title = {
          Text(
            text = "Downloading Game Data",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        },
        text = {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Progress",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$percentInt%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SampAmber
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
              progress = { animatedProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = SampAmber,
              trackColor = SampSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = String.format(Locale.US, "%.1f MB / %.1f MB", downloadedMb, totalMb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format(Locale.US, "%.1f MB/s", speedMb),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.secondary
              )
            }

            if (status.etaSeconds > 0) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "ETA: ~${status.etaSeconds}s remaining",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        confirmButton = {},
        dismissButton = {
          OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("download_cancel_button")
          ) {
            Text("Cancel Download")
          }
        }
      )
    }

    is DownloadStatus.VerifyingChecksum -> {
      AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Verifying File Integrity") },
        text = {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            LinearProgressIndicator(
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Computing SHA-256 integrity checksum...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        confirmButton = {}
      )
    }

    is DownloadStatus.Unpacking -> {
      AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Installing Game Files") },
        text = {
          Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
              progress = { status.progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = SampGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Extracting: ${status.currentFile}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        confirmButton = {}
      )
    }

    is DownloadStatus.Success -> {
      val sizeMb = status.sizeBytes / (1024f * 1024f)
      AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("download_success_dialog"),
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = SampGreen
          )
        },
        title = {
          Text(
            text = "Game Data Ready!",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        text = {
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "Required data files (v${status.version}) have been downloaded and verified successfully.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(SampSurfaceVariant, RoundedCornerShape(6.dp))
                .padding(10.dp)
            ) {
              Text(
                text = "Size: ${String.format(Locale.US, "%.1f MB", sizeMb)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Integrity: SHA-256 Passed",
                style = MaterialTheme.typography.bodySmall,
                color = SampGreen
              )
            }
          }
        },
        confirmButton = {
          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = SampGreen),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Ready to Play", color = MaterialTheme.colorScheme.surface)
          }
        }
      )
    }

    is DownloadStatus.Error -> {
      AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
          Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = SampRed
          )
        },
        title = {
          Text(
            text = "Download Failed",
            fontWeight = FontWeight.Bold,
            color = SampRed
          )
        },
        text = {
          Text(
            text = status.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        confirmButton = {
          if (status.canRetry) {
            Button(
              onClick = onRetry,
              colors = ButtonDefaults.buttonColors(containerColor = SampAmber),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Retry", color = MaterialTheme.colorScheme.onPrimary)
            }
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Dismiss")
          }
        }
      )
    }

    else -> Unit
  }
}
