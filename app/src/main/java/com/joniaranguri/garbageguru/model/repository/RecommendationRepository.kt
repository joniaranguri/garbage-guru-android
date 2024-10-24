package com.joniaranguri.garbageguru.model.repository

class RecommendationRepository {

    fun uploadPhoto(base64Image: String, callback: (String) -> Unit) {
        //TODO: Implement real call to Recommendation API
        callback.invoke("Recicla correctamente este elemento separándolo en el contenedor amarillo.")
    }
}
