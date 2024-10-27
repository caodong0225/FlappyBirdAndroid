package com.caodong0225.flappybird.Repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.caodong0225.flappybird.record.GameRecord
import com.caodong0225.flappybird.util.GameDatabaseHelper

class GameRepository(context: Context) {
    private val dbHelper = GameDatabaseHelper(context)

    fun insertGameRecord(record: GameRecord) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(GameDatabaseHelper.APP_ID, record.appId)
            put(GameDatabaseHelper.COLUMN_SCORE, record.score)
            put(GameDatabaseHelper.COLUMN_TIMESTAMP, record.timestamp)
            put(GameDatabaseHelper.COLUMN_LATITUDE, record.latitude)
            put(GameDatabaseHelper.COLUMN_LONGITUDE, record.longitude)
            put(GameDatabaseHelper.COLUMN_LOCATION, record.location)
            put(GameDatabaseHelper.DURATION, record.duration)
            put(GameDatabaseHelper.REMARK, record.remark)
        }
        db.insert(GameDatabaseHelper.TABLE_NAME, null, values)
        db.close()
    }

    fun deleteGameRecord(timestamp: Long) {
        val db = dbHelper.writableDatabase
        db.delete(GameDatabaseHelper.TABLE_NAME, "${GameDatabaseHelper.COLUMN_TIMESTAMP} = ?", arrayOf(timestamp.toString()))
        db.close()
    }

    fun getAllGameRecords(): List<GameRecord> {
        val db = dbHelper.readableDatabase
        val gameRecords = mutableListOf<GameRecord>()

        val cursor: Cursor = db.query(
            GameDatabaseHelper.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "${GameDatabaseHelper.COLUMN_TIMESTAMP} DESC" // 按时间戳降序排序
        )

        if (cursor.moveToFirst()) {
            do {
                val appId = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.APP_ID))
                val score = cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_SCORE))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_TIMESTAMP))
                val location = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_LOCATION))
                val latitude = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_LATITUDE))
                val longitude = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_LONGITUDE))
                val remark = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.REMARK))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(GameDatabaseHelper.DURATION))
                val gameRecord = GameRecord(appId, score, timestamp, location, latitude, longitude, duration, remark)
                gameRecords.add(gameRecord)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return gameRecords
    }
}