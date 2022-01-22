package com.example.google_maps_try

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.sql.DriverManager.println
import java.util.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var m: Marker? = null
    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
           .findFragmentById(R.id.map_secondary_page) as SupportMapFragment
        mapFragment.getMapAsync(this)



        val button: Button = findViewById(R.id.maps_close_button)
        button.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            println("In map activity!!!")
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val list: MutableList<LatLng> = ArrayList()


        val position = LatLng(41.4314, 25.0519)
        mMap.addCircle(
                CircleOptions()
                        .center(position)
                        .radius(3500.0)
                        .strokeWidth(3f)
                        .fillColor(Color.argb(70,240, 37, 14))
        )

        mMap.setOnMapClickListener() { point ->
            var br = 1;
            var br2 = 2;

            val database = FirebaseFirestore.getInstance()

            val user_location: MutableMap<String, GeoPoint> = HashMap()

            user_location.put("coordinates",GeoPoint(point.latitude,point.longitude))

            database.collection("locations")
                    .add(user_location)
                    .addOnSuccessListener(OnSuccessListener<DocumentReference> {
                        documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id)
                    })
                    .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error adding document", e) })

            database.collection("locations").document().update("isClean",false)




            val marker = MarkerOptions()
                .position(LatLng(point.latitude, point.longitude))
                .title("Destination $br")


            googleMap.addMarker(marker)

            println(point.latitude.toString() + "---" + point.longitude)

            br++
            if(br == br2){
                br2++

                val intent = Intent(this, MainActivity::class.java)
                finish();
                startActivity(intent)
            }
        }

    }
}


