package com.joniaranguri.garbageguru.model.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Define the data class for the request body
data class AIClassificationRequest(
    @SerializedName("image_base64")
    val image: String
)

// Define the data class for the API response
data class AIClassificationResponse(
    @SerializedName("class")
    val materialType: String
)

interface AIClassificationAPI {
    @POST("/clasificar")
    fun classifyImage(@Body request: AIClassificationRequest): Call<AIClassificationResponse>
}
