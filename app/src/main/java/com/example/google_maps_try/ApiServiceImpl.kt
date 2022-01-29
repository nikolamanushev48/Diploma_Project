package com.example.google_maps_try

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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


    override fun locationLoadData(): LiveData<List<PinData>> {
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


    override fun displayImage(documentId: String) {

        val database = FirebaseFirestore.getInstance()

        database.collection("locations").document(documentId)
            .get()
            .addOnCompleteListener{ task ->

                if (task.isSuccessful) {
                    val temp = task.result.getString("photoAddress")

                    if(temp != null){
                        val photoRef = storageRef.child("photos")
                            .child(temp)



                        val localFile = File.createTempFile("images", "jpg")

                        photoRef.getFile(localFile).addOnSuccessListener {
                            imageViewRef.setImageURI(localFile.toUri())
                        }
                    }

                } else {
                    Log.w(ContentValues.TAG, "Error getting documents.", task.exception)
                }

            }
    }

    override fun addLocationData(position: LatLng) {

            val database = FirebaseFirestore.getInstance()

            val user_location: MutableMap<String, GeoPoint> = HashMap()

            user_location.put("coordinates",GeoPoint(position.latitude,position.longitude))

            database.collection("locations")
                .add(user_location)
                .addOnSuccessListener(OnSuccessListener<DocumentReference> {
                        documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id)
                })
                .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error adding document", e) })

            database.collection("locations").document().update("isClean",false)


        }


    override fun deleteLocation(marker : Marker) : Boolean {
        var succeeded : Boolean = false
        val database = FirebaseFirestore.getInstance()
        Log.i("loc", "CORRECT LOCATION ID REGULAR!!!!")
                database.collection("locations").whereEqualTo("coordinates", GeoPoint(marker.position.latitude, marker.position.longitude))
                    .get().addOnCompleteListener() {


                        val docSnapshot: DocumentSnapshot = it.result.documents.get(0)
                        val docID: String = docSnapshot.id
                        database.collection("locations").document(docID).delete()
                            .addOnCompleteListener() { task ->
                                if (task.isSuccessful) {
                                    println("LOCATION DELETED" + task.result)
                                    succeeded = true
                                } else {
                                    println("NOT SUCCEEDED")
                                }
                            }

                    }

        return succeeded
    }

    private fun getLocations() {


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
        val database = FirebaseFirestore.getInstance()
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




