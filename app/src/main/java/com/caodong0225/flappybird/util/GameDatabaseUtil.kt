package com.caodong0225.flappybird.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GameDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "game.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "game_records"
        const val COLUMN_ID = "id"
        const val APP_ID = "app_id"
        const val COLUMN_SCORE = "score"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val DURATION = "duration"
        const val REMARK = "remark"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $APP_ID VARCHAR(255),
                $COLUMN_SCORE INTEGER,
                $COLUMN_TIMESTAMP LONG,
                $COLUMN_LOCATION VARCHAR(255),
                $COLUMN_LATITUDE VARCHAR(255),
                $COLUMN_LONGITUDE VARCHAR(255),
                $DURATION LONG,
                $REMARK TEXT
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}