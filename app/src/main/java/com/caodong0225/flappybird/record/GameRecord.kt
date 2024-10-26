package com.caodong0225.flappybird.record

data class GameRecord(
    val appId: String,
    val score: Int,
    val timestamp: Long,
    val location: String,
    val latitude: String,
    val longitude: String,
    val duration: Long,
    val remark: String
)