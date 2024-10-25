package com.joniaranguri.garbageguru.domain

import java.io.Serializable

/**
 * This class is immutable and implements [Serializable] to allow
 * photo objects to be serialized and passed between components.
 */
data class RecommendationDetails(
    val localPhotoUri: String,
    val materialType: String
) : Serializable
