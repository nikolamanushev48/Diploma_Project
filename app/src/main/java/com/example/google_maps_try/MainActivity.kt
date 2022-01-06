package com.example.google_maps_try


import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*
import java.sql.DriverManager.println
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val map : MapActivity = MapActivity();
    val fm = supportFragmentManager

    lateinit var pinCount : TextView

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    lateinit var positionSave : Location

    val result_distance = FloatArray(100)

    var br_markers : Int = 0

    val result_loc: ArrayList<GeoPoint> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        positionSave = Location("")
        checkLocationPermissions()


        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val addButton: Button = findViewById(R.id.location_button)

        addButton.setOnClickListener(){

           val intent= Intent(this, MapActivity::class.java)
           finish();
           overridePendingTransition(0, 0);
           startActivity(intent);
           println("In map activity!!!")
        }

    }

override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    val database = FirebaseFirestore.getInstance()
    pinCount = findViewById(R.id.numberPins)

    val position = LatLng(41.4314, 25.0519)


database.collection("locations")
    .get()
    .addOnCompleteListener{ task ->

        if (task.isSuccessful) {
            var i = 0;
            for (document in task.result) {
                result_loc.add(document.data.getValue("coordinates") as GeoPoint)

                val geoPoint = document.getGeoPoint("coordinates")

                Location.distanceBetween(positionSave.latitude, positionSave.longitude, result_loc[i].latitude, result_loc[i].longitude, result_distance)

                if (geoPoint != null) {
                    val marker = MarkerOptions()
                        .position(LatLng(result_loc[i].latitude, result_loc[i].longitude))
                    mMap.addMarker(marker)

                }

                br_markers = task.result.size()
                pinCount.setText(br_markers.toString());

                i++;
            }

        } else {
            Log.w(TAG, "Error getting documents.", task.exception)
        }

        }


    mMap.setOnMarkerClickListener {
        val intent= Intent(this, MarkerActivity::class.java)
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        println("In marker activity!!!")
        true
    }




    val delButton: Button = findViewById(R.id.delete_button)
    delButton.setOnClickListener() {



        mMap.setOnMarkerClickListener {
            it.remove()
            println("MARKER CLICKED !!!!!!!!!!!!!!!!!!!!!!")

           //val docRef : DocumentReference = database.collection("locations").document()

            database.collection("locations").whereEqualTo("coordinates",GeoPoint(it.position.latitude,it.position.longitude))
                    .get().addOnCompleteListener(){
                val docSnapshot : DocumentSnapshot = it.result.documents.get(0)
                val docID : String = docSnapshot.id
                database.collection("locations").document(docID).delete().addOnCompleteListener(){ task->
                    if(task.isSuccessful){
                        println("LOCATION DELETED" + task.result)
                        br_markers--
                        pinCount.setText(br_markers.toString())
                    }else{
                        println("NOT SUCCEEDED")
                    }
                }

            }


            mMap.setOnMarkerClickListener(null);
            true
        }
    }

    }


    private fun checkLocationPermissions(){
        val task = fusedLocationProviderClient.lastLocation

        println("IN FUNC")

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            println("IN ERRR")
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)

                return
        }


        task.addOnSuccessListener { location: Location? ->
            if (location != null) {
                println("LOCATIONN :" + location.latitude)
                positionSave.latitude = location.latitude
                positionSave.longitude = location.longitude
                positionSave = location
                mMap.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("MyPosition"))

                val radius:Double = 3500.0

                for (i in 0..br_markers) {
                    //if(result_distance[i] < radius){
                        println("IN BORDERS WITH THE RADIUS " + result_distance[0])
                    //}
                }

                mMap.addCircle(
                        CircleOptions()
                                .center(LatLng(location.latitude, location.longitude))
                                .radius(radius)
                                .strokeWidth(3f)
                                .fillColor(Color.argb(70, 240, 37, 14))
                )

                //Location.distanceBetween(location.latitude,location.longitude,)

            }

        }
    }

    fun test(){
    println("TeST FUNC!")
    }


}



