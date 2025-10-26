package com.example.projecttwo.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.projecttwo.db.DatabaseHelper

class WeightRepo(ctx: Context) {
    private val helper = DatabaseHelper(ctx)

    fun allForUser(userId: Long): Cursor {
        val db = helper.readableDatabase
        return db.rawQuery(
            "SELECT id AS _id, day, lbs, COALESCE(note,'') AS note FROM weights WHERE user_id=? ORDER BY day DESC",
            arrayOf(userId.toString())
        )
    }

    fun add(userId: Long, day: String, lbs: Double, note: String?): Long {
        val db = helper.writableDatabase
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("day", day)
            put("lbs", lbs)
            put("note", note)
        }
        return db.insert("weights", null, cv)
    }

    fun update(id: Long, day: String, lbs: Double, note: String?): Int {
        val db = helper.writableDatabase
        val cv = ContentValues().apply {
            put("day", day)
            put("lbs", lbs)
            put("note", note)
        }
        return db.update("weights", cv, "id=?", arrayOf(id.toString()))
    }

    fun delete(id: Long): Int {
        val db = helper.writableDatabase
        return db.delete("weights", "id=?", arrayOf(id.toString()))
    }
}