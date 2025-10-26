package com.example.projecttwo.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.projecttwo.db.DatabaseHelper

class UserRepo(ctx: Context) {
    private val helper = DatabaseHelper(ctx)

    fun register(username: String, password: String): Long {
        val db = helper.writableDatabase
        val cv = ContentValues().apply {
            put("username", username.trim())
            put("password", password) // plain text for course scope only
        }
        return db.insertOrThrow("users", null, cv)
    }

    fun login(username: String, password: String): Long {
        val db = helper.readableDatabase
        db.rawQuery(
            "SELECT id FROM users WHERE username=? AND password=?",
            arrayOf(username.trim(), password)
        ).use { c: Cursor ->
            return if (c.moveToFirst()) c.getLong(0) else -1L
        }
    }
}