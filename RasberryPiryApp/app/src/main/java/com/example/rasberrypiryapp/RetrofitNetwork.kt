package com.example.rasberrypiryapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class List (var result: ArrayList<String>)
data class UploadResult(var saved: Boolean, var fileName: String)
data class UploadingObject(var text: String, var delayTime: Array<Int>, var hasMelody: Int, var content: Array<String>)

interface RetrofitNetwork {
    @GET("list/all")
    fun getList() : Call<List>

    @POST("/load/upload")
    fun upload(@Body body: UploadingObject): Call<UploadResult>
}

