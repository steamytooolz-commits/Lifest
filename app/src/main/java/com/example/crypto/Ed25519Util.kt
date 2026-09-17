package com.example.crypto

import android.util.Base64
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.nio.charset.StandardCharsets

object Ed25519Util {

    fun verifySignature(
        publicKeyBase64: String,
        payloadJson: String,
        signatureBase64: String
    ): Boolean {
        return try {
            val pubBytes = Base64.decode(publicKeyBase64, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val sigBytes = Base64.decode(signatureBase64, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val msgBytes = payloadJson.toByteArray(StandardCharsets.UTF_8)

            // Ed25519 public key is 32 bytes (or last 32 bytes if DER encoded)
            val rawPub = if (pubBytes.size == 32) pubBytes else pubBytes.takeLast(32).toByteArray()
            val pubParams = Ed25519PublicKeyParameters(rawPub, 0)

            val signer = Ed25519Signer()
            signer.init(false, pubParams)
            signer.update(msgBytes, 0, msgBytes.size)
            signer.verifySignature(sigBytes)
        } catch (e: Exception) {
            false
        }
    }

    fun signPayload(
        privateKeyBase64: String,
        payloadJson: String
    ): String? {
        return try {
            val privBytes = Base64.decode(privateKeyBase64, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val msgBytes = payloadJson.toByteArray(StandardCharsets.UTF_8)

            val rawPriv = if (privBytes.size == 32) privBytes else privBytes.takeLast(32).toByteArray()
            val privParams = Ed25519PrivateKeyParameters(rawPriv, 0)

            val signer = Ed25519Signer()
            signer.init(true, privParams)
            signer.update(msgBytes, 0, msgBytes.size)
            val sig = signer.generateSignature()

            Base64.encodeToString(sig, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        } catch (e: Exception) {
            null
        }
    }
}
