package com.example.data.download

import android.content.Context
import com.example.data.api.ApiClient
import com.example.data.model.DataFileResponse
import com.example.data.storage.DataPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream

sealed interface DownloadStatus {
  object Idle : DownloadStatus
  object Checking : DownloadStatus
  data class UpdateAvailable(
    val release: DataFileResponse,
    val installedVersion: String?
  ) : DownloadStatus
  data class UpToDate(val version: String) : DownloadStatus
  data class Downloading(
    val progress: Float,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speedBps: Long,
    val etaSeconds: Long
  ) : DownloadStatus
  data class VerifyingChecksum(val progress: Float) : DownloadStatus
  data class Unpacking(val progress: Float, val currentFile: String) : DownloadStatus
  data class Success(val version: String, val path: String, val sizeBytes: Long) : DownloadStatus
  data class Error(val message: String, val canRetry: Boolean = true) : DownloadStatus
}

class GameDataDownloadManager(
  private val context: Context,
  private val preferences: DataPreferences
) {
  private val _status = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
  val status: StateFlow<DownloadStatus> = _status.asStateFlow()

  private var downloadJob: Job? = null
  private val client = ApiClient.okHttpClient

  val gameDataDir: File
    get() {
      // Primary: /storage/emulated/0/Android/data/<package_name>/files
      val extDir = context.getExternalFilesDir(null)
      val dir = extDir ?: File(context.filesDir, "files")
      if (!dir.exists()) {
        dir.mkdirs()
      }
      return dir
    }

  val displayStoragePath: String
    get() {
      val abs = gameDataDir.absolutePath
      val idx = abs.indexOf("Android/data/")
      return if (idx != -1) abs.substring(idx) else abs
    }

  init {
    try {
      val dir = gameDataDir
      val infoMarker = File(dir, "samp_storage_info.txt")
      if (!infoMarker.exists()) {
        infoMarker.writeText(
          "SA-MP Mobile Android/data Storage\n" +
          "Files downloaded inside the app are automatically saved and extracted here.\n" +
          "Path: ${dir.absolutePath}\n"
        )
      }
    } catch (e: Exception) {
      // Ignore
    }
  }

  val installedArchiveFile: File
    get() = File(gameDataDir, "game_data.zip")

  fun isGameDataInstalled(): Boolean {
    val version = preferences.installedDataVersion
    return !version.isNullOrEmpty() && installedArchiveFile.exists() && installedArchiveFile.length() > 0
  }

  suspend fun checkForUpdates(): DownloadStatus = withContext(Dispatchers.IO) {
    _status.value = DownloadStatus.Checking
    try {
      val service = ApiClient.createService(preferences.backendBaseUrl)
      val response = service.getLatestDataFile()

      if (response.isSuccessful && response.body() != null) {
        val release = response.body()!!
        val installedVer = preferences.installedDataVersion
        val fileExists = installedArchiveFile.exists() && installedArchiveFile.length() > 0

        // If local file is missing OR version does not match
        if (!fileExists || installedVer == null || !installedVer.equals(release.version, ignoreCase = true)) {
          val update = DownloadStatus.UpdateAvailable(release, installedVer)
          _status.value = update
          return@withContext update
        } else {
          val upToDate = DownloadStatus.UpToDate(installedVer)
          _status.value = upToDate
          return@withContext upToDate
        }
      } else {
        val errorMsg = "Server returned ${response.code()}: ${response.message()}"
        val err = DownloadStatus.Error(errorMsg)
        _status.value = err
        return@withContext err
      }
    } catch (e: Exception) {
      val errorMsg = "Could not check for data updates: ${e.localizedMessage ?: "Connection error"}"
      val err = DownloadStatus.Error(errorMsg)
      _status.value = err
      return@withContext err
    }
  }

  fun startDownload(release: DataFileResponse, scope: CoroutineScope) {
    downloadJob?.cancel()
    downloadJob = scope.launch(Dispatchers.IO) {
      val tempFile = File(context.cacheDir, "download_${System.currentTimeMillis()}.tmp")
      try {
        _status.value = DownloadStatus.Downloading(
          progress = 0f,
          downloadedBytes = 0L,
          totalBytes = release.fileSizeBytes,
          speedBps = 0L,
          etaSeconds = 0L
        )

        // Resolve absolute download URL (in case it returns relative or localhost for Android emulator)
        var targetUrl = release.fileUrl
        if (targetUrl.startsWith("/")) {
          val base = preferences.backendBaseUrl.trimEnd('/')
          targetUrl = "$base$targetUrl"
        }
        // If server provided 127.0.0.1 or localhost, and running in standard emulator, map to 10.0.2.2
        if (targetUrl.contains("127.0.0.1") && preferences.backendBaseUrl.contains("10.0.2.2")) {
          targetUrl = targetUrl.replace("127.0.0.1", "10.0.2.2")
        } else if (targetUrl.contains("localhost") && preferences.backendBaseUrl.contains("10.0.2.2")) {
          targetUrl = targetUrl.replace("localhost", "10.0.2.2")
        }

        val request = Request.Builder().url(targetUrl).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
          throw IllegalStateException("HTTP ${response.code}: ${response.message}")
        }

        val body = response.body ?: throw IllegalStateException("Empty response body from server")
        val totalBytes = if (body.contentLength() > 0) body.contentLength() else release.fileSizeBytes
        var downloadedBytes = 0L

        val digest = MessageDigest.getInstance("SHA-256")
        val inputStream: InputStream = body.byteStream()
        val outputStream = FileOutputStream(tempFile)

        val buffer = ByteArray(32 * 1024)
        var bytesRead: Int
        var lastUpdateTime = System.currentTimeMillis()
        var bytesSinceLastUpdate = 0L
        var speedBps = 0L

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
          outputStream.write(buffer, 0, bytesRead)
          digest.update(buffer, 0, bytesRead)
          downloadedBytes += bytesRead
          bytesSinceLastUpdate += bytesRead

          val now = System.currentTimeMillis()
          val elapsed = now - lastUpdateTime
          if (elapsed >= 300) {
            speedBps = if (elapsed > 0) (bytesSinceLastUpdate * 1000) / elapsed else 0L
            val eta = if (speedBps > 0 && totalBytes > downloadedBytes) {
              (totalBytes - downloadedBytes) / speedBps
            } else 0L

            val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes.toFloat() else 0f
            _status.value = DownloadStatus.Downloading(
              progress = progress.coerceIn(0f, 1f),
              downloadedBytes = downloadedBytes,
              totalBytes = totalBytes,
              speedBps = speedBps,
              etaSeconds = eta
            )
            lastUpdateTime = now
            bytesSinceLastUpdate = 0L
          }
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()

        // Step 2: SHA-256 Integrity Verification
        _status.value = DownloadStatus.VerifyingChecksum(progress = 0.5f)
        val calculatedChecksum = digest.digest().joinToString("") { "%02x".format(it) }

        val expectedChecksum = release.checksum.trim().lowercase()
        val actualChecksum = calculatedChecksum.trim().lowercase()

        if (expectedChecksum.isNotEmpty() && !expectedChecksum.equals(actualChecksum, ignoreCase = true)) {
          tempFile.delete()
          throw IllegalStateException(
            "Checksum mismatch! Corrupted or altered download.\n" +
                "Expected: $expectedChecksum\nGot: $actualChecksum"
          )
        }

        _status.value = DownloadStatus.VerifyingChecksum(progress = 1.0f)

        // Step 3: Unpacking and installing into game data directory
        _status.value = DownloadStatus.Unpacking(progress = 0.2f, currentFile = "Verifying package structure...")

        // Move to persistent game directory
        if (installedArchiveFile.exists()) {
          installedArchiveFile.delete()
        }
        tempFile.copyTo(installedArchiveFile, overwrite = true)
        tempFile.delete()

        // Try extracting known game config/files if it's a zip
        extractZipSafely(installedArchiveFile, gameDataDir)

        // Step 4: Record in SharedPreferences
        preferences.recordSuccessfulInstallation(
          version = release.version,
          checksum = actualChecksum,
          sizeBytes = downloadedBytes
        )

        _status.value = DownloadStatus.Success(
          version = release.version,
          path = gameDataDir.absolutePath,
          sizeBytes = downloadedBytes
        )
      } catch (c: CancellationException) {
        tempFile.delete()
        _status.value = DownloadStatus.Idle
      } catch (e: Exception) {
        tempFile.delete()
        _status.value = DownloadStatus.Error(
          message = e.localizedMessage ?: "Failed to download data files"
        )
      }
    }
  }

  fun cancelDownload() {
    downloadJob?.cancel()
    downloadJob = null
    _status.value = DownloadStatus.Idle
  }

  fun dismissStatus() {
    _status.value = DownloadStatus.Idle
  }

  private fun extractZipSafely(zipFile: File, outputDir: File) {
    try {
      ZipInputStream(zipFile.inputStream()).use { zis ->
        var entry = zis.nextEntry
        var count = 0
        while (entry != null) {
          count++
          val outFile = File(outputDir, entry.name)
          val canonicalDest = outputDir.canonicalPath
          val canonicalEntry = outFile.canonicalPath
          // Prevent zip slip vulnerability
          if (!canonicalEntry.startsWith(canonicalDest + File.separator) && canonicalEntry != canonicalDest) {
            entry = zis.nextEntry
            continue
          }

          if (entry.isDirectory) {
            outFile.mkdirs()
          } else {
            outFile.parentFile?.mkdirs()
            FileOutputStream(outFile).use { fos ->
              val buffer = ByteArray(16 * 1024)
              var len: Int
              while (zis.read(buffer).also { len = it } > 0) {
                fos.write(buffer, 0, len)
              }
            }
          }
          _status.value = DownloadStatus.Unpacking(
            progress = (count * 0.1f).coerceIn(0.3f, 0.95f),
            currentFile = entry.name
          )
          zis.closeEntry()
          entry = zis.nextEntry
        }
      }
    } catch (e: Exception) {
      // If not a standard zip or single archive, keep game_data.zip intact
    }
  }

  fun verifyInstalledFileIntegrity(): Pair<Boolean, String> {
    if (!installedArchiveFile.exists()) {
      return Pair(false, "Data file not found on disk")
    }
    val recordedHash = preferences.installedDataChecksum
    if (recordedHash.isNullOrEmpty()) {
      return Pair(false, "No recorded checksum in preferences")
    }

    return try {
      val digest = MessageDigest.getInstance("SHA-256")
      val stream = installedArchiveFile.inputStream()
      val buffer = ByteArray(32 * 1024)
      var bytesRead: Int
      while (stream.read(buffer).also { bytesRead = it } != -1) {
        digest.update(buffer, 0, bytesRead)
      }
      stream.close()
      val calculated = digest.digest().joinToString("") { "%02x".format(it) }
      val match = calculated.equals(recordedHash.trim(), ignoreCase = true)
      Pair(match, if (match) "SHA-256 verified successfully: $calculated" else "Hash mismatch! Expected: $recordedHash, Got: $calculated")
    } catch (e: Exception) {
      Pair(false, "Verification error: ${e.message}")
    }
  }
}
