package org.elsys.diploma

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.annotation.SuppressLint
import android.widget.ImageView
import com.example.google_maps_try.R


lateinit var storageRef: StorageReference

@SuppressLint("StaticFieldLeak")
lateinit var imageViewRef : ImageView


class MarkerActivity : AppCompatActivity() {
    private lateinit var documentId : String

    private var tempButtonResult : Int = 0

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data

                val imageBitmap = data?.extras?.get("data") as Bitmap

                (application as MyApplication).apiService.imageSave(imageBitmap,documentId,tempButtonResult == 2)

                if(tempButtonResult == 2){
                    val trashDoneIntent = Intent(this, MainActivity::class.java).putExtra("doneMarkerDocId", documentId)
                    setResult(Activity.RESULT_OK,trashDoneIntent)
                    finish()
                }


            }

        }



    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                resultLauncher.launch(takePictureIntent)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marker)
        storageRef = FirebaseStorage.getInstance().reference

        val intent : Intent = intent
        documentId = intent.getStringExtra("tempMarkerIntent")!!


        val buttonMarker: Button = findViewById(R.id.buttonMarker)

        buttonMarker.setOnClickListener {

            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {

                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    resultLauncher.launch(takePictureIntent)
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {

                }
                else -> {
                    requestPermissionLauncher.launch(
                        android.Manifest.permission.CAMERA
                    )
                }

            }

        }

        val buttonBackToMap: Button = findViewById(R.id.buttonBackToMap)

        buttonBackToMap.setOnClickListener {
            finish()
        }




        val buttonTrashDone: Button = findViewById(R.id.trashDone)

        buttonTrashDone.setOnClickListener {
            tempButtonResult = 2

                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED -> {

                            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            resultLauncher.launch(takePictureIntent)

                    }
                    shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {

                    }
                    else -> {
                        requestPermissionLauncher.launch(
                            android.Manifest.permission.CAMERA
                        )

                    }
                }
        }



        imageViewRef = findViewById(R.id.imageView)


        (application as MyApplication).apiService.displayImage(documentId)

    }
}