package com.example.google_maps_try


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
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

    lateinit var pinCount : TextView

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    lateinit var positionSave : Location

    val result_distance = FloatArray(100)

    var br_markers : Int = 0

    val result_loc: MutableMap<GeoPoint,String> = HashMap()

    lateinit var documentId : String

    lateinit var tempDocId : String

    val database = FirebaseFirestore.getInstance()

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                //mapGeneration()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        (application as MyApplication).apiService.locationData().observe(this, {
            updateMap(it)
        })

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        positionSave = Location("")


        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val addButton: Button = findViewById(R.id.location_button)

        addButton.setOnClickListener(){

           val intent= Intent(this, MapActivity::class.java).putExtra("docCleaned",0)
           finish();
           overridePendingTransition(0, 0);
           startActivity(intent);
           println("In map activity!!!")
        }


        val delButton: Button = findViewById(R.id.delete_button)
        delButton.setOnClickListener() {

            mMap!!.setOnMarkerClickListener {
                it.remove()
                println("MARKER CLICKED !!!!!!!!!!!!!!!!!!!!!!")


                Log.i("loc", "CORRECT LOCATION ID REGULAR!!!!")
                database.collection("locations").whereEqualTo("coordinates", GeoPoint(it.position.latitude, it.position.longitude))
                    .get().addOnCompleteListener() {

                        val docSnapshot: DocumentSnapshot = it.result.documents.get(0)
                        val docID: String = docSnapshot.id
                        database.collection("locations").document(docID).delete()
                            .addOnCompleteListener() { task ->
                                if (task.isSuccessful) {
                                    println("LOCATION DELETED" + task.result)
                                    br_markers--
                                    pinCount.setText(br_markers.toString())
                                } else {
                                    println("NOT SUCCEEDED")
                                }
                            }

                    }

                mMap!!.setOnMarkerClickListener(null);
                true
            }
        }




    }


    fun updateMap(data : List<PinData>){
        if(mMap != null){
            mMap?.clear()
            for(pinData in data){

                if(pinData.isCleaned){
                    mMap?.addMarker(MarkerOptions().position(LatLng(pinData.latitude, pinData.longitude)).icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN)));
                }else {
                    val marker = MarkerOptions()
                        .position(LatLng(pinData.latitude, pinData.longitude))
                    mMap!!.addMarker(marker)
                }

                result_loc.put(GeoPoint(pinData.latitude,pinData.longitude),pinData.documentId)
            }



            br_markers = data.size
            pinCount.setText(br_markers.toString());


        }
    }


//    fun mapGeneration(){
//
//        mMap?.clear()
//
//        database.collection("locations")
//            .get()
//            .addOnCompleteListener{ task ->
//
//                if (task.isSuccessful) {
//
//                    for (document in task.result) {
//                        val geoPoint = document.getGeoPoint("coordinates")
//
//                        Log.i("geo",geoPoint!!.latitude.toString())
//
//                        val isClean = document.getBoolean("isClean")
//
//                        if(isClean == true){
//                            mMap?.addMarker(MarkerOptions().position(LatLng(geoPoint.latitude, geoPoint.longitude)).icon(BitmapDescriptorFactory.defaultMarker(
//                                BitmapDescriptorFactory.HUE_GREEN)));
//                        }else{
//
//                                val marker = MarkerOptions()
//                                    .position(LatLng(geoPoint.latitude, geoPoint.longitude))
//                                mMap?.addMarker(marker)
//                            }
//
//                        result_loc.put(geoPoint,document.id)
//
//
//                        Location.distanceBetween(positionSave.latitude, positionSave.longitude, geoPoint.latitude, geoPoint.longitude, result_distance)
//
//
//
//                    }
//
//                } else {
//                    Log.w(TAG, "Error getting documents.", task.exception)
//                }
//
//            }
//    }



override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    checkLocationPermissions()

    pinCount = findViewById(R.id.numberPins)

    val data = (application as MyApplication).apiService.locationData().value

    if (data != null) {
        updateMap(data)
    }
    //mapGeneration()

    mMap!!.setOnMarkerClickListener {
        val positionMarker = GeoPoint(it.position.latitude,it.position.longitude)

        val intentMarkerActivity= Intent(this, MarkerActivity::class.java).putExtra("tempMarkerIntent",result_loc.get(positionMarker))
        overridePendingTransition(0, 0);
        getResult.launch(intentMarkerActivity)
        //startActivity(intentMarkerActivity);
        println("In marker activity!!!")
        true
    }
}





//GeoQueries - da gi rabotim sled 14 mart(predavane na dokumentaciq)
//Do togava ne pipam!!!!!!

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
                mMap?.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("MyPosition"))

                val radius:Double = 3500.0

                for (i in 0..br_markers) {
                        println("IN BORDERS WITH THE RADIUS " + result_distance[0])
                }

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



