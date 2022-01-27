package com.example.google_maps_try

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class ApiServiceImpl : ApiService {

    init {
       getLocations()
    }


    val lifeData : MutableLiveData<List<PinData>> = MutableLiveData(listOf())

    override fun locationData(): LiveData<List<PinData>> {
        return lifeData;
    }

    override fun imageSave(image: Bitmap,documentId : String,trashCleanedMode : Boolean) {
        val storageRef = FirebaseStorage.getInstance().reference

        val tempAddress = UUID.randomUUID().toString()

        val photoRef = storageRef.child("photos")
            .child(tempAddress)

        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val dataBytes = baos.toByteArray()


        photoRef.putBytes(dataBytes).addOnCompleteListener() {
            if (it.isSuccessful) {
                val downloadUri = it.result
            }


            val localFile = File.createTempFile("images", "jpg")

            photoRef.getFile(localFile).addOnSuccessListener {
                imageViewRef.setImageURI(localFile.toUri())

                val database = FirebaseFirestore.getInstance()
                val docRef = database.collection("locations").document(documentId)

                if(trashCleanedMode == true) {
                    //docRef.update("photoAddress", tempAddress)
                    docRef.update("photoAddress", tempAddress,"isClean",true).addOnCompleteListener{

                    }

                }else{
                    docRef.update("photoAddress", tempAddress)
                }


            }.addOnFailureListener { be ->
                Log.w(ContentValues.TAG, "Error getting file", be)
            }


        }
    }

    override fun deleteLocation(docId: String) {
        TODO("Not yet implemented")
    }

    private fun getLocations() {

        val database = FirebaseFirestore.getInstance()
        /*
        database.collection("locations")
            .get()
            .addOnCompleteListener { task ->
                val list : MutableList<PinData> = mutableListOf<PinData>()

                if (task.isSuccessful) {

                    for (document in task.result) {
                        val geoPoint = document.getGeoPoint("coordinates")
                        val isClean = document.getBoolean("isClean")?:false


                        if (geoPoint != null) {
                            val pinData : PinData = PinData(geoPoint.latitude,geoPoint.longitude,isClean)
                            list.add(pinData)

                        }
                    }

                }
                lifeData.postValue(list)

            }*/

        database.collection("locations")
            .addSnapshotListener { value, e ->

                val list: MutableList<PinData> = mutableListOf<PinData>()

                if (e == null) {

                    for (document in value!!) {
                        val geoPoint = document.getGeoPoint("coordinates")
                        val isClean = document.getBoolean("isClean") ?: false


                        if (geoPoint != null) {
                            val pinData: PinData =
                                PinData(geoPoint.latitude, geoPoint.longitude, isClean,document.id)
                            list.add(pinData)

                        }
                    }

                }


                lifeData.postValue(list)
            }



        }

    }




