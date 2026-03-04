package com.softtake.smstgforwarder.util

import java.security.MessageDigest

fun sha256(s: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
}
