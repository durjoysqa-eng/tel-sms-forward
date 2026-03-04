package com.softtake.smstgforwarder.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.softtake.smstgforwarder.db.AppDb
import com.softtake.smstgforwarder.db.SmsEntity
import com.softtake.smstgforwarder.util.sha256
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages.first().originatingAddress ?: "Unknown"
        val body = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val ts = messages.first().timestampMillis

        val smsKey = sha256("$sender|$ts|$body")

        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDb.get(context).smsDao()
            // IGNORE duplicates via unique smsKey index
            dao.insert(
                SmsEntity(
                    smsKey = smsKey,
                    sender = sender,
                    body = body,
                    receivedAt = ts
                )
            )
            // Queue sync (will run when internet is available)
            SmsSyncWorker.enqueue(context)
        }
    }
}
