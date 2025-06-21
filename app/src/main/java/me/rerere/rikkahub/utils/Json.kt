package me.rerere.rikkahub.utils

import kotlinx.serialization.json.Json

val JsonInstant by lazy {
  Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }
}

val JsonInstantPretty by lazy {
  Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = true
  }
}