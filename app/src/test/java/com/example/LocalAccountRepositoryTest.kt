package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.VaultDatabase
import com.example.data.repository.LocalAccountRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalAccountRepositoryTest {

    private lateinit var database: VaultDatabase
    private lateinit var repository: LocalAccountRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LocalAccountRepository(database.localAccountDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun signUpAndSignIn_normalAccountFlow_validatesHashesAndPreventsDuplicates() = runBlocking {
        // Invalid inputs rejected cleanly
        assertTrue(repository.signUp("A", "bad-email", "123456", "123456").isFailure)
        assertTrue(repository.signUp("Alex Morgan", "not-an-email", "123456", "123456").isFailure)
        assertTrue(repository.signUp("Alex Morgan", "alex@example.com", "123", "123").isFailure)
        assertTrue(repository.signUp("Alex Morgan", "alex@example.com", "secret12", "mismatch").isFailure)

        // Successful new user sign-up
        val signUpResult = repository.signUp(
            fullName = "  Alex Morgan  ",
            email = "  Alex@Example.COM ",
            password = "CinemaPassword1!",
            confirmPassword = "CinemaPassword1!"
        )
        assertTrue(signUpResult.isSuccess)
        val created = signUpResult.getOrThrow()
        assertEquals("Alex Morgan", created.displayName)
        assertEquals("alex@example.com", created.email)
        assertFalse(created.isCloudSynced)

        // Stored entity must have salted non-plaintext hash
        val stored = database.localAccountDao().getByEmail("alex@example.com")
        assertNotNull(stored)
        assertTrue(stored!!.passwordSalt.isNotBlank())
        assertTrue(stored.passwordHash.isNotBlank())
        assertFalse(stored.passwordHash.contains("CinemaPassword1!"))

        // Duplicate email sign-up blocked
        val dupResult = repository.signUp("Another Alex", "alex@example.com", "otherpass", "otherpass")
        assertTrue(dupResult.isFailure)

        // Wrong password login rejected
        assertTrue(repository.signIn("alex@example.com", "WrongPass").isFailure)
        assertTrue(repository.signIn("missing@example.com", "CinemaPassword1!").isFailure)

        // Valid login succeeds (case-insensitive email)
        val signInResult = repository.signIn("ALEX@example.com", "CinemaPassword1!")
        assertTrue(signInResult.isSuccess)
        val loggedIn = signInResult.getOrThrow()
        assertEquals(created.uid, loggedIn.uid)
        assertEquals("Alex Morgan", loggedIn.displayName)

        // Lookup by ID returns summary
        val fetched = repository.getAccountSummary(created.uid)
        assertNotNull(fetched)
        assertEquals("alex@example.com", fetched!!.email)
    }
}
