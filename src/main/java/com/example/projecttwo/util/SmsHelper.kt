package com.example.projecttwo.util

import android.telephony.SmsManager
import kotlin.random.Random

object SmsHelper {
    /** 443 + 7 random digits */
    fun randomPhone443(): String {
        val seven = (0..6).map { Random.nextInt(0, 10) }.joinToString("")
        return "443$seven"
    }

    /** Call only after SEND_SMS is granted */
    fun sendSms(to: String, msg: String) {
        try {
            SmsManager.getDefault().sendTextMessage(to, null, msg, null, null)
        } catch (_: Exception) {
            // swallow for emulator/class scope
        }
    }
}
