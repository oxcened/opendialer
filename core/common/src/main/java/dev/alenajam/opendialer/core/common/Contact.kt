package dev.alenajam.opendialer.core.common

import java.io.Serializable

data class Contact @JvmOverloads constructor(
    var id: Int = 0,
    var name: String? = null,
    var number: String? = null,
    var starred: Boolean = false,
    var imageUri: String? = null,
    var lookupKey: String? = null,
    var phoneType: Int = 0,
    var phoneLabel: String? = null
) : Serializable {
    // Secondary constructors for specific combinations if needed for legacy Java code
    constructor(name: String?, number: String?) : this(0, name, number)
}
