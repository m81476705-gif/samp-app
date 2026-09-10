package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SampServer
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampBorder
import com.example.ui.theme.SampGreen
import com.example.ui.theme.SampOnAmber
import com.example.ui.theme.SampRed
import com.example.ui.theme.SampSurface
import com.example.ui.theme.SampSurfaceVariant

@Composable
fun ServerCard(
  server: SampServer,
  isDataReady: Boolean,
  onPlay: () -> Unit,
  onToggleFavorite: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val playerFraction = if (server.maxPlayers > 0) server.players.toFloat() / server.maxPlayers else 0f
  val pingColor = when {
    server.ping < 50 -> SampGreen
    server.ping < 100 -> SampAmber
    else -> Color(0xFFEF4444)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onPlay() }
      .testTag("server_card_${server.id}"),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = SampSurface),
    border = BorderStroke(1.dp, SampBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Top row: Server Title, Ping, Favorite & Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = server.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${server.ip}:${server.port}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          // Ping Pill
          Row(
            modifier = Modifier
              .background(SampSurfaceVariant, RoundedCornerShape(12.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(pingColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${server.ping}ms",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          // Favorite Button
          IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = if (server.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
              contentDescription = "Favorite",
              tint = if (server.isFavorite) SampAmber else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }

          // Delete Button (allows user to remove servers)
          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(32.dp)
              .testTag("delete_server_${server.id}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete Server",
              tint = SampRed.copy(alpha = 0.8f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Middle tags: Mode, Language, Players
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          TagBadge(text = server.gamemode)
          TagBadge(text = server.language)
        }

        Text(
          text = "${server.players} / ${server.maxPlayers}",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = SampAmber
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Player count bar
      LinearProgressIndicator(
        progress = { playerFraction },
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = SampAmber,
        trackColor = SampSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Connect Button
      Button(
        onClick = onPlay,
        modifier = Modifier
          .fillMaxWidth()
          .height(38.dp)
          .testTag("play_button_${server.id}"),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isDataReady) SampAmber else SampSurfaceVariant,
          contentColor = if (isDataReady) SampOnAmber else MaterialTheme.colorScheme.onSurfaceVariant
        )
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isDataReady) "CONNECT / PLAY" else "UPDATE REQUIRED TO PLAY",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          letterSpacing = 0.5.sp
        )
      }
    }
  }
}

@Composable
private fun TagBadge(text: String) {
  Box(
    modifier = Modifier
      .background(SampSurfaceVariant, RoundedCornerShape(4.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
