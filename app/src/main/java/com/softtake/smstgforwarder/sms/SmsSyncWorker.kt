package com.softtake.smstgforwarder.sms

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.softtake.smstgforwarder.BuildConfig
import com.softtake.smstgforwarder.db.AppDb
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Date

class SmsSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val token = BuildConfig.TG_BOT_TOKEN
        val chatId = BuildConfig.TG_GROUP_ID

        // If secrets are not set, keep data locally and stop
        if (token.isBlank() || chatId.isBlank()) {
            return Result.success()
        }

        val dao = AppDb.get(applicationContext).smsDao()
        val items = dao.getUnsent(limit = 50)
        if (items.isEmpty()) return Result.success()

        for (sms in items) {
            val ok = sendToTelegram(token, chatId, sms.sender, sms.body, sms.receivedAt)
            if (ok) dao.markSent(sms.id, System.currentTimeMillis())
            else {
                dao.incFail(sms.id)
                return Result.retry()
            }
        }
        return Result.success()
    }

    private fun sendToTelegram(token: String, chatId: String, sender: String, body: String, receivedAt: Long): Boolean {
        val text = buildString {
            append("📩 New SMS\n")
            append("From: ").append(sender).append("\n")
            append("Time: ").append(Date(receivedAt)).append("\n")
            append("Message: ").append(body)
        }

        val url = "https://api.telegram.org/bot$token/sendMessage"
        val json = JSONObject().apply {
            put("chat_id", chatId)
            put("text", text)
        }

        return try {
            val client = OkHttpClient()
            val reqBody = json.toString().toRequestBody("application/json".toMediaType())
            val req = Request.Builder().url(url).post(reqBody).build()
            client.newCall(req).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val req = OneTimeWorkRequestBuilder<SmsSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork("sms_sync_once", ExistingWorkPolicy.KEEP, req)
        }
    }
}
