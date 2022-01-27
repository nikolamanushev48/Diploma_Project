package com.example.google_maps_try

import android.app.Application
import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.GeoPoint

class MyApplication : Application() {
    lateinit var apiService: ApiService
    override fun onCreate() {
        super.onCreate()

        apiService = ApiServiceImpl()



    }
}


interface ApiService{
    fun locationData() : LiveData<List<PinData>>
    fun imageSave(image : Bitmap,documentId : String,trashCleanedMode : Boolean)
    fun deleteLocation(docId : String)

}


