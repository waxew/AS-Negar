package com.dot.gallery.feature_node.presentation.vault.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dot.gallery.core.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

/** The type of custom authentication set for a vault. */
enum class VaultAuthType { PIN, PATTERN, PASSWORD }

/**
 * Manages per-vault custom authentication (PIN / Pattern / Password) using DataStore.
 * Credentials are stored as salted SHA-256 hashes: `"type:salt:hash"`.
 * When no custom auth is set, device biometric/credential auth is used.
 */
object VaultPasswordManager {

    private const val SALT_LENGTH = 16

    private fun keyFor(vaultUuid: UUID) =
        stringPreferencesKey("vault_password_${vaultUuid}")

    /** Returns true if any custom auth has been set for the given vault. */
    suspend fun hasCustomPassword(context: Context, vaultUuid: UUID): Boolean {
        val key = keyFor(vaultUuid)
        return context.dataStore.data.map { prefs -> prefs[key] != null }.first()
    }

    /** Returns the auth type for the given vault, or null if none is set. */
    suspend fun getAuthType(context: Context, vaultUuid: UUID): VaultAuthType? {
        val key = keyFor(vaultUuid)
        val stored = context.dataStore.data.map { prefs -> prefs[key] }.first() ?: return null
        val typePart = stored.substringBefore(":")
        return runCatching { VaultAuthType.valueOf(typePart) }.getOrNull()
    }

    /** Sets custom authentication for the vault with the given [type] and [secret]. */
    suspend fun setPassword(
        context: Context,
        vaultUuid: UUID,
        secret: String,
        type: VaultAuthType = VaultAuthType.PASSWORD
    ) {
        val key = keyFor(vaultUuid)
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val hash = hashPassword(secret, salt)
        val stored = "${type.name}:${salt.toHex()}:${hash.toHex()}"
        context.dataStore.edit { prefs -> prefs[key] = stored }
    }

    /** Removes custom auth, reverting to device security. */
    suspend fun removePassword(context: Context, vaultUuid: UUID) {
        val key = keyFor(vaultUuid)
        context.dataStore.edit { prefs -> prefs.remove(key) }
    }

    /** Verifies the entered [secret] against the stored hash. Returns true on match. */
    suspend fun verifyPassword(context: Context, vaultUuid: UUID, secret: String): Boolean {
        val key = keyFor(vaultUuid)
        val stored = context.dataStore.data.map { prefs -> prefs[key] }.first() ?: return false
        val parts = stored.split(":")
        // Format: type:salt:hash (3 parts) or legacy salt:hash (2 parts)
        val salt: ByteArray
        val expectedHash: ByteArray
        when (parts.size) {
            3 -> {
                salt = parts[1].hexToBytes()
                expectedHash = parts[2].hexToBytes()
            }
            2 -> {
                // Legacy format (password only)
                salt = parts[0].hexToBytes()
                expectedHash = parts[1].hexToBytes()
            }
            else -> return false
        }
        val actualHash = hashPassword(secret, salt)
        return MessageDigest.isEqual(expectedHash, actualHash)
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
