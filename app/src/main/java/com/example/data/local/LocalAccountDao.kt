package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocalAccountDao {
    @Query("SELECT * FROM local_accounts WHERE email = :normalizedEmail LIMIT 1")
    suspend fun getByEmail(normalizedEmail: String): LocalAccountEntity?

    @Query("SELECT * FROM local_accounts WHERE id = :accountId LIMIT 1")
    suspend fun getById(accountId: String): LocalAccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: LocalAccountEntity)
}
