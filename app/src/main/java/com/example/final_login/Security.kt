package com.example.final_login

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Base64
import java.util.Base64.getUrlEncoder
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

class Security {

    // PLEASE REMOVE OR SET AS ENVIRONMENT VARIABLES IN PRODUCTION OR BEFORE PUSHING TO GITHUB
    private val key = "ea97176337b69df46e943af5f8c5f2078610dc5775e2d2744d0defcaf390088c"
    private val iv = "61ea0760d89654d9a5448b6bfabd29f5"

    private val secretKey = key.hexStringToByteArray()
    private val fixedIv = iv.hexStringToByteArray()
    private val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    @RequiresApi(Build.VERSION_CODES.O)
    fun enc(data: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKey, "AES"), IvParameterSpec(fixedIv))
        val encryptedData = cipher.doFinal(data.toByteArray())
        return getUrlEncoder().encodeToString(encryptedData)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dec(encData: String?): String {
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(secretKey, "AES"), IvParameterSpec(fixedIv))
        val decryptedData = cipher.doFinal(Base64.getUrlDecoder().decode(encData))
        return decryptedData.toString(Charsets.UTF_8).trimEnd { it == '\u0000' }
    }

    private fun String.hexStringToByteArray(): ByteArray {
        val len = length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSecureKey(length: Int): String {
        val random = SecureRandom()
        val keyBytes = ByteArray(length)
        random.nextBytes(keyBytes)
        return Base64.getEncoder().encodeToString(keyBytes)
    }
}
