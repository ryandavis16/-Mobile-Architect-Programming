package com.example.projecttwo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projecttwo.util.SmsHelper

class SmsActivity : AppCompatActivity() {

    private companion object {
        private const val REQUEST_SEND_SMS = 2001
    }

    // hold pending text if we need to request permission first
    private var pendingTo: String? = null
    private var pendingMsg: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)

        // Local view refs: these IDs must exist in activity_sms.xml
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etMsg   = findViewById<EditText>(R.id.etMsg)
        val btnSend = findViewById<Button>(R.id.btnSend)

        btnSend.setOnClickListener {
            val to  = etPhone.text.toString().ifBlank { SmsHelper.randomPhone443() }
            val msg = etMsg.text.toString().ifBlank { "Weight Watcha’s reminder: stay on track!" }
            maybeSendSms(to, msg)
        }
    }

    private fun maybeSendSms(to: String, msg: String) {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) {
            SmsHelper.sendSms(to, msg)
            Toast.makeText(this, "SMS sent to $to", Toast.LENGTH_SHORT).show()
        } else {
            pendingTo = to
            pendingMsg = msg
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_SEND_SMS)
            Toast.makeText(this, "Requesting SMS permission…", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SEND_SMS && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val to = pendingTo
            val msg = pendingMsg
            if (!to.isNullOrBlank() && !msg.isNullOrBlank()) {
                SmsHelper.sendSms(to, msg)
                Toast.makeText(this, "SMS sent to $to", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_SEND_SMS) {
            Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
        pendingTo = null
        pendingMsg = null
    }
}
