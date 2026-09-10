package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DataFileResponse(
  @Json(name = "version") val version: String,
  @Json(name = "file_url") val fileUrl: String,
  @Json(name = "file_name") val fileName: String = "",
  @Json(name = "file_size_bytes") val fileSizeBytes: Long = 0L,
  @Json(name = "checksum") val checksum: String,
  @Json(name = "release_notes") val releaseNotes: String? = null,
  @Json(name = "uploaded_at") val uploadedAt: String? = null
)
