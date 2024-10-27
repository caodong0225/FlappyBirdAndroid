package com.caodong0225.flappybird.api

import com.caodong0225.flappybird.record.GameRecord
import com.caodong0225.flappybird.record.UploadResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/records/submit")
    fun uploadGameRecord(@Body gameRecord: GameRecord): Call<UploadResponse>
}