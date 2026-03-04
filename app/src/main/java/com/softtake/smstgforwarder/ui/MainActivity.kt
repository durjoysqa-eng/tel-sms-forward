package com.softtake.smstgforwarder.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.softtake.smstgforwarder.BuildConfig
import com.softtake.smstgforwarder.R
import com.softtake.smstgforwarder.sms.SmsSyncWorker

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        refreshStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.status)
        val btnRequest = findViewById<Button>(R.id.btnRequest)
        val btnTest = findViewById<Button>(R.id.btnTest)

        btnRequest.setOnClickListener {
            requestPermission.launch(Manifest.permission.RECEIVE_SMS)
        }

        btnTest.setOnClickListener {
            SmsSyncWorker.enqueue(this)
            refreshStatus(extra = "Sync queued.")
        }

        refreshStatus()
    }

    private fun refreshStatus(extra: String? = null) {
        val perm = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val tokenOk = BuildConfig.TG_BOT_TOKEN.isNotBlank()
        val groupOk = BuildConfig.TG_GROUP_ID.isNotBlank()

        val lines = mutableListOf<String>()
        lines += "SMS Permission: " + if (perm) "GRANTED" else "NOT GRANTED"
        lines += "Telegram Token: " + if (tokenOk) "SET" else "NOT SET (local.properties)"
        lines += "Telegram Group ID: " + if (groupOk) "SET" else "NOT SET (local.properties)"
        if (!tokenOk || !groupOk) {
            lines += "⚠️ App will store SMS but cannot send until secrets are set."
        }
        if (extra != null) lines += extra

        status.text = "Status:\n" + lines.joinToString("\n")
    }
}
