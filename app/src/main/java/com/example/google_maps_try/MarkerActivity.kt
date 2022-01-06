package com.example.google_maps_try


import android.R.attr
import android.app.Activity
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat.startActivityForResult

import android.provider.MediaStore

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.google_maps_try.databinding.ActivityMarkerBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.sql.DriverManager
import com.google.firebase.storage.StorageReference
import java.util.*
import android.R.attr.data

import android.R.attr.bitmap





lateinit var imageUri : Uri
lateinit var storageRef: StorageReference


class MarkerActivity : AppCompatActivity() {
    //val database = FirebaseFirestore.getInstance()

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data


                //val imageBitmap = data?.extras?.get("data") as Bitmap
                //imageView.setImageBitmap(imageBitmap)



                //imageUri = data.data!!



                val imageBitmap : Uri
                if (data?.data == null) {
                    imageBitmap = data?.extras!!.get("data") as Uri
                } else {
                    imageBitmap =  data.data as Uri
                }

                storageRef.putFile(imageBitmap)

            }
        }

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                resultLauncher.launch(takePictureIntent)
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    //    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marker)
        storageRef = FirebaseStorage.getInstance().reference



        val buttonMarker: Button = findViewById(R.id.buttonMarker)
//        button.setOnClickListener(View.OnClickListener { view ->
//            // Do some work here
//        })
        buttonMarker.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    resultLauncher.launch(takePictureIntent)
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.
//            showInContextUI(...)
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        android.Manifest.permission.CAMERA
                    )
                }
            }
        }

        val buttonBackToMap: Button = findViewById(R.id.buttonBackToMap)

        buttonBackToMap.setOnClickListener {
            val intent= Intent(this, MainActivity::class.java)
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            DriverManager.println("In main activity!!!")
        }
    }
}