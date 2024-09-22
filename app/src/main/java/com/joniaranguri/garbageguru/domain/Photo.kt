package com.joniaranguri.garbageguru.domain

import android.graphics.Bitmap
import java.io.Serializable

/**
 * Represents a photo with a local URI and optional bitmap data.
 * This class is immutable and implements [Serializable] to allow
 * photo objects to be serialized and passed between components.
 */
data class Photo(
    val localUri: String,
    val bitmapData: Bitmap? = null
) : Serializable
