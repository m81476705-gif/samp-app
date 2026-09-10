package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SampAmber
import com.example.ui.theme.SampBorder
import com.example.ui.theme.SampRed
import com.example.ui.theme.SampSurface
import com.example.ui.theme.SampSurfaceVariant

@Composable
fun AddServerDialog(
  onAddServer: (name: String, ip: String, port: Int, gamemode: String, language: String, isFavorite: Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var ip by remember { mutableStateOf("") }
  var portStr by remember { mutableStateOf("7777") }
  var gamemode by remember { mutableStateOf("Freeroam") }
  var language by remember { mutableStateOf("English") }
  var isFavorite by remember { mutableStateOf(false) }

  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_server_dialog"),
    shape = RoundedCornerShape(14.dp),
    containerColor = SampSurface,
    icon = {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Add Server",
        tint = SampAmber,
        modifier = Modifier.size(28.dp)
      )
    },
    title = {
      Text(
        text = "Add SA-MP Server",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    },
    text = {
      val scrollState = rememberScrollState()
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState)
      ) {
        Text(
          text = "Enter your custom server details to add it to your launcher:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Server IP / Hostname
        OutlinedTextField(
          value = ip,
          onValueChange = {
            ip = it
            errorMessage = null
          },
          label = { Text("Server IP / Hostname *") },
          placeholder = { Text("e.g. 185.125.230.12 or play.samp.net") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_server_ip_input"),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = SampSurfaceVariant,
            unfocusedContainerColor = SampSurfaceVariant,
            focusedIndicatorColor = SampAmber,
            unfocusedIndicatorColor = SampBorder
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Port
          OutlinedTextField(
            value = portStr,
            onValueChange = {
              portStr = it
              errorMessage = null
            },
            label = { Text("Port *") },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("add_server_port_input"),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = SampSurfaceVariant,
              unfocusedContainerColor = SampSurfaceVariant,
              focusedIndicatorColor = SampAmber,
              unfocusedIndicatorColor = SampBorder
            )
          )

          // Gamemode
          OutlinedTextField(
            value = gamemode,
            onValueChange = { gamemode = it },
            label = { Text("Gamemode") },
            singleLine = true,
            modifier = Modifier.weight(1.4f),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = SampSurfaceVariant,
              unfocusedContainerColor = SampSurfaceVariant,
              focusedIndicatorColor = SampAmber,
              unfocusedIndicatorColor = SampBorder
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Server Name (Optional or custom label)
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Server Display Name (Optional)") },
          placeholder = { Text("e.g. My Custom SA-MP Server") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_server_name_input"),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = SampSurfaceVariant,
            unfocusedContainerColor = SampSurfaceVariant,
            focusedIndicatorColor = SampAmber,
            unfocusedIndicatorColor = SampBorder
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Language
        OutlinedTextField(
          value = language,
          onValueChange = { language = it },
          label = { Text("Language") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = SampSurfaceVariant,
            unfocusedContainerColor = SampSurfaceVariant,
            focusedIndicatorColor = SampAmber,
            unfocusedIndicatorColor = SampBorder
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Add to Favorites Checkbox
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = isFavorite,
            onCheckedChange = { isFavorite = it },
            colors = CheckboxDefaults.colors(
              checkedColor = SampAmber,
              uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
          Text(
            text = "Add to Favorites",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = errorMessage!!,
            style = MaterialTheme.typography.bodySmall,
            color = SampRed
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val cleanIp = ip.trim()
          val port = portStr.trim().toIntOrNull()
          if (cleanIp.isEmpty()) {
            errorMessage = "Server IP / Hostname is required"
            return@Button
          }
          if (port == null || port <= 0 || port > 65535) {
            errorMessage = "Please enter a valid port (1 - 65535)"
            return@Button
          }

          val resolvedName = name.trim().ifEmpty { cleanIp }
          val resolvedGamemode = gamemode.trim().ifEmpty { "Freeroam" }
          val resolvedLanguage = language.trim().ifEmpty { "English" }

          onAddServer(resolvedName, cleanIp, port, resolvedGamemode, resolvedLanguage, isFavorite)
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = SampAmber,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("confirm_add_server_button")
      ) {
        Text("Add Server", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Cancel")
      }
    }
  )
}
