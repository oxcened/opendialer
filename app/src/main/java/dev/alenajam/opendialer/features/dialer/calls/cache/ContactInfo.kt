package dev.alenajam.opendialer.features.dialer.calls.cache

import android.text.TextUtils
import java.io.Serializable

class ContactInfo(
  val name: String? = null,
  val number: String? = null,
  val photoUri: String? = null,
  var type: Int? = 0,
  val label: String? = null,
  val lookupUri: String? = null,
  val normalizedNumber: String? = null,
  val formattedNumber: String? = null,
  val geoDescription: String? = null,
  var photoId: Long? = 0
) : Serializable {
  companion object {
    val EMPTY = ContactInfo()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    if (other === null) {
      return false
    }

    if (other !is ContactInfo) {
      return false
    }

    if (!TextUtils.equals(lookupUri, other.lookupUri)) {
      return false
    }
    if (!TextUtils.equals(name, other.name)) {
      return false
    }
    if (type != other.type) {
      return false
    }
    if (!TextUtils.equals(label, other.label)) {
      return false
    }
    if (!TextUtils.equals(number, other.number)) {
      return false
    }
    if (!TextUtils.equals(formattedNumber, other.formattedNumber)) {
      return false
    }
    if (!TextUtils.equals(normalizedNumber, other.normalizedNumber)) {
      return false
    }
    if (photoId != other.photoId) {
      return false
    }
    if (!TextUtils.equals(photoUri, other.photoUri)) {
      return false
    }
    if (!TextUtils.equals(geoDescription, other.geoDescription)) {
      return false
    }

    return true
  }

  override fun hashCode(): Int {
    // Uses only name and contactUri to determine hashcode.
    // This should be sufficient to have a reasonable distribution of hash codes.
    // Moreover, there should be no two people with the same lookupUri.
    val prime = 31
    var result = 1
    result = prime * result + (lookupUri?.hashCode() ?: 0)
    result = prime * result + (name?.hashCode() ?: 0)
    return result
  }
}