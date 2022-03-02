package org.elsys.diploma


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarkerData(
    val latitude: Double, val longitude: Double, val isCleaned: Boolean, val documentId: String,
    val creator: String, val cleanedBy: String
) : Parcelable
