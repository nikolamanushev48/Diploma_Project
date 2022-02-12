package org.elsys.diploma

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
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
    fun locationLoadData() : LiveData<List<MarkerData>>
    fun imageSave(image : Bitmap,documentId : String,trashCleanedMode : Boolean,onSuccess : (uri : Uri)->Unit)
    fun deleteLocation(marker : Marker) : Boolean
    fun displayImage(documentId: String,onSuccess : (uri : Uri)->Unit)
    fun addLocationData(position: LatLng)
}


