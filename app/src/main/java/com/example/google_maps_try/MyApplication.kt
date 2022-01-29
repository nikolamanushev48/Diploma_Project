package com.example.google_maps_try

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MyApplication : Application() {
    lateinit var apiService: ApiService
    override fun onCreate() {
        super.onCreate()

        apiService = ApiServiceImpl()



    }
}


interface ApiService{
    fun locationLoadData() : LiveData<List<PinData>>
    fun imageSave(image : Bitmap,documentId : String,trashCleanedMode : Boolean)
    fun deleteLocation(marker : Marker) : Boolean
    fun displayImage(documentId: String)
    fun addLocationData(position: LatLng)
}


