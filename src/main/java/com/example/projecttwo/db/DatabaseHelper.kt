package com.example.projecttwo.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        const val DB_NAME = "weightwatchas.db"
        const val DB_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE weights(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                day TEXT NOT NULL,
                lbs REAL NOT NULL,
                note TEXT,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_weights_user ON weights(user_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_weights_day ON weights(day)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS weights")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }
}