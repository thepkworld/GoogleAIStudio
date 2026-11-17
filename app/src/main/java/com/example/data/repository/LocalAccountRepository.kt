package com.example.data.repository

import android.content.Context
import com.example.auth.AuthUserSummary
import com.example.data.local.LocalAccountDao
import com.example.data.local.LocalAccountEntity
import com.example.data.local.VaultDatabase
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAccountRepository(
    private val dao: LocalAccountDao
) {
    constructor(context: Context) : this(VaultDatabase.getInstance(context).localAccountDao())

    suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<AuthUserSummary> = withContext(Dispatchers.IO) {
        try {
            val cleanName = fullName.trim()
            val cleanEmail = email.trim().lowercase(Locale.US)
            if (cleanName.length < 2) {
                return@withContext Result.failure(IllegalArgumentException("Please enter your full name (at least 2 characters)."))
            }
            if (!EMAIL_REGEX.matches(cleanEmail)) {
                return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
            }
            if (password.length < 6) {
                return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters long."))
            }
            if (password != confirmPassword) {
                return@withContext Result.failure(IllegalArgumentException("Passwords do not match."))
            }

            val existing = dao.getByEmail(cleanEmail)
            if (existing != null) {
                return@withContext Result.failure(
                    IllegalStateException("An account with $cleanEmail already exists. Please Sign In instead.")
                )
            }

            val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val saltHex = saltBytes.toHex()
            val hashHex = hashPassword(password, saltBytes).toHex()
            val entity = LocalAccountEntity(
                id = "local_${UUID.randomUUID().toString().replace("-", "").take(16)}",
                displayName = cleanName.take(80),
                email = cleanEmail.take(120),
                passwordSalt = saltHex,
                passwordHash = hashHex,
                createdAt = System.currentTimeMillis()
            )
            dao.insertAccount(entity)
            Result.success(entity.toAuthUserSummary())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.localizedMessage ?: "Could not create account."))
        }
    }

    suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthUserSummary> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim().lowercase(Locale.US)
            if (!EMAIL_REGEX.matches(cleanEmail)) {
                return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
            }
            if (password.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Please enter your password."))
            }

            val account = dao.getByEmail(cleanEmail)
                ?: return@withContext Result.failure(
                    IllegalStateException("No account found for $cleanEmail. Create a new account first.")
                )

            val saltBytes = account.passwordSalt.hexToBytes()
            val expectedBytes = account.passwordHash.hexToBytes()
            val actualBytes = hashPassword(password, saltBytes)
            if (!MessageDigest.isEqual(actualBytes, expectedBytes)) {
                return@withContext Result.failure(IllegalStateException("Incorrect password. Please try again."))
            }

            Result.success(account.toAuthUserSummary())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.localizedMessage ?: "Sign-in failed."))
        }
    }

    suspend fun getAccountSummary(accountId: String): AuthUserSummary? = withContext(Dispatchers.IO) {
        runCatching { dao.getById(accountId)?.toAuthUserSummary() }.getOrNull()
    }

    private fun LocalAccountEntity.toAuthUserSummary(): AuthUserSummary {
        return AuthUserSummary(
            uid = id,
            displayName = displayName,
            email = email,
            photoUrl = "",
            isCloudSynced = false
        )
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        return try {
            val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } catch (_: Exception) {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            digest.digest(password.toByteArray(Charsets.UTF_8))
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        val len = length
        if (len % 2 != 0) return ByteArray(0)
        return ByteArray(len / 2) { i ->
            val idx = i * 2
            ((Character.digit(this[idx], 16) shl 4) + Character.digit(this[idx + 1], 16)).toByte()
        }
    }

    private companion object {
        const val PBKDF2_ITERATIONS = 30_000
        const val KEY_LENGTH_BITS = 256
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
