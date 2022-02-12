package com.example.google_maps_try

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.*
import java.sql.DriverManager.println
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    private lateinit var pinCount: TextView

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var positionSave: Location

    private var brMarkers: Int = 0

    private val markerSave: MutableMap<GeoPoint, String> = HashMap()

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (application as MyApplication).apiService.locationLoadData().observe(this) { updateMap(it) }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        positionSave = Location("")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val addButton: Button = findViewById(R.id.location_button)

        addButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java).putExtra("docCleaned", 0)
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
        }

        val delButton: Button = findViewById(R.id.delete_button)
        delButton.setOnClickListener {
            mMap!!.setOnMarkerClickListener {
                it.remove()

                if ((application as MyApplication).apiService.deleteLocation(it)) {
                    brMarkers--
                    pinCount.text = brMarkers.toString()
                }

                mMap!!.setOnMarkerClickListener(null)
                true
            }
        }
    }


    private fun updateMap(data: List<PinData>) {
        if (mMap != null) {
            mMap?.clear()
            for (pinData in data) {

                if (pinData.isCleaned) {
                    mMap?.addMarker(
                        MarkerOptions()
                            .position(LatLng(pinData.latitude, pinData.longitude))
                            .icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )
                    )
                } else {
                    val marker = MarkerOptions().position(LatLng(pinData.latitude, pinData.longitude))
                    mMap!!.addMarker(marker)
                }

                markerSave.put(GeoPoint(pinData.latitude, pinData.longitude), pinData.documentId)
            }

            brMarkers = data.size
            pinCount.text = brMarkers.toString()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermissions()

        pinCount = findViewById(R.id.numberPins)

        val data = (application as MyApplication).apiService.locationLoadData().value

        if (data != null) {
            updateMap(data)
        }

        mMap!!.setOnMarkerClickListener {
            val positionMarker = GeoPoint(it.position.latitude, it.position.longitude)

            val intentMarkerActivity =
                Intent(this, MarkerActivity::class.java)
                    .putExtra("tempMarkerIntent", markerSave.get(positionMarker))
            overridePendingTransition(0, 0)
            getResult.launch(intentMarkerActivity)
            true
        }
    }












//GeoQueries - da gi rabotim sled 14 mart(predavane na dokumentaciq)
//Do togava ne pipam!!!!!!

    private fun checkLocationPermissions(){
        val task = fusedLocationProviderClient.lastLocation


        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }


        task.addOnSuccessListener { location: Location? ->
            if (location != null) {
                println("LOCATIONN :" + location.latitude)
                positionSave.latitude = location.latitude
                positionSave.longitude = location.longitude
                positionSave = location
                mMap?.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("MyPosition"))

                val radius = 3500.0


                mMap?.addCircle(
                    CircleOptions()
                        .center(LatLng(location.latitude, location.longitude))
                        .radius(radius)
                        .strokeWidth(3f)
                        .fillColor(Color.argb(70, 240, 37, 14))
                )

            }

        }
    }

}
