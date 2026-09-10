package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SampServer(
  val id: String,
  val name: String,
  val ip: String,
  val port: Int = 7777,
  val players: Int = 0,
  val maxPlayers: Int = 100,
  val gamemode: String = "Freeroam",
  val language: String = "English",
  val ping: Int = 42,
  val isFavorite: Boolean = false,
  val isHosted: Boolean = false,
  val isLocked: Boolean = false,
  val version: String = "0.3.7"
)
