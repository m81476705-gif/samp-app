package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampGreen
import com.example.ui.theme.SampRed

@Composable
fun HeroHeader(
  playerName: String,
  isDataReady: Boolean,
  installedVersion: String?,
  onOpenSettings: () -> Unit,
  onOpenDataManager: () -> Unit,
  onRefreshCheck: () -> Unit,
  modifier: Modifier = Modifier
) {
  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  val headerHeight = if (isLandscape) 74.dp else 180.dp

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(headerHeight)
  ) {
    // Hero background image
    Image(
      painter = painterResource(id = R.drawable.samp_hero_banner),
      contentDescription = "SA-MP Mobile Header",
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop
    )

    // Dark gradient overlay for contrast & readability
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color.Black.copy(alpha = 0.50f),
              Color(0xFF0A0D14).copy(alpha = 0.95f)
            )
          )
        )
    )

    if (isLandscape) {
      // Landscape single-row sleek console header
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left: Branding
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(SampAmber),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "SA",
              color = Color.Black,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 15.sp
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "SA-MP MOBILE",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
              color = Color.White,
              letterSpacing = 1.sp
            )
            Text(
              text = "Client 0.3.7-R3 • Android",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.7f)
            )
          }
        }

        // Center: Player Name + Data Ready Chips
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Player chip
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier.clickable { onOpenSettings() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(22.dp)
                  .clip(CircleShape)
                  .background(SampAmber),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = Color.Black,
                  modifier = Modifier.size(15.dp)
                )
              }
              Spacer(modifier = Modifier.width(7.dp))
              Text(
                text = playerName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
            }
          }

          // Data Status Badge
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDataReady) SampGreen.copy(alpha = 0.25f) else SampRed.copy(alpha = 0.25f),
            modifier = Modifier.clickable { onOpenDataManager() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isDataReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isDataReady) SampGreen else SampAmber,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isDataReady) "Data Ready (v${installedVersion ?: "1.0"})" else "Update Required",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isDataReady) SampGreen else SampAmber
              )
            }
          }
        }

        // Right: Control Icons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(
            onClick = onOpenDataManager,
            modifier = Modifier.size(38.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = "Data Files Manager",
              tint = Color.White
            )
          }

          IconButton(
            onClick = onRefreshCheck,
            modifier = Modifier.size(38.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Check for Updates",
              tint = Color.White
            )
          }

          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(38.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = Color.White
            )
          }
        }
      }
    } else {
      // Portrait Layout
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Top row: App brand & controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SampAmber),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "SA",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "SA-MP MOBILE",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                letterSpacing = 1.sp
              )
              Text(
                text = "Client 0.3.7-R3 • San Andreas MP",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
              onClick = onOpenDataManager,
              modifier = Modifier.size(38.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Data Files Manager",
                tint = Color.White
              )
            }

            IconButton(
              onClick = onRefreshCheck,
              modifier = Modifier.size(38.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Check for Updates",
                tint = Color.White
              )
            }

            IconButton(
              onClick = onOpenSettings,
              modifier = Modifier.size(38.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
              )
            }
          }
        }

        // Bottom row: Player profile chip & Data File status chip
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Player chip
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.clickable { onOpenSettings() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(SampAmber),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = Color.Black,
                  modifier = Modifier.size(16.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = playerName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
            }
          }

          // Data file status badge
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDataReady) SampGreen.copy(alpha = 0.2f) else SampRed.copy(alpha = 0.2f),
            modifier = Modifier.clickable { onOpenDataManager() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isDataReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isDataReady) SampGreen else SampAmber,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isDataReady) "Data Ready (v${installedVersion ?: "1.0"})" else "Update Required",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isDataReady) SampGreen else SampAmber
              )
            }
          }
        }
      }
    }
  }
}
