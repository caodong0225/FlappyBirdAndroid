package com.caodong0225.flappybird.gameData

import com.caodong0225.flappybird.api.ApiService
import com.caodong0225.flappybird.record.GameRecord
import com.caodong0225.flappybird.record.UploadResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun uploadGameRecord(record: GameRecord) {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.31.62:8080/") // 替换为你的服务器地址
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)

    apiService.uploadGameRecord(record).enqueue(object : Callback<UploadResponse> {
        override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
            if (response.isSuccessful) {
                response.body()?.let { uploadResponse ->
                    println("记录上传成功: ${uploadResponse.message}")
                } ?: println("上传失败: 响应体为空")
            } else {
                println("上传失败: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
            println("网络错误: ${t.message}")
        }
    })
}