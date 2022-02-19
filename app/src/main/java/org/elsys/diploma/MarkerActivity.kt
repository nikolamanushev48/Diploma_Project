package org.elsys.diploma

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.google_maps_try.R
import com.google.firebase.auth.FirebaseAuth


class MarkerActivity : AppCompatActivity() {
    private lateinit var document: MarkerData

    private var tempButtonResult: Int = 0


    lateinit var imageViewRef: ImageView


    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data

                val imageBitmap = data?.extras?.get("data") as Bitmap

                (application as MyApplication).apiService.imageSave(
                    imageBitmap,
                    document.documentId,
                    tempButtonResult == 2//checking if this condition is true!!!

                ) {
                    imageViewRef.setImageURI(it)
                }

                if (tempButtonResult == 2) {
                    val trashDoneIntent = Intent(this, MainActivity::class.java).putExtra(
                        "doneMarkerDocId",
                        document.documentId
                    )
                    setResult(Activity.RESULT_OK, trashDoneIntent)
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

    private fun cameraPermission() {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marker)


        val intent: Intent = intent
        document = intent.getParcelableExtra<MarkerData>("tempMarkerIntent")!!


        val buttonAddPhoto: Button = findViewById(R.id.buttonAddPhoto)

        val user = FirebaseAuth.getInstance().currentUser

        buttonAddPhoto.setOnClickListener {
            if (user!!.email == document.creator) {
                cameraPermission()
            } else {
                Toast.makeText(
                    this,
                    "You are not allowed to add the current photo!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        val buttonTrashDone: Button = findViewById(R.id.trashDone)

        buttonTrashDone.setOnClickListener {
            tempButtonResult = 2
            cameraPermission()
        }


        imageViewRef = findViewById(R.id.imageView)


        (application as MyApplication).apiService.displayImage(document.documentId) {
            imageViewRef.setImageURI(it)
        }

    }
}