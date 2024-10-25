package com.joniaranguri.garbageguru.model.repository

import com.joniaranguri.garbageguru.model.network.APIConfigurator
import com.joniaranguri.garbageguru.model.network.api.RecommendationAPI
import com.joniaranguri.garbageguru.model.network.api.RecommendationRequest
import com.joniaranguri.garbageguru.model.network.api.RecommendationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RecommendationRepository {

    private val apiService: RecommendationAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(APIConfigurator.RECOMMENDATION_API_BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        apiService = retrofit.create(RecommendationAPI::class.java)
    }

    fun getRecommendation(materialType: String, callback: (String) -> Unit) {
        apiService.getRecommendation(materialType).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    val recommendation = response.body() ?: "No recommendation available"
                    callback.invoke(recommendation)
                } else {
                    callback.invoke("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback.invoke("Failed: ${t.message}")
            }
        })
    }
}
