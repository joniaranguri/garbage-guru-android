package com.joniaranguri.garbageguru.model.repository

import com.joniaranguri.garbageguru.model.network.APIConfigurator
import com.joniaranguri.garbageguru.model.network.api.RecommendationAPI
import com.joniaranguri.garbageguru.model.network.api.RecommendationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecommendationRepository {

    private val apiService: RecommendationAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(APIConfigurator.RECOMMENDATION_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(RecommendationAPI::class.java)
    }

    fun getRecommendation(materialType: String, callback: (String) -> Unit) {
        apiService.getRecommendation(materialType)
            .enqueue(object : Callback<RecommendationResponse> {
                override fun onResponse(
                    call: Call<RecommendationResponse>,
                    response: Response<RecommendationResponse>
                ) {
                    if (response.isSuccessful) {
                        val recommendation =
                            response.body()?.recommendation ?: "No recommendation available"
                        callback.invoke(recommendation)
                    } else {
                        callback.invoke("Error: ${response.code()}")
                    }

                }

                override fun onFailure(call: Call<RecommendationResponse>, t: Throwable) {
                    callback.invoke("Failed: ${t.message}")
                }
            })
    }
}
