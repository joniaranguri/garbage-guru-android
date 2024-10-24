package com.joniaranguri.garbageguru.model.repository

import com.joniaranguri.garbageguru.model.network.APIConfigurator
import com.joniaranguri.garbageguru.model.network.api.AIClassificationAPI
import com.joniaranguri.garbageguru.model.network.api.AIClassificationResponse
import com.joniaranguri.garbageguru.model.network.api.AIClassificationRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AIClassificationRepository {

    private val apiService: AIClassificationAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(APIConfigurator.AI_CLASSIFICATION_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(AIClassificationAPI::class.java)
    }

    fun classifyImage(base64Image: String, callback: (String) -> Unit) {
        val request = AIClassificationRequest(image = base64Image)

        apiService.classifyImage(request).enqueue(object : Callback<AIClassificationResponse> {
            override fun onResponse(
                call: Call<AIClassificationResponse>,
                response: Response<AIClassificationResponse>
            ) {
                if (response.isSuccessful) {
                    val recommendation = response.body()?.materialType ?: "No recommendation available"
                    callback.invoke(recommendation)
                } else {
                    callback.invoke("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AIClassificationResponse>, t: Throwable) {
                callback.invoke("Failed: ${t.message}")
            }
        })
    }
}
