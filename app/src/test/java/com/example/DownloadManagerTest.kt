package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.download.GameDataDownloadManager
import com.example.data.model.SampServer
import com.example.data.storage.DataPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DownloadManagerTest {
  private lateinit var context: Context
  private lateinit var preferences: DataPreferences
  private lateinit var downloadManager: GameDataDownloadManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
    preferences = DataPreferences(context)
    preferences.clearInstalledData()
    preferences.saveServers(emptyList())
    downloadManager = GameDataDownloadManager(context, preferences)
  }

  @Test
  fun testInitialState_NotInstalled() {
    assertNull(preferences.installedDataVersion)
    assertFalse(downloadManager.isGameDataInstalled())
  }

  @Test
  fun testInstallationRecording_SavesVersionAndChecksum() {
    val testVersion = "1.0.2"
    val testChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    val testSize = 123456789L

    preferences.recordSuccessfulInstallation(testVersion, testChecksum, testSize)

    assertEquals(testVersion, preferences.installedDataVersion)
    assertEquals(testChecksum, preferences.installedDataChecksum)
    assertEquals(testSize, preferences.installedDataSizeBytes)
    assertTrue(preferences.installedDataTimestamp > 0)
  }

  @Test
  fun testSettingsDefaults() {
    assertEquals("Carl_Johnson", preferences.playerName)
    assertTrue(preferences.backendBaseUrl.contains("3000"))
    assertEquals(60, preferences.fpsLimit)
    assertTrue(preferences.fastConnect)
  }

  @Test
  fun testClearInstalledData() {
    preferences.recordSuccessfulInstallation("1.0.0", "hash123", 1000L)
    assertEquals("1.0.0", preferences.installedDataVersion)

    preferences.clearInstalledData()
    assertNull(preferences.installedDataVersion)
    assertNull(preferences.installedDataChecksum)
  }

  @Test
  fun testCustomServers_SaveAndRetrieve() {
    assertTrue(preferences.getSavedServers().isEmpty())

    val customServer = SampServer(
      id = "test_1",
      name = "Sri Lanka RolePlay",
      ip = "192.168.1.100",
      port = 7777,
      players = 15,
      maxPlayers = 100,
      gamemode = "SL-RP",
      language = "Sinhala",
      ping = 25,
      isFavorite = true
    )

    preferences.saveServers(listOf(customServer))

    val retrieved = preferences.getSavedServers()
    assertEquals(1, retrieved.size)
    assertEquals("Sri Lanka RolePlay", retrieved[0].name)
    assertEquals("192.168.1.100", retrieved[0].ip)
    assertEquals(7777, retrieved[0].port)
    assertTrue(retrieved[0].isFavorite)
  }
}
