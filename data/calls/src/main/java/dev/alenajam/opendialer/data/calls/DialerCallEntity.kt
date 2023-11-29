package dev.alenajam.opendialer.data.calls

class DialerCallEntity(
  val id: Int,
  val number: String?,
  val name: String?,
  val date: Long,
  val duration: Long,
  val type: Int,
  val isNew: Int,
  val photoUri: String?,
  val countryIso: String? = null,
  val label: String?,
  val lookupUri: String?,
  val normalizedNumber: String? = null,
  val formattedNumber: String? = null,
  val geoDescription: String? = null,
  val photoId: Long? = null,
  val postDialDigits: String? = null,
  val matchedNumber: String? = null,
  val numberType: Int? = null
)