package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class AuthUserSummary(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String
)

class AuthCancelledException(message: String = "Google Sign-In was cancelled") : Exception(message)

class AuthSessionManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun currentUserSummary(): AuthUserSummary? = auth.currentUser?.toSummary()

    fun observeAuthUser(): Flow<AuthUserSummary?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toSummary())
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.toSummary())
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun attemptSilentSignIn(context: Context): AuthUserSummary? {
        auth.currentUser?.let { return it.toSummary() }
        val clientId = runCatching {
            context.applicationContext.getString(R.string.default_web_client_id)
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: return null

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                authResult.user?.toSummary()
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<AuthUserSummary> {
        val clientId = try {
            activityContext.applicationContext.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return Result.failure(IllegalStateException("Google Sign-In configuration missing (default_web_client_id)."))
        }
        if (clientId.isBlank()) {
            return Result.failure(IllegalStateException("Google Sign-In Web Client ID is empty."))
        }

        val activity = activityContext as? Activity
            ?: return Result.failure(IllegalStateException("An active screen context is required for Google Sign-In."))

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        return try {
            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                    ?: return Result.failure(IllegalStateException("Authentication succeeded without a user record."))
                Result.success(user.toSummary())
            } else {
                Result.failure(IllegalStateException("Unexpected credential type returned."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            Result.failure(AuthCancelledException())
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google credentials found on device: ${e.message}", e)
            Result.failure(IllegalStateException("No Google account available on this device. Please add a Google account or continue as Guest."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(IllegalStateException(e.localizedMessage ?: "Google Sign-In failed."))
        }
    }

    suspend fun signOut(context: Context) {
        withContext(NonCancellable) {
            try {
                CredentialManager.create(context.applicationContext)
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear credential state: ${e.message}")
            } finally {
                auth.signOut()
            }
        }
    }

    private fun FirebaseUser.toSummary(): AuthUserSummary {
        val cleanName = displayName?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Cinema Member"
        return AuthUserSummary(
            uid = uid,
            displayName = cleanName,
            email = email.orEmpty(),
            photoUrl = photoUrl?.toString().orEmpty()
        )
    }

    private companion object {
        const val TAG = "Auth"
    }
}
