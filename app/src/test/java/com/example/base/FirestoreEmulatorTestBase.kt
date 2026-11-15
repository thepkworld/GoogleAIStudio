package com.example.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.robolectric.annotation.LooperMode
import java.net.HttpURLConnection
import java.net.URL

@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
abstract class FirestoreEmulatorTestBase {
    protected lateinit var app: FirebaseApp
    protected lateinit var firestore: FirebaseFirestore
    protected lateinit var auth: FirebaseAuth

    private val databaseId: String by lazy {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.getString(R.string.firestore_database_id)
    }

    @Before
    fun setUpFirebaseEmulator() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val existingApp = FirebaseApp.getApps(context).firstOrNull { it.name == "test-app" }
        app = existingApp ?: FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder()
                .setProjectId("pk-cineflix")
                .setApplicationId("1:653800279969:android:d62efc69c4e435701885e0")
                .setApiKey("fake-api-key")
                .build(),
            "test-app"
        )
        firestore = FirebaseFirestore.getInstance(app, databaseId)
        if (existingApp == null) {
            firestore.useEmulator("127.0.0.1", 8085)
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
        }
        auth = FirebaseAuth.getInstance(app)
        if (existingApp == null) {
            auth.useEmulator("127.0.0.1", 9099)
        }
        auth.signOut()
    }

    @After
    fun tearDownFirebaseEmulator() {
        auth.signOut()
        clearFirestoreEmulatorData("pk-cineflix", databaseId)
    }

    protected suspend fun signInAsTestUser(email: String, name: String): String {
        val json = """{"email":"$email","emailVerified":true,"displayName":"$name","sub":"$email"}"""
        val credential = com.google.firebase.auth.OAuthProvider.newCredentialBuilder("google.com")
            .setIdToken(json)
            .build()
        val result = auth.signInWithCredential(credential).await()
        return result.user!!.uid
    }

    private fun clearFirestoreEmulatorData(projectId: String, dbId: String) {
        val url = URL("http://127.0.0.1:8085/emulator/v1/projects/$projectId/databases/$dbId/documents")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 2000
            readTimeout = 2000
        }
        runCatching { conn.responseCode }
        conn.disconnect()
    }
}
