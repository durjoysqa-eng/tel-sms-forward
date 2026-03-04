package com.softtake.smstgforwarder.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sms: SmsEntity): Long

    @Query("SELECT * FROM sms WHERE sent = 0 ORDER BY receivedAt ASC LIMIT :limit")
    suspend fun getUnsent(limit: Int = 50): List<SmsEntity>

    @Query("UPDATE sms SET sent = 1, sentAt = :sentAt WHERE id = :id")
    suspend fun markSent(id: Long, sentAt: Long)

    @Query("UPDATE sms SET failCount = failCount + 1 WHERE id = :id")
    suspend fun incFail(id: Long)
}
