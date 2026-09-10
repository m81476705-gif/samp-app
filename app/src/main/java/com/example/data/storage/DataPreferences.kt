package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.SampServer
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class DataPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val serverListType = Types.newParameterizedType(List::class.java, SampServer::class.java)
  private val serverListAdapter = moshi.adapter<List<SampServer>>(serverListType)

  companion object {
    private const val PREFS_NAME = "samp_mobile_prefs"

    // Required Data File keys
    private const val KEY_INSTALLED_DATA_VERSION = "installed_data_version"
    private const val KEY_INSTALLED_DATA_CHECKSUM = "installed_data_checksum"
    private const val KEY_INSTALLED_DATA_SIZE = "installed_data_size_bytes"
    private const val KEY_INSTALLED_DATA_TIME = "installed_data_timestamp"

    // Settings keys
    private const val KEY_BACKEND_URL = "backend_base_url"
    private const val KEY_PLAYER_NAME = "player_nickname"
    private const val KEY_FPS_LIMIT = "fps_limit"
    private const val KEY_FAST_CONNECT = "fast_connect"

    // Custom added servers key
    private const val KEY_CUSTOM_SERVERS_JSON = "custom_servers_json"

    // Default emulator host for localhost
    const val DEFAULT_BACKEND_URL = "http://10.0.2.2:3000/"
    const val DEFAULT_PLAYER_NAME = "Carl_Johnson"
  }

  var installedDataVersion: String?
    get() = prefs.getString(KEY_INSTALLED_DATA_VERSION, null)
    set(value) = prefs.edit().putString(KEY_INSTALLED_DATA_VERSION, value).apply()

  var installedDataChecksum: String?
    get() = prefs.getString(KEY_INSTALLED_DATA_CHECKSUM, null)
    set(value) = prefs.edit().putString(KEY_INSTALLED_DATA_CHECKSUM, value).apply()

  var installedDataSizeBytes: Long
    get() = prefs.getLong(KEY_INSTALLED_DATA_SIZE, 0L)
    set(value) = prefs.edit().putLong(KEY_INSTALLED_DATA_SIZE, value).apply()

  var installedDataTimestamp: Long
    get() = prefs.getLong(KEY_INSTALLED_DATA_TIME, 0L)
    set(value) = prefs.edit().putLong(KEY_INSTALLED_DATA_TIME, value).apply()

  var backendBaseUrl: String
    get() = prefs.getString(KEY_BACKEND_URL, DEFAULT_BACKEND_URL) ?: DEFAULT_BACKEND_URL
    set(value) {
      val normalized = if (value.endsWith("/")) value else "$value/"
      prefs.edit().putString(KEY_BACKEND_URL, normalized).apply()
    }

  var playerName: String
    get() = prefs.getString(KEY_PLAYER_NAME, DEFAULT_PLAYER_NAME) ?: DEFAULT_PLAYER_NAME
    set(value) {
      prefs.edit().putString(KEY_PLAYER_NAME, value).apply()
    }

  var fpsLimit: Int
    get() = prefs.getInt(KEY_FPS_LIMIT, 60)
    set(value) = prefs.edit().putInt(KEY_FPS_LIMIT, value).apply()

  var fastConnect: Boolean
    get() = prefs.getBoolean(KEY_FAST_CONNECT, true)
    set(value) = prefs.edit().putBoolean(KEY_FAST_CONNECT, value).apply()

  fun getSavedServers(): List<SampServer> {
    val json = prefs.getString(KEY_CUSTOM_SERVERS_JSON, null) ?: return emptyList()
    return try {
      serverListAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun saveServers(servers: List<SampServer>) {
    try {
      val json = serverListAdapter.toJson(servers)
      prefs.edit().putString(KEY_CUSTOM_SERVERS_JSON, json).apply()
    } catch (e: Exception) {
      // Ignored
    }
  }

  fun recordSuccessfulInstallation(
    version: String,
    checksum: String,
    sizeBytes: Long
  ) {
    prefs.edit()
      .putString(KEY_INSTALLED_DATA_VERSION, version)
      .putString(KEY_INSTALLED_DATA_CHECKSUM, checksum)
      .putLong(KEY_INSTALLED_DATA_SIZE, sizeBytes)
      .putLong(KEY_INSTALLED_DATA_TIME, System.currentTimeMillis())
      .apply()
  }

  fun clearInstalledData() {
    prefs.edit()
      .remove(KEY_INSTALLED_DATA_VERSION)
      .remove(KEY_INSTALLED_DATA_CHECKSUM)
      .remove(KEY_INSTALLED_DATA_SIZE)
      .remove(KEY_INSTALLED_DATA_TIME)
      .apply()
  }
}
