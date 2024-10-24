package com.joniaranguri.garbageguru.model.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Define the data class for the request body
data class RecommendationRequest(val materialType: String)

// Define the data class for the API response
data class RecommendationResponse(val message: String)

interface RecommendationAPI {
    @POST("/recommendation")
    fun getRecommendation(@Body request: RecommendationRequest): Call<RecommendationResponse>
}
