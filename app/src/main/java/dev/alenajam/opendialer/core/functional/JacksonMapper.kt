package dev.alenajam.opendialer.core.functional

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JacksonMapper : ObjectMapper() {
  init {
    registerKotlinModule()
  }
}