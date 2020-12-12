package com.example.rasberrypiryapp

import android.media.Image
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

data class List (var result: ArrayList<String>)
data class UploadResult(var saved: Boolean, var fileName: String)
data class UploadingObject(var text: String, var delayTime: Array<Int>, var content: Array<String>, var hasMelody: Int = 0, var type: Int? = null)
data class UploadImage(var image: Image)
data class UploadImagePixelfyResult(var pixel: ArrayList<ArrayList<String>>)

interface RetrofitNetwork {
    @GET("list/all")
    fun getList() : Call<List>

    @POST("/load/upload")
    fun upload(@Body body: UploadingObject): Call<UploadResult>

    @Multipart
    @POST("/load/pixelfy")
    fun uploadImage(@Part img: MultipartBody.Part): Call<UploadImagePixelfyResult>
}

