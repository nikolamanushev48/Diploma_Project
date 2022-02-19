package org.elsys.diploma

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.properties.Delegates

class ApiServiceImpl : AppCompatActivity(), ApiService {

    init {
        getLocations()
    }

    private val lifeData: MutableLiveData<List<MarkerData>> = MutableLiveData(listOf())

    private val storageRef = FirebaseStorage.getInstance().reference

    private val database get() = FirebaseFirestore.getInstance()

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun locationLoadData(): LiveData<List<MarkerData>> {
        return lifeData
    }

    private fun getLocations() {

        database.collection("locations").addSnapshotListener { value, e ->
            val list: MutableList<MarkerData> = mutableListOf()

            if (e == null) {

                for (document in value!!) {
                    val geoPoint = document.getGeoPoint("coordinates")
                    val isClean = document.getBoolean("isClean") ?: false
                    val creator = document.getString("creator") ?: "no value"


                    if (geoPoint != null) {
                        val pinData =
                            MarkerData(
                                geoPoint.latitude,
                                geoPoint.longitude,
                                isClean,
                                document.id,
                                creator
                            )
                        list.add(pinData)
                    }
                }
            }
            lifeData.postValue(list)
        }
    }

    override fun addLocationData(position: LatLng) {

        val docData = hashMapOf(
            "coordinates" to GeoPoint(position.latitude, position.longitude),
            "isClean" to false,
            "creator" to firebaseAuth.currentUser!!.email!!
        )


        database
            .collection("locations")
            .add(docData)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: " + documentReference.id)
            }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error adding document", e) }

    }


    override fun imageSave(
        image: Bitmap,
        documentId: String,
        trashCleanedMode: Boolean,
        onSuccess: (uri: Uri) -> Unit
    ) {

        val tempAddress = UUID.randomUUID().toString()

        val photoRef = storageRef.child("photos").child(tempAddress)

        val imageByteSave = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, imageByteSave)
        val dataBytes = imageByteSave.toByteArray()

        val localFile = File.createTempFile("images", "jpg")
        localFile.writeBytes(dataBytes)

        photoRef.putBytes(dataBytes).addOnCompleteListener {

            onSuccess(localFile.toUri())

            val docRef = database.collection("locations").document(documentId)

            if (trashCleanedMode) {
                docRef.update(
                    "photoAddress",
                    tempAddress,
                    "isClean",
                    true,
                    "trashCleanedBy",
                    firebaseAuth.currentUser!!.email
                )
            } else {
                docRef.update("photoAddress", tempAddress)
            }

        }

    }

    override fun displayImage(documentId: String, onSuccess: (uri: Uri) -> Unit) {

        database.collection("locations").document(documentId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val temp = task.result.getString("photoAddress")

                    if (temp != null) {
                        val photoRef = storageRef.child("photos").child(temp)

                        val localFile = File.createTempFile("images", "jpg")

                        photoRef.getFile(localFile)
                            .addOnSuccessListener { onSuccess(localFile.toUri()) }
                    }
                } else {
                    Log.w(ContentValues.TAG, "Error getting documents.", task.exception)
                }
            }

    }

    override fun deleteLocation(marker: Marker, onComplete: (bool: Boolean) -> Unit) {
        var succeeded = true



        database
            .collection("locations")
            .whereEqualTo(
                "coordinates",
                GeoPoint(marker.position.latitude, marker.position.longitude)
            )
            .get()
            .addOnCompleteListener {
                val docSnapshot: DocumentSnapshot = it.result.documents.get(0)
                val docID: String = docSnapshot.id


                if (firebaseAuth.currentUser!!.email == docSnapshot.getString("creator")) {

                    database.collection("locations").document(docID).delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("LOCATION DELETED" + task.result)

                            } else {
                                println("NOT SUCCEEDED")
                                succeeded = false
                                onComplete(succeeded)
                            }
                        }
                } else {
                    Log.i("test", "Error toast!")
                    succeeded = false
                    onComplete(succeeded)
                }


            }
    }


    override fun currentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun login(email: String, password: String, onComplete: (bool: Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                onComplete(task.isSuccessful)
            }
    }

    override fun register(email: String, password: String, onComplete: (bool: Boolean) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                onComplete(task.isSuccessful)
            }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }


}