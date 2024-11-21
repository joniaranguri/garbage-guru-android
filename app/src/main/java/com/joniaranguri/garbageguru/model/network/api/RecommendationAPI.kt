package com.joniaranguri.garbageguru.model.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Define the data class for the request body
data class RecommendationRequest(val materialType: String)

// Define the data class for the API response
data class RecommendationResponse(
    @SerializedName("recommendation")
    val recommendation: String
)

interface RecommendationAPI {
    @GET("/recommendation")
    fun getRecommendation(@Query("materialType") materialType: String): Call<RecommendationResponse>
}
