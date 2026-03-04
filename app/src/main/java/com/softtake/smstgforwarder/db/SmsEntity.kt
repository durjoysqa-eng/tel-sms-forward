package com.softtake.smstgforwarder.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms",
    indices = [Index(value = ["smsKey"], unique = true)]
)
data class SmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val smsKey: String,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    val sent: Boolean = false,
    val sentAt: Long? = null,
    val failCount: Int = 0
)
